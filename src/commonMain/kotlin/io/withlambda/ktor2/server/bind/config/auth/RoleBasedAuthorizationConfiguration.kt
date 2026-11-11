package io.withlambda.ktor2.server.bind.config.auth

import io.ktor.server.application.ApplicationCall
import io.withlambda.ktor2.server.bind.config.LogLevel

/**
 * Type alias for a custom authorization predicate that is evaluated during request processing.
 *
 * Receives the principal's [PrincipalRoles] and the current [ApplicationCall], and returns
 * `true` if the request is authorized, or `false` to deny it with HTTP 403.
 */
typealias AuthorizationPredicate = (PrincipalRoles, ApplicationCall) -> Boolean

/**
 * Configuration interface for the RoleBasedAuthorization plugin.
 *
 * Note: The RoleBasedAuthorization plugin is configured for each route individually.
 * At least one of [requiredRolesAllOf], [requiredRolesAnyOf], [disallowedRoles], or
 * a custom [predicate] (tracked by [predicateConfigured]) must be provided.
 *
 * @property enabled Whether to enable role based authorization for a specific route.
 *   If set to `false`, role based authorization is disabled for the specific route.
 * @property requiredRolesAllOf All roles that must be provided in the request in order route access is authorized.
 * @property requiredRolesAnyOf At least one of the roles must be provided in the request in order route access
 *   is authorized.
 * @property disallowedRoles Roles which lead to a denial of route access if they are provided in the request.
 * @property logLevel The level at which to log within the role base authorization plugin. If null,
 *   logging is disabled.
 * @property predicate A custom authorization predicate evaluated after role checks pass. Defaults to
 *   always returning `true` (no additional restriction).
 * @property predicateConfigured Whether a custom [predicate] has been explicitly configured.
 *   Used to allow predicate-only authorization without role sets.
 */
interface RoleBasedAuthorizationConfiguration {
    /**
     * Whether to enable role based authorization for a specific route.
     * If set to `false`, role based authorization is disabled for the specific route.
     */
    var enabled: Boolean

    /**
     * All roles that must be provided in the request in order route access is authorized.
     */
    var requiredRolesAllOf: Set<String>

    /**
     * At least one of the roles must be provided in the request in order route access is authorized.
     */
    var requiredRolesAnyOf: Set<String>

    /**
     * Roles which lead to a denial of route access if they are provided in the request.
     */
    var disallowedRoles: Set<String>

    /**
     * The level at which to log within the role base authorization plugin. If null, logging is disabled.
     */
    var logLevel: LogLevel?

    /**
     * A custom authorization predicate evaluated after role checks pass.
     * Returns `true` to allow the request, or `false` to respond with HTTP 403.
     * Defaults to always returning `true`.
     * Setting this property automatically sets [predicateConfigured] to `true`.
     */
    var predicate: AuthorizationPredicate

    /**
     * Whether a custom [predicate] has been explicitly configured on this instance.
     * Allows the plugin to accept predicate-only authorization without requiring role sets.
     */
    val predicateConfigured: Boolean
}
