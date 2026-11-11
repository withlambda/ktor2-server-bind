package io.withlambda.ktor2.server.bind

import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.util.StringValues
import io.ktor.util.pipeline.PipelineContext
import io.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import io.withlambda.ktor2.server.bind.request.RequestCookies
import io.withlambda.ktor2.server.bind.request.RequestData
import io.withlambda.ktor2.server.bind.response.ResponseCookies
import io.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import io.withlambda.ktor2.server.bind.util.RequiredCookiesHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CookieIntegrationTest {

    class MyRequestCookies(stringValues: StringValues) : RequestCookies {
        val sessionId: String by RequiredCookiesHandler(stringValues)
    }

    class MyResponseCookies(val lastVisit: String) : ResponseCookies {
        override val mapOfCookies: Map<String, Cookie> = mapOf(
            "LastVisit" to Cookie("LastVisit", lastVisit, path = "/", httpOnly = true)
        )
    }

    class MyRequestData(
        override val pathParameter: io.withlambda.ktor2.server.bind.request.PathParameters,
        override val optionalQueryParameter: io.withlambda.ktor2.server.bind.request.OptionalQueryParameters,
        override val requiredQueryParameter: io.withlambda.ktor2.server.bind.request.RequiredQueryParameters,
        override val headers: io.withlambda.ktor2.server.bind.request.RequestHeaders,
        override val cookies: MyRequestCookies
    ) : RequestData

    class MyResponseData(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: String?,
        override val responseMessage: String?,
        override val responseHeaders: io.withlambda.ktor2.server.bind.response.ResponseHeaders?,
        override val responseCookies: ResponseCookies?
    ) : ResponseDataWithBody<String>

    class MyRouteProcessor(
        override val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(MyRequestData) -> MyResponseData
    ) : GetRouteProcessor<MyRequestData, String, MyResponseData> {
        override val requestMapping = io.withlambda.ktor2.server.bind.request.mapping.GetRequestMapping(
            name = "testCookie",
            path = "/test-cookie",
            produces = ContentType.Text.Plain
        )
        override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
            enabled = false,
            requiredRolesAllOf = setOf("dummy")
        )

        override suspend fun ApplicationCall.prepareRequestData(): MyRequestData {
            val cookieValues = StringValues.build {
                request.cookies.rawCookies.forEach { (k, _) ->
                    append(k, request.cookies[k]!!)
                }
            }
            return MyRequestData(
                pathParameter = object : io.withlambda.ktor2.server.bind.request.PathParameters {},
                optionalQueryParameter = object : io.withlambda.ktor2.server.bind.request.OptionalQueryParameters {},
                requiredQueryParameter = object : io.withlambda.ktor2.server.bind.request.RequiredQueryParameters {},
                headers = object : io.withlambda.ktor2.server.bind.request.RequestHeaders {},
                cookies = MyRequestCookies(cookieValues)
            )
        }

        override suspend fun ApplicationCall.respondWithBody(httpStatusCode: HttpStatusCode, responseBody: String) {
            respond(httpStatusCode, responseBody)
        }
    }

    @Test
    fun `test cookie with special characters and multiple cookies`() = testApplication {
        application {
            routing {
                val processor = MyRouteProcessor { requestData ->
                    val sessionId = requestData.cookies.sessionId
                    MyResponseData(
                        httpStatusCode = HttpStatusCode.OK,
                        responseBody = "Session: $sessionId",
                        responseMessage = null,
                        responseHeaders = null,
                        responseCookies = object : ResponseCookies {
                            override val mapOfCookies: Map<String, Cookie> = mapOf(
                                "Complex-Cookie" to Cookie("Complex-Cookie", "val with spaces!", path = "/path"),
                                "Another" to Cookie("Another", "val2")
                            )
                        }
                    )
                }
                with(processor) { install() }
            }
        }

        val response = client.get("/test-cookie") {
            cookie("sessionId", "val with spaces!")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Session: val with spaces!", response.bodyAsText())
        val setCookies = response.headers.getAll("Set-Cookie")
        assertTrue(setCookies != null)
        assertTrue(setCookies.any { it.contains("Complex-Cookie=") && it.contains("val+with+spaces%21") })
        assertTrue(setCookies.any { it.contains("Another=val2") })
    }
}
