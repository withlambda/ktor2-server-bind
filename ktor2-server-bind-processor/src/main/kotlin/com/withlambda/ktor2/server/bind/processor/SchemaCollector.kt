package com.withlambda.ktor2.server.bind.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.withlambda.ktor2.server.bind.processor.model.PropertySchema
import com.withlambda.ktor2.server.bind.processor.model.SchemaMetadata

/**
 * Recursively collects [SchemaMetadata] entries for DTO types encountered during KSP processing.
 *
 * Schemas are accumulated in [schemas] and can be retrieved after all processing is complete.
 * Already-visited types are skipped to prevent infinite recursion on self-referential types.
 */
internal class SchemaCollector {

    /** Accumulated component schemas keyed by simple type name. */
    val schemas: Map<String, SchemaMetadata> get() = _schemas.toMap()

    private val _schemas = mutableMapOf<String, SchemaMetadata>()

    /**
     * Visits [classDecl] and all complex property types it references, building a
     * [SchemaMetadata] entry for each. No-ops for already-collected types.
     *
     * @param classDecl The DTO class to introspect.
     * @param resolver KSP [Resolver] used to look up nested type declarations.
     */
    fun collect(classDecl: KSClassDeclaration, resolver: Resolver) {
        val typeName = classDecl.simpleName.asString()
        if (typeName in _schemas) return

        // Insert a placeholder early to prevent infinite recursion on self-referential types.
        _schemas[typeName] = SchemaMetadata(typeName = typeName, properties = emptyMap())

        val properties = classDecl.getAllProperties()
            .associate { prop ->
                val propType = prop.type.resolve()
                val propFqName = propType.declaration.qualifiedName?.asString() ?: "kotlin.String"
                val nullable = propType.isMarkedNullable
                prop.simpleName.asString() to buildPropertySchemaFor(propFqName, propType, nullable, resolver)
            }

        _schemas[typeName] = SchemaMetadata(typeName = typeName, properties = properties)
    }

    /**
     * Builds a [PropertySchema] for the given type, handling arrays, maps, primitive types,
     * and complex object references recursively.
     *
     * @param fqName Fully-qualified Kotlin type name of the property.
     * @param resolvedType The KSP-resolved type (needed for generic argument inspection).
     * @param nullable Whether the property is nullable.
     * @param resolver KSP [Resolver] used for recursive nested type lookup.
     * @return A [PropertySchema] describing the property for the OpenAPI spec.
     */
    private fun buildPropertySchemaFor(
        fqName: String,
        resolvedType: KSType,
        nullable: Boolean,
        resolver: Resolver
    ): PropertySchema = when {
        SchemaMapper.isListType(fqName) -> {
            val elemFqName = resolvedType.arguments.firstOrNull()
                ?.type?.resolve()?.declaration?.qualifiedName?.asString() ?: "kotlin.String"
            PropertySchema(
                type = "array",
                nullable = nullable,
                items = PropertySchema(type = SchemaMapper.kotlinTypeToOpenApi(elemFqName))
            )
        }

        SchemaMapper.isMapType(fqName) -> PropertySchema(type = "object", nullable = nullable)

        SchemaMapper.kotlinTypeToOpenApi(fqName) != "object" ->
            PropertySchema(type = SchemaMapper.kotlinTypeToOpenApi(fqName), nullable = nullable)

        else -> {
            // Complex type — collect its schema recursively and emit a $ref.
            val nestedDecl = resolvedType.declaration
            if (nestedDecl is KSClassDeclaration) collect(nestedDecl, resolver)
            PropertySchema(
                type = "object",
                nullable = nullable,
                ref = "#/components/schemas/${nestedDecl.simpleName.asString()}"
            )
        }
    }
}
