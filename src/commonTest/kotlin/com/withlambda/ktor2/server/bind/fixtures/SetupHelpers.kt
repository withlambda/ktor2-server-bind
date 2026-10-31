package com.withlambda.ktor2.server.bind.fixtures

import com.withlambda.ktor2.server.bind.fixtures.auth.TokenTestConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

/**
 * Adds an `Authorization: Bearer <token>` header to this request, where the token encodes [roles]
 * as a comma-separated string produced by [TokenTestConfig.makeToken].
 *
 * Use this helper inside a [withCustomTestApplication] block to simulate an authenticated caller
 * with a specific set of roles.
 *
 * @param roles The set of role identifiers to embed in the Bearer token.
 */
fun HttpRequestBuilder.addAuthorizationHeaderWithToken(roles: Set<String>) {
    val token = TokenTestConfig.makeToken(roles)
    header("Authorization", "Bearer $token")
}
