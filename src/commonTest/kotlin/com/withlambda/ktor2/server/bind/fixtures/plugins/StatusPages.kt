package com.withlambda.ktor2.server.bind.fixtures.plugins

import com.withlambda.ktor2.server.bind.exception.ExceptionHandling
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages

/**
 * Installs the Ktor [StatusPages] plugin on the test application with predefined bad-request
 * exception handlers.
 *
 * Delegates to [ExceptionHandling.predefinedBadRequestExceptionsHandler] to register handlers for
 * framework exceptions such as missing or unconvertible path/query parameters and headers,
 * returning HTTP 400 responses with descriptive error messages.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        with(ExceptionHandling) {
            predefinedBadRequestExceptionsHandler()
        }
    }
}
