package com.withlambda.ktor2.server.bind.processor

import com.withlambda.ktor2.server.bind.processor.model.ApiMetadata
import com.withlambda.ktor2.server.bind.processor.model.EndpointMetadata
import com.withlambda.ktor2.server.bind.processor.model.PropertySchema
import com.withlambda.ktor2.server.bind.processor.model.SchemaMetadata
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for the full metadata → OpenAPI JSON pipeline via [OpenApiSerializer].
 *
 * These tests verify that [ApiMetadata] produced by [ApiBindProcessor] is correctly serialised
 * to a valid OpenAPI 3.0.3 JSON document, covering paths, HTTP methods, request bodies, and
 * component schemas.
 */
class ApiBindProcessorTest {

    private val sampleMetadata = ApiMetadata(
        endpoints = listOf(
            EndpointMetadata(
                path = "/sample",
                method = "get",
                summary = "Get sample",
                tag = "sample",
                requestBodyType = null,
                responseBodyType = null
            ),
            EndpointMetadata(
                path = "/sample",
                method = "post",
                summary = "Create sample",
                tag = "sample",
                requestBodyType = "com.example.SampleDTO",
                responseBodyType = null
            )
        ),
        schemas = mapOf(
            "SampleDTO" to SchemaMetadata(
                typeName = "SampleDTO",
                properties = mapOf(
                    "name" to PropertySchema(type = "string"),
                    "count" to PropertySchema(type = "integer", nullable = true)
                )
            )
        )
    )

    /**
     * Verifies that the generated JSON contains the declared paths and HTTP methods.
     */
    @Test
    fun `generated openapi json contains declared paths and methods`() {
        val json = OpenApiSerializer.toJson(sampleMetadata)
        assertTrue(json.contains("/sample"), "Expected path '/sample' in openapi.json")
        assertTrue(json.contains("\"get\""), "Expected method 'get' in openapi.json")
        assertTrue(json.contains("\"post\""), "Expected method 'post' in openapi.json")
    }

    /**
     * Verifies that the generated JSON contains the OpenAPI version and info block.
     * The version string is taken from [OpenApiSerializer.OPENAPI_VERSION] so that the
     * constant and the serialised output can never diverge.
     */
    @Test
    fun `generated openapi json contains openapi version header`() {
        val json = OpenApiSerializer.toJson(sampleMetadata)
        assertTrue(
            json.contains("\"${OpenApiSerializer.OPENAPI_VERSION}\""),
            "Expected openapi version ${OpenApiSerializer.OPENAPI_VERSION}"
        )
        assertTrue(json.contains("\"info\""), "Expected info block")
    }

    /**
     * Verifies that [OpenApiSerializer.OPENAPI_VERSION] holds the expected OpenAPI spec version
     * that this serialiser targets.
     */
    @Test
    fun `OPENAPI_VERSION constant equals expected spec version`() {
        assertEquals("3.0.3", OpenApiSerializer.OPENAPI_VERSION)
    }

    /**
     * Verifies that the generated JSON contains the request body schema reference for POST.
     */
    @Test
    fun `generated openapi json contains request body schema ref for post endpoint`() {
        val json = OpenApiSerializer.toJson(sampleMetadata)
        assertTrue(json.contains("requestBody"), "Expected requestBody in openapi.json")
        assertTrue(json.contains("SampleDTO"), "Expected SampleDTO schema reference")
    }

    /**
     * Verifies that the generated JSON includes the components/schemas section with
     * the correct property types.
     */
    @Test
    fun `generated openapi json contains components schemas`() {
        val json = OpenApiSerializer.toJson(sampleMetadata)
        assertTrue(json.contains("\"schemas\""), "Expected schemas section")
        assertTrue(json.contains("\"SampleDTO\""), "Expected SampleDTO schema")
        assertTrue(json.contains("\"name\""), "Expected name property")
        assertTrue(json.contains("\"string\""), "Expected string type")
    }

    /**
     * Verifies that an empty metadata model still produces a valid structural JSON skeleton.
     */
    @Test
    fun `empty metadata produces valid openapi json skeleton`() {
        val empty = ApiMetadata(endpoints = emptyList(), schemas = emptyMap())
        val json = OpenApiSerializer.toJson(empty)
        assertTrue(json.contains("openapi"), "Expected openapi key in skeleton")
        assertTrue(json.contains("paths"), "Expected paths key in skeleton")
        assertFalse(json.contains("SampleDTO"), "Expected no schemas in empty spec")
    }
}
