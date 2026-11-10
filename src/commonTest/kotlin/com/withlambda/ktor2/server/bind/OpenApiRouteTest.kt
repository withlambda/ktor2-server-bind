package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.fixtures.testModule
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests for the `/openapi.json` route provided by [installOpenApiRoute].
 *
 * The Red phase verifies that the route returns 404 when [installOpenApiRoute] has not yet been
 * called. The Green phase verifies that the route returns 200 with `application/json` content
 * once [installOpenApiRoute] is applied to the application.
 */
class OpenApiRouteTest {

    /**
     * Asserts that `GET /openapi.json` returns HTTP 404 when [installOpenApiRoute] is not
     * installed — confirming the route does not exist by default.
     */
    @Test
    fun `GET openapi json returns 404 when route is not installed`() = testApplication {
        application { testModule() }
        val response = client.get("/openapi.json")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    /**
     * Asserts that `GET /openapi.json` returns HTTP 200 with `application/json` content type
     * once [installOpenApiRoute] is applied to the [io.ktor.server.application.Application].
     */
    @Test
    fun `GET openapi json returns 200 with json content type when route is installed`() =
        testApplication {
            application {
                testModule()
                installOpenApiRoute()
            }
            val response = client.get("/openapi.json")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(
                response.headers["Content-Type"]
                    ?.startsWith(ContentType.Application.Json.toString()),
                true,
                "Expected Content-Type application/json"
            )
        }
}
