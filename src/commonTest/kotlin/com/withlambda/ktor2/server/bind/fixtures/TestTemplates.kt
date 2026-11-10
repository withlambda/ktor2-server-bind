@file:Suppress("UNUSED", "MemberVisibilityCanBePrivate", "UNUSED_IMPORT")
package com.withlambda.ktor2.server.bind.fixtures

import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.deleteRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.getRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.headRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsBadRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.httpStatusIsOK
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.is200WithApplicationJsonContent
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.json
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.optionsRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.patchRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.postRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.putRequest
import com.withlambda.ktor2.server.bind.fixtures.TestTemplates.request
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.http.withCharset
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Collection of reusable HTTP assertion helpers and request factory functions for the Hello World
 * fixture integration tests.
 *
 * ### Status assertions
 * Extension functions on [HttpResponse] that assert a specific HTTP status code
 * (e.g. [httpStatusIsOK], [httpStatusIsBadRequest]) or a combined status + content-type
 * (e.g. [is200WithApplicationJsonContent]).
 *
 * ### Request helpers
 * Inline functions that spin up a [withCustomTestApplication] instance, send an HTTP request, and
 * run caller-supplied assertion lambdas against both the raw [HttpResponse] and the deserialized
 * response body:
 * - [request] — generic entry point accepting any [HttpMethod].
 * - [postRequest], [putRequest], [patchRequest] — convenience wrappers for body-bearing methods.
 * - [getRequest], [deleteRequest] — convenience wrappers for body-less methods.
 * - [headRequest], [optionsRequest] — wrappers for methods that return no response body.
 *
 * ### JSON serializer
 * The shared [json] instance is configured identically to the server's content-negotiation
 * serializer (see [com.withlambda.ktor2.server.bind.fixtures.plugins.configureContentNegotiation])
 * so that request serialization and response deserialization are consistent.
 */
object TestTemplates {
    /**
     * Shared [Json] instance used to serialize request bodies and deserialize response bodies in
     * all request helper functions.
     *
     * Configuration:
     * - `prettyPrint = true` — human-readable output.
     * - `isLenient = false` — strict JSON; no unquoted keys.
     * - `ignoreUnknownKeys = false` — unknown keys are an error.
     * - `explicitNulls = false` — `null` fields are omitted from output.
     */
    @OptIn(ExperimentalSerializationApi::class)
    val json: Json = Json {
        prettyPrint = true
        isLenient = false
        ignoreUnknownKeys = false
        explicitNulls = false
    }

    private const val ERROR_MESSAGE_RESPONSE_STATUS = "The http status of the response does not match the expected one."
    private const val ERROR_MESSAGE_RESPONSE_CONTENT_TYPE =
        "The content type of the response does not match the expected one."

    /** Asserts that the response status is [HttpStatusCode.OK] (200). */
    fun HttpResponse.httpStatusIsOK() {
        assertEquals(HttpStatusCode.OK, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.NoContent] (204). */
    fun HttpResponse.httpStatusIsNoContent() {
        assertEquals(HttpStatusCode.NoContent, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.Accepted] (202). */
    fun HttpResponse.httpStatusIsAccepted() {
        assertEquals(HttpStatusCode.Accepted, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.MultiStatus] (207). */
    fun HttpResponse.httpStatusIsMultiStatus() {
        assertEquals(HttpStatusCode.MultiStatus, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.BadRequest] (400). */
    fun HttpResponse.httpStatusIsBadRequest() {
        assertEquals(HttpStatusCode.BadRequest, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.Unauthorized] (401). */
    fun HttpResponse.httpStatusIsUnauthorized() {
        assertEquals(HttpStatusCode.Unauthorized, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.Forbidden] (403). */
    fun HttpResponse.httpStatusIsForbidden() {
        assertEquals(HttpStatusCode.Forbidden, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.NotAcceptable] (406). */
    fun HttpResponse.httpStatusIsNotAcceptable() {
        assertEquals(HttpStatusCode.NotAcceptable, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.UnsupportedMediaType] (415). */
    fun HttpResponse.httpStatusIsUnsupportedMediaType() {
        assertEquals(HttpStatusCode.UnsupportedMediaType, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.NotImplemented] (501). */
    fun HttpResponse.httpStatusIsNotImplemented() {
        assertEquals(HttpStatusCode.NotImplemented, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is [HttpStatusCode.InternalServerError] (500). */
    fun HttpResponse.httpStatusIsInternalServerError() {
        assertEquals(HttpStatusCode.InternalServerError, this.status, ERROR_MESSAGE_RESPONSE_STATUS)
    }

    /** Asserts that the response status is 200 OK and the content type is `application/json; charset=UTF-8`. */
    fun HttpResponse.is200WithApplicationJsonContent() {
        httpStatusIsOK()
        assertEquals(
            ContentType.Application.Json.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /** Asserts that the response status is 202 Accepted and the content type is `application/json; charset=UTF-8`. */
    fun HttpResponse.is202WithApplicationJsonContent() {
        httpStatusIsAccepted()
        assertEquals(
            ContentType.Application.Json.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /**
     * Asserts that the response status is 207 Multi-Status and the content type is
     * `application/json; charset=UTF-8`.
     */
    fun HttpResponse.is207WithApplicationJsonContent() {
        httpStatusIsMultiStatus()
        assertEquals(
            ContentType.Application.Json.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /**
     * Asserts that the response status is 400 Bad Request and the content type is
     * `application/json; charset=UTF-8`.
     */
    fun HttpResponse.is400WithApplicationJsonContent() {
        httpStatusIsBadRequest()
        assertEquals(
            ContentType.Application.Json.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /** Asserts that the response status is 400 Bad Request and the content type is `text/plain; charset=UTF-8`. */
    fun HttpResponse.is400WithTextPlainContent() {
        httpStatusIsBadRequest()
        assertEquals(
            ContentType.Text.Plain.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /**
     * Asserts that the response status is 500 Internal Server Error and the content type is
     * `text/plain; charset=UTF-8`.
     */
    fun HttpResponse.is500WithTextPlainContent() {
        httpStatusIsInternalServerError()
        assertEquals(
            ContentType.Text.Plain.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /** Asserts that the response status is 501 Not Implemented and the content type is `text/plain; charset=UTF-8`. */
    fun HttpResponse.is501WithTextPlainContent() {
        httpStatusIsNotImplemented()
        assertEquals(
            ContentType.Text.Plain.withCharset(Charsets.UTF_8),
            this.contentType(),
            ERROR_MESSAGE_RESPONSE_CONTENT_TYPE
        )
    }

    /**
     * Reads the response body as a string and deserializes it to [T] using [json].
     *
     * Fails the test with a descriptive message if the body cannot be parsed as [T].
     *
     * @param T The target deserialization type.
     * @return The deserialized response body.
     */
    suspend inline fun <reified T : Any> HttpResponse.toParsedResponse(): T {
        val content = this.body<String>()
        return runCatching { json.decodeFromString<T>(content) }.getOrElse {
            fail(
                "Received response content could not be parsed as the desired target type ${T::class.simpleName}. " +
                    "Received response content data: $content. " +
                    "Error: $it"
            )
        }
    }

    /**
     * Sends an HTTP request to the test application and runs assertions on the response.
     *
     * Internally calls [withCustomTestApplication], builds the request using [httpMethod] and
     * [path], optionally attaches a serialized [input] body, query parameters, headers, and a
     * Bearer token, then invokes [responseAssertions] on the raw [HttpResponse] and
     * (when [parseResponseBody] is `true`) [parsedResponseAssertions] on the deserialized body.
     *
     * @param RequestBodyType The type of the optional request body; use [Unit] when there is none.
     * @param ResponseBodyType The expected deserialized response body type.
     * @param httpMethod The HTTP method to use for the request.
     * @param path The URL path (without query string) to send the request to.
     * @param input Optional request body to serialize and attach; `null` means no body.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param accept Optional `Accept` header content type.
     * @param contentType Optional `Content-Type` header value.
     * @param authorizationRoles Roles to encode in the Bearer token; an empty set means no
     *   `Authorization` header is added.
     * @param parseResponseBody When `true` (default), the response body is deserialized and
     *   passed to [parsedResponseAssertions].
     * @param additionalHeaders Extra headers appended to the request as name-value pairs.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     * @param parsedResponseAssertions Assertions executed on the deserialized [ResponseBodyType].
     */
    inline fun <reified RequestBodyType : Any, reified ResponseBodyType : Any> request(
        httpMethod: HttpMethod,
        path: String,
        input: RequestBodyType? = null,
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = null,
        contentType: ContentType? = null,
        authorizationRoles: Set<String> = emptySet(),
        parseResponseBody: Boolean = true,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend ResponseBodyType.() -> Unit,
    ) {
        val queryParametersAsString = queryParameters?.formUrlEncode()?.let { "?$it" }
        val fullPath = "${path}${queryParametersAsString ?: ""}"
        val inputSerialized = input?.let { json.encodeToString(it) }

        withCustomTestApplication {
            val httpResponse = client.request {
                method = httpMethod
                url(fullPath)
                if (authorizationRoles.isNotEmpty()) {
                    addAuthorizationHeaderWithToken(authorizationRoles)
                }
                contentType?.let { header(HttpHeaders.ContentType, it) }
                accept?.let { accept(it) }
                additionalHeaders.forEach { (name, value) ->
                    header(name, value)
                }
                inputSerialized?.let { setBody(it) }
            }

            with(httpResponse) {
                responseAssertions()
                if (parseResponseBody) {
                    toParsedResponse<ResponseBodyType>().parsedResponseAssertions()
                }
            }
        }
    }

    /**
     * Sends a POST request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Post].
     *
     * @param RequestBodyType The type of the optional request body; use [Unit] when there is none.
     * @param ResponseBodyType The expected deserialized response body type.
     * @param path The URL path to send the request to.
     * @param input Optional request body to serialize and attach.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param accept Optional `Accept` header content type.
     * @param contentType Optional `Content-Type` header value.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param parseResponseBody When `true`, the response body is deserialized for assertions.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     * @param parsedResponseAssertions Assertions executed on the deserialized [ResponseBodyType].
     */
    inline fun <reified RequestBodyType : Any, reified ResponseBodyType : Any> postRequest(
        path: String,
        input: RequestBodyType? = null,
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = null,
        contentType: ContentType? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend ResponseBodyType.() -> Unit,
    ) {
        request<RequestBodyType, ResponseBodyType>(
            httpMethod = HttpMethod.Post,
            path = path,
            input = input,
            queryParameters = queryParameters,
            accept = accept,
            contentType = contentType,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions
        )
    }

    /**
     * Sends a PUT request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Put].
     *
     * @param RequestBodyType The type of the optional request body; use [Unit] when there is none.
     * @param ResponseBodyType The expected deserialized response body type.
     * @param path The URL path to send the request to.
     * @param input Optional request body to serialize and attach.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param accept Optional `Accept` header content type.
     * @param contentType Optional `Content-Type` header value.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param parseResponseBody When `true`, the response body is deserialized for assertions.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     * @param parsedResponseAssertions Assertions executed on the deserialized [ResponseBodyType].
     */
    inline fun <reified RequestBodyType : Any, reified ResponseBodyType : Any> putRequest(
        path: String,
        input: RequestBodyType? = null,
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = null,
        contentType: ContentType? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend ResponseBodyType.() -> Unit,
    ) {
        request<RequestBodyType, ResponseBodyType>(
            httpMethod = HttpMethod.Put,
            path = path,
            input = input,
            queryParameters = queryParameters,
            accept = accept,
            contentType = contentType,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions
        )
    }

    /**
     * Sends a PATCH request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Patch].
     *
     * @param RequestBodyType The type of the optional request body; use [Unit] when there is none.
     * @param ResponseBodyType The expected deserialized response body type.
     * @param path The URL path to send the request to.
     * @param input Optional request body to serialize and attach.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param accept Optional `Accept` header content type.
     * @param contentType Optional `Content-Type` header value.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param parseResponseBody When `true`, the response body is deserialized for assertions.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     * @param parsedResponseAssertions Assertions executed on the deserialized [ResponseBodyType].
     */
    inline fun <reified RequestBodyType : Any, reified ResponseBodyType : Any> patchRequest(
        path: String,
        input: RequestBodyType? = null,
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = null,
        contentType: ContentType? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend ResponseBodyType.() -> Unit,
    ) {
        request<RequestBodyType, ResponseBodyType>(
            httpMethod = HttpMethod.Patch,
            path = path,
            input = input,
            queryParameters = queryParameters,
            accept = accept,
            contentType = contentType,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions
        )
    }

    /**
     * Sends a GET request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Get] and passes no request body.
     *
     * @param ResponseBodyType The expected deserialized response body type.
     * @param path The URL path to send the request to.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param accept Optional `Accept` header content type.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param parseResponseBody When `true`, the response body is deserialized for assertions.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     * @param parsedResponseAssertions Assertions executed on the deserialized [ResponseBodyType].
     */
    inline fun <reified ResponseBodyType : Any> getRequest(
        path: String,
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend ResponseBodyType.() -> Unit,
    ) {
        request<Unit, ResponseBodyType>(
            httpMethod = HttpMethod.Get,
            path = path,
            input = null,
            queryParameters = queryParameters,
            accept = accept,
            contentType = null,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions
        )
    }

    /**
     * Sends a DELETE request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Delete] and passes no request body.
     *
     * @param ResponseBodyType The expected deserialized response body type.
     * @param path The URL path to send the request to.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param accept Optional `Accept` header content type.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param parseResponseBody When `true`, the response body is deserialized for assertions.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     * @param parsedResponseAssertions Assertions executed on the deserialized [ResponseBodyType].
     */
    inline fun <reified ResponseBodyType : Any> deleteRequest(
        path: String,
        queryParameters: List<Pair<String, String>>? = null,
        accept: ContentType? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        parseResponseBody: Boolean = true,
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
        crossinline parsedResponseAssertions: suspend ResponseBodyType.() -> Unit,
    ) {
        request<Unit, ResponseBodyType>(
            httpMethod = HttpMethod.Delete,
            path = path,
            input = null,
            queryParameters = queryParameters,
            accept = accept,
            contentType = null,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = parseResponseBody,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = parsedResponseAssertions
        )
    }

    /**
     * Sends a HEAD request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Head], passes no request body,
     * and skips response body deserialization (HEAD responses carry no body by definition).
     *
     * @param path The URL path to send the request to.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     */
    inline fun headRequest(
        path: String,
        queryParameters: List<Pair<String, String>>? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
    ) {
        request<Unit, Unit>(
            httpMethod = HttpMethod.Head,
            path = path,
            input = null,
            queryParameters = queryParameters,
            accept = null,
            contentType = null,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = false,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = {}
        )
    }

    /**
     * Sends an OPTIONS request to the test application.
     *
     * Convenience wrapper around [request] that fixes [HttpMethod.Options], passes no request body,
     * and skips response body deserialization.
     *
     * @param path The URL path to send the request to.
     * @param queryParameters Optional list of query parameter name-value pairs.
     * @param additionalHeaders Extra headers appended to the request.
     * @param authorizationRoles Roles to encode in the Bearer token.
     * @param responseAssertions Assertions executed on the raw [HttpResponse].
     */
    inline fun optionsRequest(
        path: String,
        queryParameters: List<Pair<String, String>>? = null,
        additionalHeaders: List<Pair<String, Any?>> = emptyList(),
        authorizationRoles: Set<String> = emptySet(),
        crossinline responseAssertions: suspend HttpResponse.() -> Unit = {},
    ) {
        request<Unit, Unit>(
            httpMethod = HttpMethod.Options,
            path = path,
            input = null,
            queryParameters = queryParameters,
            accept = null,
            contentType = null,
            additionalHeaders = additionalHeaders,
            authorizationRoles = authorizationRoles,
            parseResponseBody = false,
            responseAssertions = responseAssertions,
            parsedResponseAssertions = {}
        )
    }
}
