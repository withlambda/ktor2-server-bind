package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.AuthenticationConfiguration
import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.exception.ExceptionHandlerBuilder
import com.withlambda.ktor2.server.bind.exception.exception
import com.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import com.withlambda.ktor2.server.bind.request.PathParameters
import com.withlambda.ktor2.server.bind.request.RequestAttributes
import com.withlambda.ktor2.server.bind.request.RequestCookies
import com.withlambda.ktor2.server.bind.request.RequestData
import com.withlambda.ktor2.server.bind.request.RequestHeaders
import com.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import com.withlambda.ktor2.server.bind.request.mapping.GetRequestMapping
import com.withlambda.ktor2.server.bind.response.ResponseCookies
import com.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import com.withlambda.ktor2.server.bind.response.ResponseHeaders
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.util.pipeline.PipelineContext
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Integration tests that verify controller-scoped exception handling takes precedence over global
 * [StatusPages] handlers, and that exceptions not handled by the controller fall through to the
 * global handler.
 */
class ScopedExceptionHandlingIntegrationTest {

    /** Exception to be handled specifically by the controller. */
    class EntityNotFoundException(val id: Int) : Exception("Entity $id not found")

    /** Exception that the controller does NOT handle, so the global handler should catch it. */
    class UnhandledByControllerException : Exception("not handled by controller")

    private val disabledAuth = object : AuthenticationConfiguration {
        override val enable = false
        override val providers: Array<String?> = emptyArray()
        override val optional = false
    }

    private val disabledRoleAuth = RoleBasedAuthorizationConfigurationImpl(
        enabled = false,
        requiredRolesAllOf = emptySet()
    )

    class SimpleRequestData(
        override val pathParameter: PathParameters,
        override val optionalQueryParameter: OptionalQueryParameters,
        override val requiredQueryParameter: RequiredQueryParameters,
        override val headers: RequestHeaders,
        override val cookies: RequestCookies,
        override val attributes: RequestAttributes
    ) : RequestData

    class SimpleResponseData(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: String?,
        override val responseMessage: String?,
        override val responseHeaders: ResponseHeaders?,
        override val responseCookies: ResponseCookies?
    ) : ResponseDataWithBody<String>

    /**
     * A GET route processor that always throws the exception produced by [throwable].
     * @param path The route path to register.
     * @param throwable Factory producing the exception to throw on each request.
     */
    inner class ThrowingRouteProcessor(
        private val path: String,
        private val throwable: () -> Throwable
    ) : GetRouteProcessor<SimpleRequestData, String, SimpleResponseData> {

        override val requestMapping = GetRequestMapping(
            name = "test-scoped",
            path = path,
            produces = ContentType.Text.Plain
        )
        override val roleBasedAuthorizationConfiguration = disabledRoleAuth

        override suspend fun ApplicationCall.prepareRequestData(): SimpleRequestData =
            SimpleRequestData(
                pathParameter = object : PathParameters {},
                optionalQueryParameter = object : OptionalQueryParameters {},
                requiredQueryParameter = object : RequiredQueryParameters {},
                headers = object : RequestHeaders {},
                cookies = object : RequestCookies {},
                attributes = object : RequestAttributes {}
            )

        override val processRequest:
            suspend PipelineContext<Unit, ApplicationCall>.(SimpleRequestData) -> SimpleResponseData =
            { throw throwable() }

        override suspend fun ApplicationCall.respondWithBody(httpStatusCode: HttpStatusCode, responseBody: String) {
            respond(httpStatusCode, responseBody)
        }
    }

    @Test
    fun `controller exception handler takes precedence over global StatusPages handler`() = testApplication {
        val processor = ThrowingRouteProcessor("/test-scoped-entity") { EntityNotFoundException(42) }
        val controller = object : Controller {
            override val endpoints: List<RouteProcessorBase<*, *, *>> = listOf(processor)
            override val authenticationConfiguration = disabledAuth
            override val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit) = {
                exception<EntityNotFoundException> { call, e ->
                    call.respondText(status = HttpStatusCode.NotFound, text = e.message ?: "not found")
                }
            }
        }

        application {
            install(StatusPages) {
                exception<EntityNotFoundException> { call, e ->
                    call.respondText(
                        status = HttpStatusCode.InternalServerError,
                        text = "global: ${e.message}"
                    )
                }
            }
            routing { install(controller) }
        }

        val response = client.get("/test-scoped-entity")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("Entity 42 not found", response.bodyAsText())
    }

    @Test
    fun `exception not handled by controller propagates to global StatusPages handler`() = testApplication {
        val processor = ThrowingRouteProcessor("/test-unhandled") { UnhandledByControllerException() }
        val controller = object : Controller {
            override val endpoints: List<RouteProcessorBase<*, *, *>> = listOf(processor)
            override val authenticationConfiguration = disabledAuth
            override val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit) = {
                // Only handles EntityNotFoundException — UnhandledByControllerException falls through
                exception<EntityNotFoundException> { call, e ->
                    call.respondText(status = HttpStatusCode.NotFound, text = e.message ?: "not found")
                }
            }
        }

        application {
            install(StatusPages) {
                exception<UnhandledByControllerException> { call, e ->
                    call.respondText(
                        status = HttpStatusCode.InternalServerError,
                        text = "global: ${e.message}"
                    )
                }
            }
            routing { install(controller) }
        }

        val response = client.get("/test-unhandled")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals("global: not handled by controller", response.bodyAsText())
    }

    @Test
    fun `controller without exceptionHandler propagates all exceptions to global StatusPages handler`() =
        testApplication {
            val processor = ThrowingRouteProcessor("/test-no-handler") { EntityNotFoundException(99) }
            val controller = object : Controller {
                override val endpoints: List<RouteProcessorBase<*, *, *>> = listOf(processor)
                override val authenticationConfiguration = disabledAuth
                // exceptionHandler defaults to null — all exceptions go to global StatusPages
            }

            application {
                install(StatusPages) {
                    exception<EntityNotFoundException> { call, e ->
                        call.respondText(
                            status = HttpStatusCode.InternalServerError,
                            text = "global: ${e.message}"
                        )
                    }
                }
                routing { install(controller) }
            }

            val response = client.get("/test-no-handler")
            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals("global: Entity 99 not found", response.bodyAsText())
        }
}
