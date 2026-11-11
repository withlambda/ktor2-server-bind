package io.withlambda.ktor2.server.bind.response

import io.ktor.http.Cookie
import io.withlambda.ktor2.server.bind.Cookies

/**
 * Interface for Response cookies of a route
 * @property mapOfCookies A map storing all response cookies as key-value pairs
 */
interface ResponseCookies : Cookies {
    /**
     * A map storing all response cookies as key-value pairs
     */
    val mapOfCookies: Map<String, Cookie>
}
