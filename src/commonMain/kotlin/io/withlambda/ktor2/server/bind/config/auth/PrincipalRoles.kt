package io.withlambda.ktor2.server.bind.config.auth

import io.ktor.server.auth.Principal

/**
 * Principal to store authorization roles
 * retrieved from the request and checked
 * via the RolesBasedAuthorization plugin
 * @param roles The authorization roles retrieved
 * from the request
 */
class PrincipalRoles(val roles: Set<String>) : Principal
