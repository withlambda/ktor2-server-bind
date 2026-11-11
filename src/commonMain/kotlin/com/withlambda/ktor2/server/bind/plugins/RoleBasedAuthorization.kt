@file:Suppress("UNUSED")
package io.withlambda.ktor2.server.bind.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.util.logging.KtorSimpleLogger
import io.withlambda.ktor2.server.bind.config.LogLevel
import io.withlambda.ktor2.server.bind.config.auth.PrincipalRoles
import io.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfiguration
import io.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl

private val logger = KtorSimpleLogger("io.withlambda.ktor2.server.bind.plugins.RoleBasedAuthorizationPluginKt")

/**
 * The plugin that specifies role-based authorization per route.
 *
 * Role checks (allOf, anyOf, disallowed) are evaluated first. If they pass — or if no role sets are
 * configured — the custom [RoleBasedAuthorizationConfiguration.predicate] is evaluated next.
 * The request is rejected with HTTP 403 if either check fails.
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
            requiredRolesAllOf.isNotEmpty() || requiredRolesAnyOf.isNotEmpty() ||
                disallowedRoles.isNotEmpty() || predicateConfigured
        ) {
        }

        on(AuthenticationChecked) { call ->

            if (enabled.not()) return@on

            val principal = call.principal<PrincipalRoles>() ?: PrincipalRoles(emptySet())
            val principalRoles = principal.roles
            val hasRoleConfig = requiredRolesAllOf.isNotEmpty() ||
                requiredRolesAnyOf.isNotEmpty() ||
                disallowedRoles.isNotEmpty()

            // Evaluate role-based checks when role sets are configured; skip them for predicate-only setups.
            val rolesAuthorized = if (hasRoleConfig) {
                when {
                    principalRoles.isEmpty() -> {
                        logEvent?.invoke("No roles found for principal.")
                        false
                    }

                    principalRoles.containsAll(requiredRolesAllOf).not() -> {
                        logEvent?.invoke("Principal does not contain all required roles: `$requiredRolesAllOf`.")
                        false
                    }

                    requiredRolesAnyOf.isNotEmpty() && requiredRolesAnyOf.none { principalRoles.contains(it) } -> {
                        logEvent?.invoke(
                            "Principal does not contain any of the required roles: `$requiredRolesAnyOf`."
                        )
                        false
                    }

                    disallowedRoles.isNotEmpty() && disallowedRoles.any { principalRoles.contains(it) } -> {
                        logEvent?.invoke("Principal contains a disallowed role: `$disallowedRoles`.")
                        false
                    }

                    else -> true
                }
            } else {
                true
            }

            // Apply the custom predicate after role checks pass; it may impose additional constraints.
            val isAuthorized = rolesAuthorized && predicate(principal, call)

            if (isAuthorized.not()) {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
    }
}

/**
 * Set up role based authorization for a route
 * via [RoleBasedAuthorizationConfiguration] instance.
 *
 * At least one of [RoleBasedAuthorizationConfiguration.requiredRolesAllOf],
 * [RoleBasedAuthorizationConfiguration.requiredRolesAnyOf],
 * [RoleBasedAuthorizationConfiguration.disallowedRoles], or a custom
 * [RoleBasedAuthorizationConfiguration.predicate] must be configured.
 *
 * @param roleBasedAuthorizationConfiguration Instance of [RoleBasedAuthorizationConfiguration]
 * @throws IllegalStateException if no roles and no custom predicate are configured.
 */
fun Route.roleBasedAuthorization(
    roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfiguration
) = with(roleBasedAuthorizationConfiguration) {
    check(
        requiredRolesAllOf.isNotEmpty() ||
            requiredRolesAnyOf.isNotEmpty() ||
            disallowedRoles.isNotEmpty() ||
            predicateConfigured
    ) {
        """
            For the role based authorization plugin at least one role must be provided for either
            the `requiredRolesAllOf`, `requiredRolesAnyOf` or `disallowedRoles` parameter,
            or a custom `predicate` must be configured.
        """.replace("\n", " ").trimIndent().trimEnd()
    }

    install(plugin = RoleBasedAuthorizationPlugin) {
        this.enabled = this@with.enabled
        this.requiredRolesAllOf = this@with.requiredRolesAllOf
        this.requiredRolesAnyOf = this@with.requiredRolesAnyOf
        this.disallowedRoles = this@with.disallowedRoles
        this.logLevel = this@with.logLevel
        this.predicate = this@with.predicate
    }
}

/**
 * Set up role-based authorization for a route with a single authorized role.
 *
 * @param role Name of the authorized role.
 */
fun Route.authorizedWithRole(role: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAllOf = setOf(role)
    )
)

/**
 * Set up role-based authorization for a route with multiple authorized roles that
 * all must be available in the request such that access to the route is authorized.
 *
 * @param roles Name of roles that all must be available in the request to get authorized access to the route.
 */
fun Route.authorizedWithAllRoles(vararg roles: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAllOf = roles.toSet()
    )
)

/**
 * Set up role-based authorization for a route with multiple authorized roles of which
 * any must be available in the request such that access to the route is authorized.
 *
 * @param roles Name of roles of which any must be available in the request to get authorized access to the route.
 */
fun Route.authorizedWithAnyRole(vararg roles: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAnyOf = roles.toSet()
    )
)

/**
 * Set up role-based authorization for a route with a role that causes a denial of authorization to this route
 * when it is available in the request.
 *
 * @param role Name of a role which causes denial of authorization to the route when it is available in the request.
 */
fun Route.unauthorizedWithRole(role: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        disallowedRoles = setOf(role)
    )
)

/**
 * Set up role-based authorization for a route with roles that causes a denial of authorization to this route
 * when any of those roles is available in the request.
 *
 * @param roles Name of roles which cause denial of authorization to the route when any of those roles is available
 *   in the request.
 */
fun Route.unauthorizedWithAnyRole(vararg roles: String) = roleBasedAuthorization(
    RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        disallowedRoles = roles.toSet()
    )
)
