package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.AuthenticationConfiguration
import com.withlambda.ktor2.server.bind.exception.ExceptionHandlerBuilder
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route

/**
 * Controller interface
 * that specifies all endpoints of the controller
 * and allows to install all the endpoints
 *
 * @property endpoints List of all endpoints of the controller [Controller]
 * @property authenticationConfiguration Configuration of authentication for the controller
 * @property exceptionHandler Optional DSL block applied before global StatusPages handling; register
 * typed exception handlers with [com.withlambda.ktor2.server.bind.exception.exception]. When `null` all exceptions
 * propagate to the global StatusPages plugin.
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
     * Optional DSL block for controller-scoped exception handling. Register typed handlers using
     * [com.withlambda.ktor2.server.bind.exception.exception]. When non-null, exceptions thrown during request
     * processing are checked against registered handlers before falling back to the global
     * [io.ktor.server.plugins.statuspages.StatusPages] plugin. Defaults to `null`.
     */
    val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit)?
        get() = null

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
                @Suppress("SpreadOperator")
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
                with(endpoint) { install(exceptionHandler) }
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
