package io.withlambda.ktor2.server.bind.response

import io.withlambda.ktor2.server.bind.Headers

/**
 * Interface for Response headers of a route
 * @property mapOfHeaders A map storing all response headers as key-value pairs
 */
interface ResponseHeaders : Headers {
    /**
     * A map storing all response headers as key-value pairs
     */
    val mapOfHeaders: Map<String, Any>
}
