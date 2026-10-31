package com.withlambda.ktor2.server.bind.fixtures.plugins

import com.withlambda.ktor2.server.bind.fixtures.controllers.HelloWorldControllerImpl
import com.withlambda.ktor2.server.bind.install
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

/**
 * Configures routing for the test application by installing [HelloWorldControllerImpl].
 *
 * All Hello World endpoints (GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS) are registered under
 * the `/helloworld/{pathParam1}/{pathParam2}` path via the library's [install] extension.
 */
fun Application.configureRouting() {
    routing {
        install(HelloWorldControllerImpl)
    }
}
