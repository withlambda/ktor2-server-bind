package io.withlambda.ktor2.server.bind.fixtures

import io.ktor.http.Cookie
import io.withlambda.ktor2.server.bind.request.RequestCookies
import io.withlambda.ktor2.server.bind.response.ResponseCookies

object EmptyRequestCookies : RequestCookies
object EmptyResponseCookies : ResponseCookies {
    override val mapOfCookies: Map<String, Cookie> = emptyMap()
}
