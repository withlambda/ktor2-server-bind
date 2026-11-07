package com.withlambda.ktor2.server.bind.config.auth

import com.withlambda.ktor2.server.bind.config.LogLevel
import com.withlambda.ktor2.server.bind.config.Logging

/**
 * Implementation of [RoleBasedAuthorizationConfiguration]
 * to store role based authorization configuration
 */
class RoleBasedAuthorizationConfigurationImpl(
    override var enabled: Boolean = true,
    override var requiredRolesAllOf: Set<String> = emptySet(),
    override var requiredRolesAnyOf: Set<String> = emptySet(),
    override var disallowedRoles: Set<String> = emptySet(),
    override var logLevel: LogLevel? = Logging.LogLevel.Plugin.roleBasedAuthorization
) : RoleBasedAuthorizationConfiguration
