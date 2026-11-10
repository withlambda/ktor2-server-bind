package com.withlambda.ktor2.server.bind.processor.model

/**
 * Root metadata model collected by the KSP processor for a single compilation run.
 *
 * @param endpoints All API endpoints discovered across all [@ApiController][com.withlambda.ktor2.server.bind.ApiController]
 *   annotated classes.
 * @param schemas Component schemas keyed by their simple type name, used to populate the
 *   `components/schemas` section of the generated OpenAPI spec.
 */
data class ApiMetadata(
    val endpoints: List<EndpointMetadata>,
    val schemas: Map<String, SchemaMetadata>
)

/**
 * Metadata for a single API endpoint extracted from an [@ApiEndpoint][com.withlambda.ktor2.server.bind.ApiEndpoint]
 * annotated property inside an [@ApiController][com.withlambda.ktor2.server.bind.ApiController] class.
 *
 * @param path The URL path (e.g. `"/users/{id}"`).
 * @param method The HTTP method in lower-case (e.g. `"get"`, `"post"`).
 * @param summary A short human-readable description of the endpoint.
 * @param tag The controller-level tag used to group endpoints in the OpenAPI spec.
 * @param requestBodyType Fully qualified name of the request body DTO, or `null` when absent.
 * @param responseBodyType Fully qualified name of the response body DTO, or `null` when absent.
 */
data class EndpointMetadata(
    val path: String,
    val method: String,
    val summary: String,
    val tag: String,
    val requestBodyType: String?,
    val responseBodyType: String?
)

/**
 * OpenAPI schema for a single DTO type, used to populate `components/schemas`.
 *
 * @param typeName Simple name of the type (used as the schema key).
 * @param properties Map of property name to its [PropertySchema].
 */
data class SchemaMetadata(
    val typeName: String,
    val properties: Map<String, PropertySchema>
)

/**
 * Schema for a single property within a [SchemaMetadata].
 *
 * @param type OpenAPI type string (e.g. `"string"`, `"integer"`, `"array"`, `"object"`).
 * @param nullable Whether the property is nullable.
 * @param items For `array` types, the schema of the array element; `null` otherwise.
 * @param ref For object references, the `${'$'}ref` value pointing to `#/components/schemas/…`; `null` otherwise.
 */
data class PropertySchema(
    val type: String,
    val nullable: Boolean = false,
    val items: PropertySchema? = null,
    val ref: String? = null
)
