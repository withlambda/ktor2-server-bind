package com.withlambda.ktor2.server.bind.response

import io.ktor.http.HttpStatusCode

/**
 * Interface to specify response data for a route such as the response http status code, message, and headers
 * @property httpStatusCode The response http status code
 * @property responseMessage The response message
 * @property responseHeaders The response headers
 * @property responseCookies The response cookies
 */
interface ResponseData {
    /**
     * The response http status code
     */
    val httpStatusCode: HttpStatusCode

    /**
     * The response message
     */
    val responseMessage: String?

    /**
     * The response headers
     */
    val responseHeaders: ResponseHeaders?

    /**
     * The response cookies
     */
    val responseCookies: ResponseCookies?
}

/**
 * Interface to specify response data for a route with response body
 * such as the response http status code, message, headers, and body
 * @param ResponseBodyType The type of the response body
 * @property responseBody The response body
 */
interface ResponseDataWithBody<ResponseBodyType> : ResponseData {
    /**
     * The response body
     */
    val responseBody: ResponseBodyType?
}
