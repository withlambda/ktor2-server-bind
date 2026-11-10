package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.fixtures.EmptyRequestCookies
import com.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import com.withlambda.ktor2.server.bind.request.PathParameters
import com.withlambda.ktor2.server.bind.request.RequestCookies
import com.withlambda.ktor2.server.bind.request.RequestData
import com.withlambda.ktor2.server.bind.request.RequestHeaders
import com.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import com.withlambda.ktor2.server.bind.request.mapping.GetRequestMapping
import com.withlambda.ktor2.server.bind.response.ResponseCookies
import com.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import com.withlambda.ktor2.server.bind.response.ResponseDataWithBytes
import com.withlambda.ktor2.server.bind.response.ResponseDataWithFile
import com.withlambda.ktor2.server.bind.response.ResponseDataWithOutputStream
import com.withlambda.ktor2.server.bind.response.ResponseDataWithSSE
import com.withlambda.ktor2.server.bind.response.ResponseHeaders
import com.withlambda.ktor2.server.bind.response.ServerSentEvent
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StreamingResponseIntegrationTest {

    private val noAuth = RoleBasedAuthorizationConfigurationImpl(
        enabled = false,
        requiredRolesAllOf = setOf("dummy")
    )

    // Minimal marker implementations for a no-param endpoint
    private object NoPathParameters : PathParameters
    private object NoOptionalQueryParameters : OptionalQueryParameters
    private object NoRequiredQueryParameters : RequiredQueryParameters
    private object NoRequestHeaders : RequestHeaders

    private object NoRequestData : RequestData {
        override val pathParameter: PathParameters = NoPathParameters
        override val optionalQueryParameter: OptionalQueryParameters = NoOptionalQueryParameters
        override val requiredQueryParameter: RequiredQueryParameters = NoRequiredQueryParameters
        override val headers: RequestHeaders = NoRequestHeaders
        override val cookies: RequestCookies = EmptyRequestCookies
    }

    // Response data for bytes — also implements ResponseDataWithBody<Unit> so it can be used with GetRouteProcessor;
    // the specialized ResponseDataWithBytes check fires before the body check in customizedResponse.
    private class BytesResponseData(
        override val bytes: ByteArray,
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
        override val responseMessage: String? = null,
        override val responseHeaders: ResponseHeaders? = null,
        override val responseCookies: ResponseCookies? = null,
        override val responseBody: Unit? = null
    ) : ResponseDataWithBody<Unit>, ResponseDataWithBytes

    // Response data for file
    private class FileResponseData(
        override val file: java.io.File,
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
        override val responseMessage: String? = null,
        override val responseHeaders: ResponseHeaders? = null,
        override val responseCookies: ResponseCookies? = null,
        override val responseBody: Unit? = null
    ) : ResponseDataWithBody<Unit>, ResponseDataWithFile

    // Response data for output stream
    private class OutputStreamResponseData(
        override val producer: suspend (java.io.OutputStream) -> Unit,
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
        override val responseMessage: String? = null,
        override val responseHeaders: ResponseHeaders? = null,
        override val responseCookies: ResponseCookies? = null,
        override val responseBody: Unit? = null
    ) : ResponseDataWithBody<Unit>, ResponseDataWithOutputStream

    // Response data for SSE
    private class SSEResponseData(
        override val events: kotlinx.coroutines.flow.Flow<ServerSentEvent>,
        override val httpStatusCode: HttpStatusCode = HttpStatusCode.OK,
        override val responseMessage: String? = null,
        override val responseHeaders: ResponseHeaders? = null,
        override val responseCookies: ResponseCookies? = null,
        override val responseBody: Unit? = null
    ) : ResponseDataWithBody<Unit>, ResponseDataWithSSE

    // Shared respondWithBody fallback — never reached when specialized check fires first
    private suspend fun ApplicationCall.noBodyResponse(httpStatusCode: HttpStatusCode) = respond(httpStatusCode)

    @Test
    fun `bytes endpoint responds with correct byte content`() {
        val expected = "Hello, Bytes!"
        testApplication {
            application {
                routing {
                    val processor = object : GetRouteProcessor<NoRequestData, Unit, BytesResponseData> {
                        override val requestMapping = GetRequestMapping(
                            name = "streaming-bytes",
                            path = "/streaming/bytes",
                            produces = null
                        )
                        override val roleBasedAuthorizationConfiguration = noAuth
                        override val processRequest:
                            suspend PipelineContext<Unit, ApplicationCall>.(NoRequestData) -> BytesResponseData = {
                                BytesResponseData(bytes = expected.toByteArray())
                            }

                        override suspend fun ApplicationCall.prepareRequestData() = NoRequestData
                        override suspend fun ApplicationCall.respondWithBody(
                            httpStatusCode: HttpStatusCode,
                            responseBody: Unit
                        ) = noBodyResponse(httpStatusCode)
                    }
                    with(processor) { install() }
                }
            }
            val response = client.get("/streaming/bytes")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(expected, response.bodyAsText())
        }
    }

    @Test
    fun `file endpoint responds with correct file content`() {
        val content = "Hello, File!"
        val file = java.io.File.createTempFile("ktor-test", ".txt").also { it.writeText(content) }
        try {
            testApplication {
                application {
                    routing {
                        val processor = object : GetRouteProcessor<NoRequestData, Unit, FileResponseData> {
                            override val requestMapping = GetRequestMapping(
                                name = "streaming-file",
                                path = "/streaming/file",
                                produces = null
                            )
                            override val roleBasedAuthorizationConfiguration = noAuth
                            override val processRequest:
                                suspend PipelineContext<Unit, ApplicationCall>.(NoRequestData) -> FileResponseData = {
                                    FileResponseData(file = file)
                                }

                            override suspend fun ApplicationCall.prepareRequestData() = NoRequestData
                            override suspend fun ApplicationCall.respondWithBody(
                                httpStatusCode: HttpStatusCode,
                                responseBody: Unit
                            ) = noBodyResponse(httpStatusCode)
                        }
                        with(processor) { install() }
                    }
                }
                val response = client.get("/streaming/file")
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals(content, response.bodyAsText())
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun `outputstream endpoint responds with correct streamed content`() {
        val expected = "Hello, OutputStream!"
        testApplication {
            application {
                routing {
                    val processor = object : GetRouteProcessor<NoRequestData, Unit, OutputStreamResponseData> {
                        override val requestMapping = GetRequestMapping(
                            name = "streaming-outputstream",
                            path = "/streaming/outputstream",
                            produces = null
                        )
                        override val roleBasedAuthorizationConfiguration = noAuth
                        override val processRequest:
                            suspend PipelineContext<Unit, ApplicationCall>.(NoRequestData) -> OutputStreamResponseData =
                            {
                                OutputStreamResponseData(
                                    producer = { outputStream -> outputStream.write(expected.toByteArray()) }
                                )
                            }

                        override suspend fun ApplicationCall.prepareRequestData() = NoRequestData
                        override suspend fun ApplicationCall.respondWithBody(
                            httpStatusCode: HttpStatusCode,
                            responseBody: Unit
                        ) = noBodyResponse(httpStatusCode)
                    }
                    with(processor) { install() }
                }
            }
            val response = client.get("/streaming/outputstream")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(expected, response.bodyAsText())
        }
    }

    @Test
    fun `SSE endpoint responds with correct content-type and event format`() {
        testApplication {
            application {
                routing {
                    val processor = object : GetRouteProcessor<NoRequestData, Unit, SSEResponseData> {
                        override val requestMapping = GetRequestMapping(
                            name = "streaming-sse",
                            path = "/streaming/sse",
                            produces = null
                        )
                        override val roleBasedAuthorizationConfiguration = noAuth
                        override val processRequest:
                            suspend PipelineContext<Unit, ApplicationCall>.(NoRequestData) -> SSEResponseData = {
                                SSEResponseData(
                                    events = flowOf(ServerSentEvent("event1"), ServerSentEvent("event2"))
                                )
                            }

                        override suspend fun ApplicationCall.prepareRequestData() = NoRequestData
                        override suspend fun ApplicationCall.respondWithBody(
                            httpStatusCode: HttpStatusCode,
                            responseBody: Unit
                        ) = noBodyResponse(httpStatusCode)
                    }
                    with(processor) { install() }
                }
            }
            val response = client.get("/streaming/sse")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(
                response.headers["Content-Type"]?.startsWith(ContentType.Text.EventStream.toString()) == true
            )
            val body = response.bodyAsText()
            assertTrue(body.contains("data: event1\n\n"))
            assertTrue(body.contains("data: event2\n\n"))
        }
    }
}
