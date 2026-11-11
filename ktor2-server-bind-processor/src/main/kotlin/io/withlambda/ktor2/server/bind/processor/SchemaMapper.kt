package io.withlambda.ktor2.server.bind.processor

/**
 * Utility object that maps Kotlin fully-qualified type names to their OpenAPI equivalents
 * and builds schema descriptor maps used when serialising the OpenAPI spec to JSON.
 */
object SchemaMapper {

    private val KOTLIN_TO_OPENAPI: Map<String, String> = mapOf(
        "kotlin.String" to "string",
        "kotlin.Char" to "string",
        "kotlin.Int" to "integer",
        "kotlin.Long" to "integer",
        "kotlin.Short" to "integer",
        "kotlin.Byte" to "integer",
        "kotlin.Double" to "number",
        "kotlin.Float" to "number",
        "kotlin.Boolean" to "boolean",
        "kotlin.Unit" to "object"
    )

    private val LIST_TYPES: Set<String> = setOf(
        "kotlin.collections.List",
        "kotlin.collections.MutableList",
        "kotlin.collections.Collection",
        "kotlin.collections.MutableCollection",
        "java.util.List",
        "java.util.ArrayList"
    )

    private val MAP_TYPES: Set<String> = setOf(
        "kotlin.collections.Map",
        "kotlin.collections.MutableMap",
        "java.util.Map"
    )

    /**
     * Returns the OpenAPI type string for the given Kotlin fully-qualified type name.
     * Falls back to `"object"` for unknown or complex types.
     *
     * @param kotlinFqName Fully-qualified Kotlin type name (e.g. `"kotlin.String"`).
     * @return OpenAPI type string such as `"string"`, `"integer"`, `"number"`, `"boolean"`, or `"object"`.
     */
    fun kotlinTypeToOpenApi(kotlinFqName: String): String =
        KOTLIN_TO_OPENAPI[kotlinFqName] ?: "object"

    /**
     * Returns `true` when [kotlinFqName] represents a list or collection type.
     *
     * @param kotlinFqName Fully-qualified Kotlin type name.
     */
    fun isListType(kotlinFqName: String): Boolean = kotlinFqName in LIST_TYPES

    /**
     * Returns `true` when [kotlinFqName] represents a map type.
     *
     * @param kotlinFqName Fully-qualified Kotlin type name.
     */
    fun isMapType(kotlinFqName: String): Boolean = kotlinFqName in MAP_TYPES

    /**
     * Builds a property schema descriptor map for use inside an OpenAPI `properties` object.
     *
     * @param kotlinFqName Fully-qualified Kotlin type name of the property.
     * @param nullable Whether the property is nullable.
     * @return A [Map] with at least a `"type"` key and optionally a `"nullable"` key.
     */
    fun buildPropertySchema(kotlinFqName: String, nullable: Boolean): Map<String, Any> {
        val type = kotlinTypeToOpenApi(kotlinFqName)
        return if (nullable) {
            mapOf("type" to type, "nullable" to true)
        } else {
            mapOf("type" to type)
        }
    }

    /**
     * Builds an `array` schema descriptor whose `items` resolve to [elementKotlinFqName].
     *
     * @param elementKotlinFqName Fully-qualified Kotlin type name of the list element.
     * @return A [Map] with `"type": "array"` and an `"items"` entry.
     */
    fun buildArraySchema(elementKotlinFqName: String): Map<String, Any> =
        mapOf(
            "type" to "array",
            "items" to mapOf("type" to kotlinTypeToOpenApi(elementKotlinFqName))
        )

    /**
     * Builds a `${'$'}ref` schema descriptor pointing to a named component schema.
     *
     * @param typeName Simple name of the referenced schema (e.g. `"HelloWorldDTO"`).
     * @return A [Map] with a single `"${'$'}ref"` key.
     */
    fun buildRefSchema(typeName: String): Map<String, Any> =
        mapOf("\$ref" to "#/components/schemas/$typeName")
}
