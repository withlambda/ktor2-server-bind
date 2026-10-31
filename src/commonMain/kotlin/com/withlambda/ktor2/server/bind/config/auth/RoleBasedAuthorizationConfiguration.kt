package com.withlambda.ktor2.server.bind.config.auth

import com.withlambda.ktor2.server.bind.config.LogLevel

/**
 * Configuration interface for the RoleBasedAuthorization plugin
 * Note: The RoleBasedAuthorization plugin is configured for each route
 * individually.
 * @property enabled Whether to enable role based authorization for a specific route
 * if set to `false`, role based authorization is disabled for the specific route
 * @property requiredRolesAllOf All roles that must be provided in the request in order route access is authorized.
 * @property requiredRolesAnyOf At least one of the roles must be provided in the request in order route access
 * is authorized
 * @property disallowedRoles Roles which lead to a denial of route access if they are provided in the request
 * @property logLevel The level at which to log within the role base authorization plugin. If null,
 * logging is disabled
 */
interface RoleBasedAuthorizationConfiguration {
    /**
     * Whether to enable role based authorization for a specific route
     * if set to `false`, role based authorization is disabled for the specific route
     */
    var enabled: Boolean

    /**
     * All roles that must be provided in the request in order route access is authorized.
     */
    var requiredRolesAllOf: Set<String>

    /**
     * At least one of the roles must be provided in the request in order route access is authorized
     */
    var requiredRolesAnyOf: Set<String>

    /**
     * Roles which lead to a denial of route access if they are provided in the request
     */
    var disallowedRoles: Set<String>

    /**
     * The level at which to log within the role base authorization plugin. If null, logging is disabled
     */
    var logLevel: LogLevel?
}
