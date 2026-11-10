package com.withlambda.ktor2.server.bind.fixtures

import com.withlambda.ktor2.server.bind.request.RequestCookies
import com.withlambda.ktor2.server.bind.response.ResponseCookies
import io.ktor.http.Cookie

object EmptyRequestCookies : RequestCookies
object EmptyResponseCookies : ResponseCookies {
    override val mapOfCookies: Map<String, Cookie> = emptyMap()
}
