package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.AuthenticationConfiguration
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

/**
 * Controller interface
 * that specifies all endpoints of the controller
 * and allows to install all the endpoints
 *
 * @property endpoints List of all endpoints of the controller [Controller]
 * @property authenticationConfiguration Configuration of authentication for the controller
 * @property Route.auth Function to process authentication if enabled
 * @property Route.install Function to install all endpoints of the controller
 */
interface Controller {
    /**
     * List of all endpoints of the controller [Controller]
     */
    val endpoints: List<RouteProcessorBase<*, *, *>>

    /**
     * Configuration of authentication for the controller
     */
    val authenticationConfiguration: AuthenticationConfiguration

    /**
     * Function to process authentication if enabled
     * @param authenticationConfiguration The authentication configuration to be used for the specified route
     * @param build The lambda function for further route processing
     */
    fun Route.auth(
        authenticationConfiguration: AuthenticationConfiguration,
        build: Route.() -> Unit,
    ) {
        with(authenticationConfiguration) {
            if (enable) {
                authenticate(*providers, optional = optional, build = build)
            } else {
                build()
            }
        }
    }

    /**
     * Install all endpoints of this [Controller]
     */
    fun Route.install() {
        auth(authenticationConfiguration) {
            endpoints.forEach { endpoint ->
                with(endpoint) { install() }
            }
        }
    }
}

/**
 * Install all endpoints of a controller
 * @param controller The [Controller] instance of which all endpoints are to be installed
 */
fun Route.install(
    controller: Controller,
) = with(controller) { install() }
