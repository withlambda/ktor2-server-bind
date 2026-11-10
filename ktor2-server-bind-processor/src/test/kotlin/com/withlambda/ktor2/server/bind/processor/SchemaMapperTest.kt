package com.withlambda.ktor2.server.bind.processor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [SchemaMapper], verifying that Kotlin type names are correctly
 * mapped to their OpenAPI equivalents and that nested/complex types produce
 * valid `components/schemas` entries.
 */
class SchemaMapperTest {

    /**
     * Verifies that primitive Kotlin types map to the correct OpenAPI type strings.
     */
    @Test
    fun `maps primitive kotlin types to openapi types`() {
        assertEquals("string", SchemaMapper.kotlinTypeToOpenApi("kotlin.String"))
        assertEquals("integer", SchemaMapper.kotlinTypeToOpenApi("kotlin.Int"))
        assertEquals("integer", SchemaMapper.kotlinTypeToOpenApi("kotlin.Long"))
        assertEquals("number", SchemaMapper.kotlinTypeToOpenApi("kotlin.Double"))
        assertEquals("number", SchemaMapper.kotlinTypeToOpenApi("kotlin.Float"))
        assertEquals("boolean", SchemaMapper.kotlinTypeToOpenApi("kotlin.Boolean"))
    }

    /**
     * Verifies that an unknown or complex type falls back to `"object"`.
     */
    @Test
    fun `maps unknown type to object`() {
        assertEquals("object", SchemaMapper.kotlinTypeToOpenApi("com.example.MyComplexDTO"))
    }

    /**
     * Verifies that [SchemaMapper.buildPropertySchema] produces a schema map with `type`
     * set for a simple string property.
     */
    @Test
    fun `builds simple string property schema`() {
        val schema = SchemaMapper.buildPropertySchema("kotlin.String", nullable = false)
        assertEquals("string", schema["type"])
    }

    /**
     * Verifies that a nullable property schema includes `nullable: true`.
     */
    @Test
    fun `builds nullable property schema with nullable flag`() {
        val schema = SchemaMapper.buildPropertySchema("kotlin.Int", nullable = true)
        assertEquals("integer", schema["type"])
        assertEquals(true, schema["nullable"])
    }

    /**
     * Verifies that a List type produces an `array` schema with the correct `items` reference.
     */
    @Test
    fun `builds array schema for list type`() {
        val schema = SchemaMapper.buildArraySchema("kotlin.String")
        assertEquals("array", schema["type"])
        val items = schema["items"]
        assertNotNull(items, "Expected 'items' key in array schema")
        assertTrue(items is Map<*, *>, "Expected 'items' to be a Map")
        @Suppress("UNCHECKED_CAST")
        assertEquals("string", (items as Map<String, Any>)["type"])
    }

    /**
     * Verifies that [SchemaMapper.isListType] returns true for `kotlin.collections.List` variants.
     */
    @Test
    fun `identifies list types correctly`() {
        assertTrue(SchemaMapper.isListType("kotlin.collections.List"))
        assertTrue(SchemaMapper.isListType("kotlin.collections.MutableList"))
        assertTrue(SchemaMapper.isListType("java.util.List"))
    }
}
