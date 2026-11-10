package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.CreateHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldDTO
import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationIntegrationTest {
    @Test
    fun testValidationFailsWith400AfterBridgeAndHandler() = testApplication {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
            install(io.ktor.server.plugins.statuspages.StatusPages) {
                with(com.withlambda.ktor2.server.bind.exception.ExceptionHandling) {
                    predefinedBadRequestExceptionsHandler()
                }
            }
            routing {
                val processor = CreateHelloWorld.RouteProcessorImpl(
                    roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(enabled = false),
                    validation = Validation {
                        HelloWorldDTO::name {
                            minLength(5)
                        }
                    }
                ) { _ ->
                    CreateHelloWorld.ResponseDataImpl(
                        httpStatusCode = HttpStatusCode.OK,
                        responseBody = null,
                        responseMessage = "Success",
                        responseHeaders = null,
                        responseCookies = null
                    )
                }

                with(processor) { install() }
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.post("/helloworld/1/2?requiredQueryParam1=10&requiredQueryParam2=test") {
            contentType(ContentType.Application.Json)
            header("header2", "test-header")
            setBody(HelloWorldDTO(name = "ab")) // Invalid name
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        // The handler maps reasons to a map with the "errors" key
        assertTrue(body.contains(".name: must have at least 5 characters"))
    }
}
