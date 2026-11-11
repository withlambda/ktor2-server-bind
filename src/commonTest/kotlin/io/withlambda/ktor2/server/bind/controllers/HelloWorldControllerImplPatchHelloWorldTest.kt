package io.withlambda.ktor2.server.bind.controllers

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsBadRequest
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsForbidden
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsNotAcceptable
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsUnauthorized
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsUnsupportedMediaType
import io.withlambda.ktor2.server.bind.fixtures.TestTemplates.is200WithApplicationJsonContent
import io.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldDTO
import io.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldResponseDTO
import kotlin.test.Test
import kotlin.test.assertEquals

class HelloWorldControllerImplPatchHelloWorldTest {

    private val patchHelloWorldAuthorizationRoles = setOf("patchHelloWorld1", "patchHelloWorld2")

    private val expectedHelloWorldResponseDTO = HelloWorldResponseDTO(
        name = "Test",
        count = 1,
        pathParam1 = 1,
        pathParam2 = "value",
        optionalQueryParam1 = 1,
        optionalQueryParam2 = "2",
        requiredQueryParam1 = 3,
        requiredQueryParam2 = "4",
        requestHeader1 = 1,
        requestHeader2 = "2"
    )

    private inline fun patchHelloWorldRequest(
        path: String = "/helloworld/1/value",
        input: HelloWorldDTO = HelloWorldDTO("Test"),
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = ContentType.Application.Json,
        contentType: ContentType? = ContentType.Application.Json,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = patchHelloWorldAuthorizationRoles,
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend HelloWorldResponseDTO.() -> Unit
    ) {
        TestTemplates.patchRequest<HelloWorldDTO, HelloWorldResponseDTO>(
            path = path,
            input = input,
            queryParameters = queryParameters,
            authorizationRoles = authorizationRoles,
            accept = accept,
            contentType = contentType,
            additionalHeaders = additionalHeaders,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions,
        )
    }

    @Test
    fun `test patch endpoint 'patchHelloWorld' which response with status 200 and response body when all is ok`() {
        patchHelloWorldRequest(
            queryParameters = listOf(
                "optionalQueryParam1" to "1",
                "optionalQueryParam2" to "2",
                "requiredQueryParam1" to "3",
                "requiredQueryParam2" to "4",
            ),
            additionalHeaders = listOf(
                "header1" to "1",
                "header2" to "2"
            ),
            responseAssertions = {
                assert(
                    headers.contains("responseHeader1", "1")
                )
                assert(
                    headers.contains("responseHeader2", "2")
                )

                is200WithApplicationJsonContent()
            }
        ) {
            assertEquals(
                expectedHelloWorldResponseDTO,
                this
            )
        }
    }

    @Test
    fun `patchHelloWorld returns 401 when Authorization header is missing`() {
        patchHelloWorldRequest(
            authorizationRoles = emptySet(),
            parseResponseBody = false,
            responseAssertions = {
                httpStatusIsUnauthorized()
            }
        ) {}
    }

    @Test
    fun `patchHelloWorld returns 403 when Authorization role is wrong`() {
        patchHelloWorldRequest(
            authorizationRoles = setOf("wrongRole"),
            parseResponseBody = false,
            responseAssertions = {
                httpStatusIsForbidden()
            }
        ) {}
    }

    @Test
    fun `patchHelloWorld returns 400 when required query parameter is missing`() {
        patchHelloWorldRequest(
            parseResponseBody = false,
            accept = null,
            responseAssertions = {
                assertEquals("Request parameter requiredQueryParam1 is missing", this.body<String>())
                httpStatusIsBadRequest()
            }
        ) {}
    }

    @Test
    fun `patchHelloWorld returns 400 when required request header is missing`() {
        patchHelloWorldRequest(
            queryParameters = listOf(
                "requiredQueryParam1" to "3",
                "requiredQueryParam2" to "4",
            ),
            accept = null,
            parseResponseBody = false,
            responseAssertions = {
                assertEquals("Header header2 is missing", this.body<String>())
                httpStatusIsBadRequest()
            }
        ) {}
    }

    @Test
    fun `patchHelloWorld returns 415 when Content-Type header is missing for JSON input`() {
        patchHelloWorldRequest(
            queryParameters = listOf(
                "requiredQueryParam1" to "3",
                "requiredQueryParam2" to "4",
            ),
            additionalHeaders = listOf(
                "header2" to "2"
            ),
            contentType = null,
            parseResponseBody = false,
            responseAssertions = {
                httpStatusIsUnsupportedMediaType()
            }
        ) {}
    }

    @Test
    fun `patchHelloWorld returns 200 when all is missing except accept header`() {
        patchHelloWorldRequest(
            queryParameters = listOf(
                "optionalQueryParam1" to "1",
                "optionalQueryParam2" to "2",
                "requiredQueryParam1" to "3",
                "requiredQueryParam2" to "4",
            ),
            contentType = ContentType.Application.Json,
            additionalHeaders = listOf(
                "header1" to "1",
                "header2" to "2"
            ),
            responseAssertions = {
                assert(
                    headers.contains("responseHeader1", "1")
                )
                assert(
                    headers.contains("responseHeader2", "2")
                )

                is200WithApplicationJsonContent()
            }
        ) {
            assertEquals(
                expectedHelloWorldResponseDTO,
                this
            )
        }
    }

    @Test
    fun `test patch endpoint 'patchHelloWorld' which response with 406 when wrong accept header in request`() {
        patchHelloWorldRequest(
            queryParameters = listOf(
                "requiredQueryParam1" to "3",
                "requiredQueryParam2" to "4",
            ),
            accept = ContentType.Text.Plain,
            additionalHeaders = listOf(
                "header2" to "2"
            ),
            parseResponseBody = false,
            responseAssertions = {
                httpStatusIsNotAcceptable()
            }
        ) {}
    }
}
