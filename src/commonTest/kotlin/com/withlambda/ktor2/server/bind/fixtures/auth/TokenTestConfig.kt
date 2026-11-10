package com.withlambda.ktor2.server.bind.fixtures.auth

/**
 * Utility for creating test authentication tokens.
 *
 * Tokens produced here are consumed by [com.withlambda.ktor2.server.bind.fixtures.plugins.TokenAuthProvider],
 * which decodes the comma-separated role list back into a
 * [com.withlambda.ktor2.server.bind.config.auth.PrincipalRoles] instance.
 */
object TokenTestConfig {
    /**
     * Produces a Bearer token that encodes the given roles as a comma-separated string.
     *
     * @param roles The set of role identifiers to embed in the token.
     * @return A plain-text token string suitable for use as a `Bearer` credential in the
     *   `Authorization` header.
     */
    fun makeToken(roles: Set<String>): String = roles.joinToString(",")
}
