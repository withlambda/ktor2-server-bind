package io.withlambda.ktor2.server.bind.config.auth

import io.withlambda.ktor2.server.bind.config.LogLevel
import io.withlambda.ktor2.server.bind.config.Logging

/**
 * Implementation of [RoleBasedAuthorizationConfiguration] to store role based authorization configuration.
 *
 * Setting [predicate] automatically marks [predicateConfigured] as `true`, enabling predicate-only
 * authorization without requiring any role sets to be populated.
 */
class RoleBasedAuthorizationConfigurationImpl(
    override var enabled: Boolean = true,
    override var requiredRolesAllOf: Set<String> = emptySet(),
    override var requiredRolesAnyOf: Set<String> = emptySet(),
    override var disallowedRoles: Set<String> = emptySet(),
    override var logLevel: LogLevel? = Logging.LogLevel.Plugin.roleBasedAuthorization
) : RoleBasedAuthorizationConfiguration {

    override var predicateConfigured: Boolean = false
        private set

    /**
     * Custom authorization predicate evaluated after role checks pass.
     * Defaults to always returning `true`. Setting this property marks [predicateConfigured] as `true`.
     */
    override var predicate: AuthorizationPredicate = { _, _ -> true }
        set(value) {
            field = value
            predicateConfigured = true
        }
}
