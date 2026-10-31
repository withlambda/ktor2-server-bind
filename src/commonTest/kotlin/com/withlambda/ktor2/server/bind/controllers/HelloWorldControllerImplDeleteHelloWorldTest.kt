package com.withlambda.ktor2.server.bind.controllers

import com.withlambda.ktor2.server.bind.fixtures.TestTemplates
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsBadRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsForbidden
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsNotAcceptable
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsUnauthorized
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.is200WithApplicationJsonContent
import com.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldResponseDTO
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import kotlin.test.Test
import kotlin.test.assertEquals

class HelloWorldControllerImplDeleteHelloWorldTest {

    private val deleteHelloWorldAuthorizationRoles = setOf("deleteHelloWorld1", "deleteHelloWorld2")

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

    private inline fun deleteHelloWorldRequest(
        path: String = "/helloworld/1/value",
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = ContentType.Application.Json,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = deleteHelloWorldAuthorizationRoles,
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend HelloWorldResponseDTO.() -> Unit
    ) {
        TestTemplates.deleteRequest<HelloWorldResponseDTO>(
            path = path,
            queryParameters = queryParameters,
            authorizationRoles = authorizationRoles,
            accept = accept,
            additionalHeaders = additionalHeaders,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions,
        )
    }

    @Test
    fun `test delete endpoint 'deleteHelloWorld' which response with status 200 and response body when all is ok`() {
        deleteHelloWorldRequest(
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
    fun `test delete endpoint 'deleteHelloWorld' which response with status 401 when no Authorization header is provided`() {
        deleteHelloWorldRequest(
            authorizationRoles = emptySet(),
            parseResponseBody = false,
            responseAssertions = {
                httpStatusIsUnauthorized()
            }
        ) {}
    }

    @Test
    fun `test delete endpoint 'deleteHelloWorld' which response with status 403 when no Authorization role is wrong`() {
        deleteHelloWorldRequest(
            authorizationRoles = setOf("wrongRole"),
            parseResponseBody = false,
            responseAssertions = {
                httpStatusIsForbidden()
            }
        ) {}
    }

    @Test
    fun `test delete endpoint 'deleteHelloWorld' which response with status 400 when required query parameter is missing`() {
        deleteHelloWorldRequest(
            parseResponseBody = false,
            accept = null,
            responseAssertions = {
                assertEquals("Request parameter requiredQueryParam1 is missing", this.body<String>())
                httpStatusIsBadRequest()
            }
        ) {}
    }

    @Test
    fun `test delete endpoint 'deleteHelloWorld' which response with status 400 when required request header is missing`() {
        deleteHelloWorldRequest(
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
    fun `test delete endpoint 'deleteHelloWorld' which response with 200 when all is missing except accept header`() {
        deleteHelloWorldRequest(
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
    fun `test delete endpoint 'deleteHelloWorld' which response with 406 when wrong accept header in request`() {
        deleteHelloWorldRequest(
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
