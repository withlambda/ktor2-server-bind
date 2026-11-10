package com.withlambda.ktor2.server.bind.fixtures.plugins

import com.withlambda.ktor2.server.bind.config.auth.PrincipalRoles
import com.withlambda.ktor2.server.bind.fixtures.auth.TokenTestConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.request.header
import io.ktor.server.response.respond

/**
 * Installs the Ktor [Authentication] plugin on the test application.
 *
 * By default, [tokenAuthentication] is registered as the sole authentication method, which
 * decodes roles from a comma-separated Bearer token produced by [TokenTestConfig.makeToken].
 * The [installAuthentication] flag and the [authenticationMethod] lambda can be overridden to
 * test scenarios with alternative or disabled authentication.
 *
 * @param installAuthentication When `false`, the authentication plugin is installed but no
 *   provider is registered, effectively disabling authentication. Defaults to `true`.
 * @param authenticationMethod A configuration block applied to [AuthenticationConfig]; defaults
 *   to registering [tokenAuthentication].
 */
fun Application.configureAuth(
    installAuthentication: Boolean = true,
    authenticationMethod: AuthenticationConfig.() -> Unit = { tokenAuthentication() }
) {
    install(Authentication) {
        if (installAuthentication) {
            authenticationMethod()
        }
    }
}

/**
 * A simple multiplatform auth provider that reads a Bearer token whose value
 * is a comma-separated list of roles (produced by [TokenTestConfig.makeToken]).
 *
 * When the `Authorization` header is absent or does not start with `Bearer `, the request is
 * challenged with [AuthenticationFailedCause.NoCredentials] and HTTP 401 is returned.
 * Otherwise, the token is split on commas and the resulting set of roles is stored as a
 * [PrincipalRoles] principal on the authentication context.
 *
 * @param config The provider configuration holding the optional provider name.
 */
class TokenAuthProvider(config: Config) : AuthenticationProvider(config) {

    /**
     * Configuration class for [TokenAuthProvider].
     *
     * @param name Optional name used to distinguish this provider from others when multiple
     *   authentication providers are installed.
     */
    class Config(name: String?) : AuthenticationProvider.Config(name)

    /**
     * Authenticates the current request by parsing the `Authorization: Bearer <token>` header.
     *
     * Sets a [PrincipalRoles] principal on success, or issues a 401 challenge on failure.
     *
     * @param context The Ktor authentication context for the current request.
     */
    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val authHeader = context.call.request.header("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            context.challenge("TokenAuth", AuthenticationFailedCause.NoCredentials) { challenge, call ->
                call.respond(HttpStatusCode.Unauthorized)
                challenge.complete()
            }
            return
        }
        val token = authHeader.removePrefix("Bearer ").trim()
        val roles = if (token.isEmpty()) emptySet() else token.split(",").toSet()
        context.principal(PrincipalRoles(roles))
    }
}

/**
 * Registers a [TokenAuthProvider] with the given optional [name] in this [AuthenticationConfig].
 *
 * @param name Optional provider name; pass `null` (the default) to register as the default
 *   unnamed provider.
 */
fun AuthenticationConfig.tokenAuthentication(name: String? = null) {
    register(TokenAuthProvider(TokenAuthProvider.Config(name)))
}
