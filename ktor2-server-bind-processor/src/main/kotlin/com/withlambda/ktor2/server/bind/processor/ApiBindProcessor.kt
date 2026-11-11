package io.withlambda.ktor2.server.bind.processor

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.withlambda.ktor2.server.bind.processor.model.ApiMetadata
import io.withlambda.ktor2.server.bind.processor.model.EndpointMetadata

/**
 * KSP [SymbolProcessor] that discovers all classes annotated with
 * [@ApiController][io.withlambda.ktor2.server.bind.ApiController], extracts endpoint and DTO
 * metadata from their [@ApiEndpoint][io.withlambda.ktor2.server.bind.ApiEndpoint]-annotated
 * properties, and writes an `openapi.json` resource file on [finish].
 *
 * Schema collection is delegated to [SchemaCollector].
 *
 * @param environment The KSP environment providing logger, code generator, and options.
 */
class ApiBindProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val logger = environment.logger
    private val codeGenerator = environment.codeGenerator

    private val endpoints = mutableListOf<EndpointMetadata>()
    private val schemaCollector = SchemaCollector()

    /**
     * Holds the annotation arguments extracted from an
     * [@ApiEndpoint][io.withlambda.ktor2.server.bind.ApiEndpoint] annotation together with the
     * resolved property type, keeping [processEndpointProperty] within the allowed return count.
     *
     * @param path URL path declared in the annotation.
     * @param summary Human-readable summary declared in the annotation.
     * @param resolvedType The KSP-resolved type of the annotated property.
     */
    private data class EndpointAnnotationArgs(
        val path: String,
        val summary: String,
        val resolvedType: KSType
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation(API_CONTROLLER_FQ_NAME)
            .filterIsInstance<KSClassDeclaration>()
            .forEach { processController(it, resolver) }
        return emptyList()
    }

    override fun finish() {
        if (endpoints.isEmpty()) {
            logger.info("ApiBindProcessor: no @ApiController classes found — skipping openapi.json generation")
            return
        }
        val metadata = ApiMetadata(endpoints = endpoints.toList(), schemas = schemaCollector.schemas)
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "",
            fileName = "openapi",
            extensionName = "json"
        )
        file.bufferedWriter().use { it.write(OpenApiSerializer.toJson(metadata)) }
        logger.info("ApiBindProcessor: wrote openapi.json with ${endpoints.size} endpoint(s)")
    }

    private fun processController(classDecl: KSClassDeclaration, resolver: Resolver) {
        val tag = classDecl.annotations
            .find { it.shortName.asString() == "ApiController" }
            ?.arguments
            ?.find { it.name?.asString() == "tag" }
            ?.value as? String ?: ""

        classDecl.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .forEach { processEndpointProperty(it, tag, resolver) }
    }

    /**
     * Reads the [@ApiEndpoint][io.withlambda.ktor2.server.bind.ApiEndpoint] annotation from
     * [property] and bundles its arguments with the resolved property type.
     * Returns `null` when the annotation is absent or the path argument is missing.
     *
     * Uses an inline `let` chain so internal guard `return null` statements are excluded
     * from the [ReturnCount] rule via `excludeReturnFromLambda`.
     */
    private fun readEndpointAnnotation(property: KSPropertyDeclaration): EndpointAnnotationArgs? =
        property.annotations.find { it.shortName.asString() == "ApiEndpoint" }
            ?.let { annotation ->
                val path = annotation.arguments
                    .find { it.name?.asString() == "path" }?.value as? String ?: return null
                val summary = annotation.arguments
                    .find { it.name?.asString() == "summary" }?.value as? String ?: ""
                EndpointAnnotationArgs(path, summary, property.type.resolve())
            }

    /**
     * Resolves the lower-case HTTP method for the given [resolvedType] by checking its
     * fully qualified name against the known [RouteConfig] subtypes, then falling back to
     * supertype inspection.
     *
     * @return The HTTP method string, or `null` when the type is not a recognised RouteConfig.
     */
    private fun resolveHttpMethod(resolvedType: KSType): String? {
        val fqName = resolvedType.declaration.qualifiedName?.asString() ?: return null
        return httpMethodFor(fqName)
            ?: httpMethodFromSupertypes(resolvedType.declaration as? KSClassDeclaration)
    }

    private fun processEndpointProperty(
        property: KSPropertyDeclaration,
        tag: String,
        resolver: Resolver
    ) {
        val args = readEndpointAnnotation(property) ?: return
        val method = resolveHttpMethod(args.resolvedType) ?: return

        val requestBodyType = extractRequestBodyType(method, args.resolvedType)
        endpoints.add(
            EndpointMetadata(
                path = args.path,
                method = method,
                summary = args.summary,
                tag = tag,
                requestBodyType = requestBodyType,
                responseBodyType = null
            )
        )

        if (requestBodyType != null) {
            resolver.getClassDeclarationByName(resolver.getKSNameFromString(requestBodyType))
                ?.let { schemaCollector.collect(it, resolver) }
        }
    }

    /**
     * Returns the lower-case HTTP method string for a fully-qualified [RouteConfig] subtype name,
     * or `null` when [fqName] is not a recognised route-config interface.
     */
    private fun httpMethodFor(fqName: String): String? = when {
        fqName.endsWith("GetRouteConfig") -> "get"
        fqName.endsWith("PostRouteConfig") -> "post"
        fqName.endsWith("PutRouteConfig") -> "put"
        fqName.endsWith("DeleteRouteConfig") -> "delete"
        fqName.endsWith("PatchRouteConfig") -> "patch"
        fqName.endsWith("HeadRouteConfig") -> "head"
        fqName.endsWith("OptionsRouteConfig") -> "options"
        else -> null
    }

    /**
     * Walks the supertype chain of [classDecl] to find a [RouteConfig] subtype and return
     * its HTTP method. Used when the declared property type is an indirect implementor.
     */
    private fun httpMethodFromSupertypes(classDecl: KSClassDeclaration?): String? =
        classDecl
            ?.getAllSuperTypes()
            ?.mapNotNull { httpMethodFor(it.declaration.qualifiedName?.asString() ?: "") }
            ?.firstOrNull()

    /**
     * Extracts the fully qualified request body type from the first generic type argument of
     * POST, PUT, and PATCH [RouteConfig] supertypes. Returns `null` for methods without a body
     * or when the type argument resolves to [Unit].
     */
    private fun extractRequestBodyType(method: String, resolvedType: KSType): String? =
        if (method !in setOf("post", "put", "patch")) {
            null
        } else {
            resolvedType.arguments.firstOrNull()
                ?.type?.resolve()
                ?.declaration?.qualifiedName?.asString()
                ?.takeIf { it != "kotlin.Unit" }
        }

    private companion object {
        const val API_CONTROLLER_FQ_NAME = "io.withlambda.ktor2.server.bind.ApiController"
    }
}
