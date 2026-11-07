package com.withlambda.ktor2.server.bind.controllers

import com.withlambda.ktor2.server.bind.fixtures.TestTemplates
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsBadRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsForbidden
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsNoContent
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsUnauthorized
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class HelloWorldControllerImplOptionsHelloWorldTest {

    private val optionsHelloWorldAuthorizationRoles = setOf("optionsHelloWorld1", "optionsHelloWorld2")

    private inline fun optionsHelloWorldRequest(
        path: String = "/helloworld/1/value",
        queryParameters: List<Pair<String, String>>? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = optionsHelloWorldAuthorizationRoles,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
    ) {
        TestTemplates.optionsRequest(
            path = path,
            queryParameters = queryParameters,
            authorizationRoles = authorizationRoles,
            additionalHeaders = additionalHeaders,
            responseAssertions = responseAssertions,
        )
    }

    @Test
    fun `test options endpoint 'optionsHelloWorld' which response with status 200 and response body when all is ok`() {
        optionsHelloWorldRequest(
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
                assert(
                    headers.contains("Allow", "OPTIONS, GET, HEAD, POST, PATCH, PUT, DELETE")
                )

                httpStatusIsNoContent()
            }
        )
    }

    @Test
    fun `optionsHelloWorld returns 401 when Authorization header is missing`() {
        optionsHelloWorldRequest(
            authorizationRoles = emptySet(),
            responseAssertions = {
                httpStatusIsUnauthorized()
            }
        )
    }

    @Test
    fun `optionsHelloWorld returns 403 when Authorization role is wrong`() {
        optionsHelloWorldRequest(
            authorizationRoles = setOf("wrongRole"),
            responseAssertions = {
                httpStatusIsForbidden()
            }
        )
    }

    @Test
    fun `optionsHelloWorld returns 400 when required query parameter is missing`() {
        optionsHelloWorldRequest(
            responseAssertions = {
                assertEquals("Request parameter requiredQueryParam1 is missing", this.body<String>())
                httpStatusIsBadRequest()
            }
        )
    }

    @Test
    fun `optionsHelloWorld returns 400 when required request header is missing`() {
        optionsHelloWorldRequest(
            queryParameters = listOf(
                "requiredQueryParam1" to "3",
                "requiredQueryParam2" to "4",
            ),
            responseAssertions = {
                assertEquals("Header header2 is missing", this.body<String>())
                httpStatusIsBadRequest()
            }
        )
    }
}
