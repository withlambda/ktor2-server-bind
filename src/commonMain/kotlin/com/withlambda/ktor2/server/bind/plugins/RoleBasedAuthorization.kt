@file:Suppress("UNUSED")
package com.withlambda.ktor2.server.bind.plugins

import com.withlambda.ktor2.server.bind.config.LogLevel
import com.withlambda.ktor2.server.bind.config.auth.PrincipalRoles
import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfiguration
import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.logging.KtorSimpleLogger

private val logger = KtorSimpleLogger("com.withlambda.ktor2.server.bind.plugins.RoleBasedAuthorizationPluginKt")

/**
 * The plugin that specifies role-based authorization per route.
 */
val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleBasedAuthorizationPlugin",
    createConfiguration = ::RoleBasedAuthorizationConfigurationImpl
) {

    with(pluginConfig) {

        val logEvent: ((String) -> Unit)? = logLevel?.let { level ->
            when (level) {
                LogLevel.TRACE -> logger::trace
                LogLevel.DEBUG -> logger::debug
                LogLevel.INFO -> logger::info
                LogLevel.WARN -> logger::warn
                LogLevel.ERROR -> logger::error
            }
        }

        require(
            requiredRolesAllOf.isEmpty().not() || requiredRolesAnyOf.isEmpty().not() || disallowedRoles.isEmpty().not()
        ) {
        }
        on(AuthenticationChecked) { call ->

            if (enabled.not()) return@on

            val principalRoles = call.principal<PrincipalRoles>()?.roles ?: emptySet()

            var isAuthorized = false

            when {
                principalRoles.isEmpty() -> {
                    logEvent?.invoke("No roles found for principal.")
                }

                principalRoles.containsAll(requiredRolesAllOf).not() -> {
                    logEvent?.invoke("Principal does not contain all required roles: `$requiredRolesAllOf`.")
                }

                requiredRolesAnyOf.isNotEmpty() &&
                    requiredRolesAnyOf.none {
                        principalRoles.contains(it)
                    } -> {
                    logEvent?.invoke("Principal does not contain any of the required roles: `$requiredRolesAnyOf`.")
                }

                disallowedRoles.isNotEmpty() && disallowedRoles.any { principalRoles.contains(it) } -> {
                    logEvent?.invoke("Principal contains a disallowed role: `$disallowedRoles`.")
                }

                else -> {
                    isAuthorized = true
                }
            }

            if (isAuthorized.not()) {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}

/**
 * Set up role based authorization for a route
 * via [RoleBasedAuthorizationConfiguration] instance
 * @param roleBasedAuthorizationConfiguration Instance of [RoleBasedAuthorizationConfiguration]
 */
fun Route.roleBasedAuthorization(
    roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfiguration
) = with(roleBasedAuthorizationConfiguration) {
    check(
        requiredRolesAllOf.isNotEmpty() ||
            requiredRolesAnyOf.isNotEmpty() ||
            disallowedRoles.isNotEmpty()
    ) {
        """
            For the role based authorization plugin at least one role must be provided for either
            the `requiredRolesAllOf`, `requiredRolesAnyOf` or `disallowedRoles` parameter.
        """.replace("\n", " ").trimIndent().trimEnd()
    }

    install(plugin = RoleBasedAuthorizationPlugin) {
        this.enabled = this@with.enabled
        this.requiredRolesAllOf = this@with.requiredRolesAllOf
        this.requiredRolesAnyOf = this@with.requiredRolesAnyOf
        this.disallowedRoles = this@with.disallowedRoles
        this.logLevel = this@with.logLevel
    }
}

/**
 * Set up role-based authorization for a route with a single authorized role
 * @param role Name of the authorized role
 */
fun Route.authorizedWithRole(role: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAllOf = setOf(role)
    )
)

/**
 * Set up role-based authorization for a route with multiple authorized roles that
 * all must be available in the request such that access to the route is authorized
 * @param roles Name of roles that all must be available in the request to get authorized access to the route
 */
fun Route.authorizedWithAllRoles(vararg roles: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAllOf = roles.toSet()
    )
)

/**
 * Set up role-based authorization for a route with multiple authorized roles of which
 * any must be available in the request such that access to the route is authorized
 * @param roles Name of roles of which any must be available in the request to get authorized access to the route
 */
fun Route.authorizedWithAnyRole(vararg roles: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAnyOf = roles.toSet()
    )
)

/**
 * Set up role-based authorization for a route with a role that causes a denial of authorization to this route
 * when it is available in the request
 * @param role Name of a role which causes denial of authorization to the route when it is available in the request
 */
fun Route.unauthorizedWithRole(role: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        disallowedRoles = setOf(role)
    )
)

/**
 * Set up role-based authorization for a route with roles that causes a denial of authorization to this route
 * when any of those roles is available in the request
 * @param roles Name of roles which cause denial of authorization to the route when any of those roles is available
 * in the request
 */
fun Route.unauthorizedWithAnyRole(vararg roles: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        disallowedRoles = roles.toSet()
    )
)
