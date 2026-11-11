package io.withlambda.ktor2.server.bind

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.util.pipeline.PipelineContext
import io.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import io.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import io.withlambda.ktor2.server.bind.request.PathParameters
import io.withlambda.ktor2.server.bind.request.RequestAttributes
import io.withlambda.ktor2.server.bind.request.RequestCookies
import io.withlambda.ktor2.server.bind.request.RequestData
import io.withlambda.ktor2.server.bind.request.RequestHeaders
import io.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import io.withlambda.ktor2.server.bind.request.mapping.GetRequestMapping
import io.withlambda.ktor2.server.bind.response.ResponseCookies
import io.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import io.withlambda.ktor2.server.bind.response.ResponseHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AttributeIntegrationTest {

    /**
     * Request attributes that expose the Ktor-generated call ID.
     * The [callId] is populated from [ApplicationCall.callId] in [prepareRequestData],
     * demonstrating out-of-the-box support for well-known Ktor attributes.
     */
    class MyRequestAttributes(val callId: String?) : RequestAttributes

    class MyRequestData(
        override val pathParameter: PathParameters,
        override val optionalQueryParameter: OptionalQueryParameters,
        override val requiredQueryParameter: RequiredQueryParameters,
        override val headers: RequestHeaders,
        override val cookies: RequestCookies,
        override val attributes: MyRequestAttributes
    ) : RequestData

    class MyResponseData(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: String?,
        override val responseMessage: String?,
        override val responseHeaders: ResponseHeaders?,
        override val responseCookies: ResponseCookies?
    ) : ResponseDataWithBody<String>

    class MyRouteProcessor(
        override val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(MyRequestData) -> MyResponseData
    ) : GetRouteProcessor<MyRequestData, String, MyResponseData> {
        override val requestMapping = GetRequestMapping(
            name = "testAttributes",
            path = "/test-attributes",
            produces = ContentType.Text.Plain
        )
        override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
            enabled = false,
            requiredRolesAllOf = setOf("dummy")
        )

        /**
         * Populates [MyRequestData.attributes] with the Ktor-generated call ID from [ApplicationCall.callId],
         * wiring the well-known Ktor attribute into the typed [RequestAttributes] abstraction.
         */
        override suspend fun ApplicationCall.prepareRequestData(): MyRequestData =
            MyRequestData(
                pathParameter = object : PathParameters {},
                optionalQueryParameter = object : OptionalQueryParameters {},
                requiredQueryParameter = object : RequiredQueryParameters {},
                headers = object : RequestHeaders {},
                cookies = object : RequestCookies {},
                // Wire the Ktor CallId plugin's value into the typed attributes
                attributes = MyRequestAttributes(callId = this.callId)
            )

        override suspend fun ApplicationCall.respondWithBody(httpStatusCode: HttpStatusCode, responseBody: String) {
            respond(httpStatusCode, responseBody)
        }
    }

    @Test
    fun `controller reads CallId from RequestData attributes`() = testApplication {
        application {
            install(CallId) {
                generate { "test-call-id-123" }
            }
            routing {
                val processor = MyRouteProcessor { requestData ->
                    val callId = requestData.attributes.callId
                    MyResponseData(
                        httpStatusCode = HttpStatusCode.OK,
                        responseBody = callId,
                        responseMessage = null,
                        responseHeaders = null,
                        responseCookies = null
                    )
                }
                with(processor) { install() }
            }
        }

        val response = client.get("/test-attributes")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertNotNull(body)
        assertEquals("test-call-id-123", body)
    }

    @Test
    fun `callId is null in RequestData attributes when CallId plugin is not installed`() = testApplication {
        application {
            routing {
                val processor = MyRouteProcessor { requestData ->
                    MyResponseData(
                        httpStatusCode = HttpStatusCode.OK,
                        responseBody = requestData.attributes.callId ?: "no-call-id",
                        responseMessage = null,
                        responseHeaders = null,
                        responseCookies = null
                    )
                }
                with(processor) { install() }
            }
        }

        val response = client.get("/test-attributes")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("no-call-id", response.bodyAsText())
    }
}
