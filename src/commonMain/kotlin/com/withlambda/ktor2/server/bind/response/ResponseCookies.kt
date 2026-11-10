package com.withlambda.ktor2.server.bind.response

import com.withlambda.ktor2.server.bind.Cookies
import io.ktor.http.Cookie

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
