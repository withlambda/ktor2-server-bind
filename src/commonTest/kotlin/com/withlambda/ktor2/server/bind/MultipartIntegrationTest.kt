package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.request.FileItemDTO
import com.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import com.withlambda.ktor2.server.bind.request.PathParameters
import com.withlambda.ktor2.server.bind.request.RequestFormData
import com.withlambda.ktor2.server.bind.request.RequestHeaders
import com.withlambda.ktor2.server.bind.request.RequestWithBodyData
import com.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import com.withlambda.ktor2.server.bind.request.mapping.PostRequestMapping
import com.withlambda.ktor2.server.bind.response.ResponseCookies
import com.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import com.withlambda.ktor2.server.bind.response.ResponseHeaders
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.core.readBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipartIntegrationTest {

    class MyRequestData(
        override val pathParameter: PathParameters,
        override val optionalQueryParameter: OptionalQueryParameters,
        override val requiredQueryParameter: RequiredQueryParameters,
        override val headers: RequestHeaders,
        override val cookies: com.withlambda.ktor2.server.bind.request.RequestCookies,
        val description: String,
        val file: FileItemDTO,
        override val requestBody: Unit = Unit
    ) : RequestWithBodyData<Unit>, RequestFormData

    class MyResponseData(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: String?,
        override val responseMessage: String?,
        override val responseHeaders: ResponseHeaders?,
        override val responseCookies: ResponseCookies?
    ) : ResponseDataWithBody<String>

    class MyRouteProcessor(
        override val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(MyRequestData) -> MyResponseData
    ) : PostRouteProcessor<Unit, MyRequestData, String, MyResponseData> {
        override val requestMapping = PostRequestMapping(
            name = "upload",
            path = "/upload",
            consumes = ContentType.MultiPart.FormData,
            produces = ContentType.Text.Plain
        )
        override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
            enabled = false,
            requiredRolesAllOf = setOf("dummy")
        )

        override suspend fun ApplicationCall.prepareRequestData(): MyRequestData {
            val parts = receiveMultipartData()

            return MyRequestData(
                pathParameter = object : PathParameters {},
                optionalQueryParameter = object : OptionalQueryParameters {},
                requiredQueryParameter = object : RequiredQueryParameters {},
                headers = object : RequestHeaders {},
                cookies = object : com.withlambda.ktor2.server.bind.request.RequestCookies {},
                description = parts["description"] as String,
                file = parts["file"] as FileItemDTO
            )
        }

        override suspend fun ApplicationCall.respondWithBody(httpStatusCode: HttpStatusCode, responseBody: String) {
            respond(httpStatusCode, responseBody)
        }
    }

    @Test
    fun testMultipartUpload() = testApplication {
        application {
            routing {
                val processor = MyRouteProcessor { requestData ->
                    val fileName = requestData.file.fileName
                    val fileSize = requestData.file.openStream().readBytes().size
                    MyResponseData(
                        httpStatusCode = HttpStatusCode.OK,
                        responseBody = "File: $fileName, Size: $fileSize, Desc: ${requestData.description}",
                        responseMessage = null,
                        responseHeaders = null,
                        responseCookies = null
                    )
                }
                with(processor) { install() }
            }
        }

        val response = client.post("/upload") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("description", "Ktor logo")
                        append(
                            "file",
                            "ktor_logo.png".encodeToByteArray(),
                            io.ktor.http.Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(HttpHeaders.ContentDisposition, "filename=\"ktor_logo.png\"")
                            }
                        )
                    }
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("File: ktor_logo.png, Size: 13, Desc: Ktor logo", response.bodyAsText())
    }
}
