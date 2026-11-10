package com.withlambda.ktor2.server.bind.processor

import com.withlambda.ktor2.server.bind.processor.model.ApiMetadata
import com.withlambda.ktor2.server.bind.processor.model.EndpointMetadata
import com.withlambda.ktor2.server.bind.processor.model.PropertySchema
import com.withlambda.ktor2.server.bind.processor.model.SchemaMetadata

/**
 * Converts an [ApiMetadata] model into a valid OpenAPI 3.0.3 JSON string without
 * requiring any external JSON serialisation library.
 *
 * The targeted OpenAPI Specification version is exposed as [OPENAPI_VERSION]. Note that
 * different spec versions (e.g. 3.1.x) have incompatible schema semantics — for example,
 * nullable fields use `"nullable": true` in 3.0.x but `"type": ["T", "null"]` in 3.1.x —
 * so changing [OPENAPI_VERSION] alone would produce an invalid document. Supporting a
 * different version requires a dedicated serialiser implementation.
 */
object OpenApiSerializer {

    /** The OpenAPI Specification version this serialiser targets. */
    const val OPENAPI_VERSION = "3.0.3"

    // JSON key for OpenAPI schema references. Stored as a constant to avoid raw-string
    // dollar-sign escaping issues (${'$'}ref) throughout the serialiser.
    private const val DOLLAR_REF = "\$ref"

    /**
     * Serialises [metadata] to an OpenAPI [OPENAPI_VERSION] JSON string.
     *
     * @param metadata The collected API metadata produced by the KSP processor.
     * @return A formatted JSON string ready to be written to `openapi.json`.
     */
    fun toJson(metadata: ApiMetadata): String = buildString {
        appendLine("{")
        appendLine("""  "openapi": "$OPENAPI_VERSION",""")
        appendLine("""  "info": {""")
        appendLine("""    "title": "API",""")
        appendLine("""    "version": "1.0.0"""")
        appendLine("  },")
        appendLine("""  "paths": {""")
        appendPaths(metadata.endpoints)
        appendLine("  },")
        appendLine("""  "components": {""")
        appendLine("""    "schemas": {""")
        appendSchemas(metadata.schemas.values.toList())
        appendLine("    }")
        appendLine("  }")
        append("}")
    }

    private fun StringBuilder.appendPaths(endpoints: List<EndpointMetadata>) {
        // Group endpoints by path so multiple methods on the same path are nested correctly.
        val byPath = endpoints.groupBy { it.path }
        byPath.entries.forEachIndexed { pathIndex, (path, methods) ->
            appendLine("""    "${escapeJson(path)}": {""")
            methods.forEachIndexed { methodIndex, endpoint ->
                appendLine("""      "${escapeJson(endpoint.method)}": {""")
                if (endpoint.summary.isNotBlank()) {
                    appendLine("""        "summary": "${escapeJson(endpoint.summary)}",""")
                }
                if (endpoint.tag.isNotBlank()) {
                    appendLine("""        "tags": ["${escapeJson(endpoint.tag)}"],""")
                }
                appendRequestBody(endpoint)
                appendLine("""        "responses": {""")
                appendLine("""          "200": {"description": "OK"}""")
                appendLine("        }")
                val isLastMethod = methodIndex == methods.size - 1
                appendLine(if (isLastMethod) "      }" else "      },")
            }
            val isLastPath = pathIndex == byPath.size - 1
            appendLine(if (isLastPath) "    }" else "    },")
        }
    }

    private fun StringBuilder.appendRequestBody(endpoint: EndpointMetadata) {
        val requestType = endpoint.requestBodyType ?: return
        val schemaRef = "#/components/schemas/${requestType.substringAfterLast('.')}"
        appendLine("""        "requestBody": {""")
        appendLine("""          "content": {""")
        appendLine("""            "application/json": {""")
        appendLine("              \"schema\": {\"$DOLLAR_REF\": \"${escapeJson(schemaRef)}\"}")
        appendLine("            }")
        appendLine("          }")
        appendLine("        },")
    }

    private fun StringBuilder.appendSchemas(schemas: List<SchemaMetadata>) {
        schemas.forEachIndexed { index, schema ->
            appendLine("""      "${escapeJson(schema.typeName)}": {""")
            appendLine("""        "type": "object",""")
            appendLine("""        "properties": {""")
            schema.properties.entries.forEachIndexed { propIndex, (propName, propSchema) ->
                val isLastProp = propIndex == schema.properties.size - 1
                val propJson = propertySchemaToJson(propSchema)
                appendLine(
                    if (isLastProp) {
                        "          \"${escapeJson(propName)}\": $propJson"
                    } else {
                        "          \"${escapeJson(propName)}\": $propJson,"
                    }
                )
            }
            appendLine("        }")
            val isLastSchema = index == schemas.size - 1
            appendLine(if (isLastSchema) "      }" else "      },")
        }
    }

    private fun propertySchemaToJson(schema: PropertySchema): String = when {
        schema.ref != null ->
            "{\"$DOLLAR_REF\": \"${escapeJson(schema.ref)}\"}"

        schema.type == "array" && schema.items != null ->
            "{\"type\": \"array\", \"items\": ${propertySchemaToJson(schema.items)}}"

        schema.nullable ->
            "{\"type\": \"${escapeJson(schema.type)}\", \"nullable\": true}"

        else ->
            "{\"type\": \"${escapeJson(schema.type)}\"}"
    }

    /** Escapes characters that are unsafe inside a JSON string value. */
    private fun escapeJson(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
