package com.withlambda.ktor2.server.bind.config.auth

/**
 * Authentication configuration interface
 * @property enable Toggle to enable or disable authentication for a Controller
 * @property providers Names of authentication providers defined in the Authentication plugin configuration
 * @property optional When [optional] is true, if the client provides no authentication,
 * a call continues but with a null Principal
 */
@Suppress("UNUSED")
interface AuthenticationConfiguration {
    /**
     * Toggle to enable or disable authentication
     * for a Controller
     */
    val enable: Boolean

    /**
     * Names of authentication providers defined in the Authentication plugin configuration
     */
    val providers: Array<String?>

    /**
     * When [optional] is true, if the client provides no authentication,
     * a call continues but with a null Principal.
     */
    val optional: Boolean
}

val AUTHENTICATION_CONFIGURATION_DEFAULT = object : AuthenticationConfiguration {
    override val enable: Boolean = true
    override val providers: Array<String?> = arrayOf(null)
    override val optional: Boolean = false
}
