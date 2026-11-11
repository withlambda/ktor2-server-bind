package io.withlambda.ktor2.server.bind.fixtures.plugins

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.withlambda.ktor2.server.bind.fixtures.controllers.HelloWorldControllerImpl
import io.withlambda.ktor2.server.bind.install

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
