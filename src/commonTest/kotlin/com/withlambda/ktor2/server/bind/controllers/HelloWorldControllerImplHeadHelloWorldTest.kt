package com.withlambda.ktor2.server.bind.controllers

import com.withlambda.ktor2.server.bind.fixtures.TestTemplates
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsBadRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsForbidden
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsOK
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsUnauthorized
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class HelloWorldControllerImplHeadHelloWorldTest {

    private val headHelloWorldAuthorizationRoles = setOf("headHelloWorld1", "headHelloWorld2")

    private inline fun headHelloWorldRequest(
        path: String = "/helloworld/1/value",
        queryParameters: List<Pair<String, String>>? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = headHelloWorldAuthorizationRoles,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
    ) {
        TestTemplates.headRequest(
            path = path,
            queryParameters = queryParameters,
            authorizationRoles = authorizationRoles,
            additionalHeaders = additionalHeaders,
            responseAssertions = responseAssertions,
        )
    }

    @Test
    fun `test head endpoint 'headHelloWorld' which response with status 200 and response body when all is ok`() {
        headHelloWorldRequest(
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

                httpStatusIsOK()
            }
        )
    }

    @Test
    fun `headHelloWorld returns 401 when Authorization header is missing`() {
        headHelloWorldRequest(
            authorizationRoles = emptySet(),
            responseAssertions = {
                httpStatusIsUnauthorized()
            }
        )
    }

    @Test
    fun `test head endpoint 'headHelloWorld' which response with status 403 when no Authorization role is wrong`() {
        headHelloWorldRequest(
            authorizationRoles = setOf("wrongRole"),
            responseAssertions = {
                httpStatusIsForbidden()
            }
        )
    }

    @Test
    fun `headHelloWorld returns 400 when required query parameter is missing`() {
        headHelloWorldRequest(
            responseAssertions = {
                assertEquals("Request parameter requiredQueryParam1 is missing", this.body<String>())
                httpStatusIsBadRequest()
            }
        )
    }

    @Test
    fun `test head endpoint 'headHelloWorld' which response with status 400 when required request header is missing`() {
        headHelloWorldRequest(
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
