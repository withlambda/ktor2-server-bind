package com.withlambda.ktor2.server.bind.exception

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExceptionHandlerBuilderImplTest {

    private fun baseBuilder() = ExceptionHandlerBuilderImpl(
        httpStatusCode = HttpStatusCode.BadRequest,
        getCallResponseContent = { _, logMessage -> logMessage }
    )

    @Test
    fun `withHttpStatusCode returns new instance with updated status code`() {
        val original = baseBuilder()
        val updated = original.withHttpStatusCode(HttpStatusCode.InternalServerError)
        assertEquals(HttpStatusCode.InternalServerError, updated.httpStatusCode)
        assertEquals(HttpStatusCode.BadRequest, original.httpStatusCode)
    }

    @Test
    fun `withLog returns new instance with updated log function`() {
        val original = baseBuilder()
        assertNull(original.log)
        val log: (String) -> Unit = {}
        val updated = original.withLog(log)
        assertNotNull(updated.log)
        assertNull(original.log)
    }

    @Test
    fun `withLog returns new instance with null log when null is passed`() {
        val log: (String) -> Unit = {}
        val original = baseBuilder().withLog(log)
        assertNotNull(original.log)
        val updated = original.withLog(null)
        assertNull(updated.log)
    }

    @Test
    fun `withGetLogMessage returns new instance with updated getLogMessage`() {
        val original = baseBuilder()
        val customGetLogMessage: (Throwable) -> String = { "custom: ${it.message}" }
        val updated = original.withGetLogMessage(customGetLogMessage)
        val exception = IllegalStateException("test")
        assertEquals("custom: test", updated.getLogMessage(exception))
    }

    @Test
    fun `withGetCallResponseContent returns new instance with updated getCallResponseContent`() {
        val original = baseBuilder()
        val customContent: (Throwable, String) -> String = { _, _ -> "custom response" }
        val updated = original.withGetCallResponseContent(customContent)
        assertEquals("custom response", updated.getCallResponseContent(IllegalStateException("x"), "log"))
    }

    @Test
    fun `defaultGetResponseMessage returns exception message when present`() {
        val exception = IllegalStateException("something went wrong")
        val result = ExceptionHandlerBuilder.defaultGetResponseMessage(exception)
        assertEquals("something went wrong", result)
    }

    @Test
    fun `defaultGetResponseMessage returns class name occurred when message is null`() {
        val exception = object : IllegalStateException() {}
        val result = ExceptionHandlerBuilder.defaultGetResponseMessage(exception)
        assertEquals("occurred", result.substringAfterLast(" "))
    }

    @Test
    fun `build produces handler that responds with configured status code and message`() {
        testApplication {
            application {
                installTestStatusPages(withLog = false)
                routing {
                    get("/test") { throw IllegalStateException("handler error") }
                }
            }
            val response = client.get("/test")
            assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
            assertEquals("handler error", response.bodyAsText())
        }
    }

    @Test
    fun `build produces handler that invokes log when log is set`() {
        val loggedMessages = mutableListOf<String>()
        testApplication {
            application {
                installTestStatusPages(withLog = true, logSink = loggedMessages)
                routing {
                    get("/test") { throw IllegalStateException("logged error") }
                }
            }
            client.get("/test")
            assertEquals(1, loggedMessages.size)
            assertEquals("logged error", loggedMessages.first())
        }
    }

    private fun Application.installTestStatusPages(
        withLog: Boolean,
        logSink: MutableList<String> = mutableListOf()
    ) {
        val builder = ExceptionHandlerBuilderImpl(
            httpStatusCode = HttpStatusCode.UnprocessableEntity,
            log = if (withLog) ({ msg -> logSink.add(msg) }) else null,
            getCallResponseContent = { _, logMessage -> logMessage }
        )
        install(StatusPages) {
            exception<IllegalStateException>(builder.build())
        }
    }
}
