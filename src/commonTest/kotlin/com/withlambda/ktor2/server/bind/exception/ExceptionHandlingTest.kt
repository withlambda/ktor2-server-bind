package com.withlambda.ktor2.server.bind.exception

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ExceptionHandlingTest {

    @Test
    fun `predefinedBadRequestExceptionsHandler handles MissingHeaderException with 400`() {
        testApplication {
            application {
                install(StatusPages) {
                    with(ExceptionHandling) { predefinedBadRequestExceptionsHandler() }
                }
                routing {
                    get("/test") { throw MissingHeaderException("X-Header") }
                }
            }
            val response = client.get("/test")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `predefinedBadRequestExceptionsHandler handles MissingRequestParameterException with 400`() {
        testApplication {
            application {
                install(StatusPages) {
                    with(ExceptionHandling) { predefinedBadRequestExceptionsHandler() }
                }
                routing {
                    get("/test") { throw MissingRequestParameterException("param1") }
                }
            }
            val response = client.get("/test")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `predefinedBadRequestExceptionsHandler handles ParameterConversionException with 400`() {
        testApplication {
            application {
                install(StatusPages) {
                    with(ExceptionHandling) { predefinedBadRequestExceptionsHandler() }
                }
                routing {
                    get("/test") { throw ParameterConversionException("param1", "Int") }
                }
            }
            val response = client.get("/test")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `predefinedBadRequestExceptionsHandler accepts a custom handler builder`() {
        val customBuilder = ExceptionHandlerBuilderImpl(
            httpStatusCode = HttpStatusCode.UnprocessableEntity,
            getCallResponseContent = { _, logMessage -> logMessage }
        )
        testApplication {
            application {
                install(StatusPages) {
                    with(ExceptionHandling) {
                        predefinedBadRequestExceptionsHandler(customBuilder)
                    }
                }
                routing {
                    get("/test") { throw MissingHeaderException("X-Custom") }
                }
            }
            val response = client.get("/test")
            assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        }
    }
}
