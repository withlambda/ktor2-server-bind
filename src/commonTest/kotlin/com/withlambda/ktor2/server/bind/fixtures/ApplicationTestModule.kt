@file:Suppress("UNUSED")
package com.withlambda.ktor2.server.bind.fixtures
import com.withlambda.ktor2.server.bind.fixtures.plugins.configureAuth
import com.withlambda.ktor2.server.bind.fixtures.plugins.configureContentNegotiation
import com.withlambda.ktor2.server.bind.fixtures.plugins.configureRouting
import com.withlambda.ktor2.server.bind.fixtures.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication

/**
 * Launches a [testApplication] configured with the full Hello World fixture stack and executes
 * [test] within it.
 *
 * The application module installed is [Application.testModule], which wires authentication,
 * content negotiation, routing, and status-page exception handling. Use this function as the
 * entry point for integration tests that need a running Ktor server.
 *
 * @param test A suspending block that receives an [ApplicationTestBuilder] and contains the
 *   HTTP client calls and assertions for the test scenario.
 */
fun withCustomTestApplication(
    test: suspend ApplicationTestBuilder.() -> Unit = {}
) {
    testApplication {
        application { testModule() }
        test()
    }
}

/**
 * Applies the full Hello World fixture plugin stack to this [Application].
 *
 * The following plugins are installed in order:
 * 1. Authentication — token-based Bearer auth via [configureAuth].
 * 2. Content negotiation — JSON serialization via [configureContentNegotiation].
 * 3. Routing — Hello World endpoints via [configureRouting].
 * 4. Status pages — bad-request exception handlers via [configureStatusPages].
 */
fun Application.testModule() {
    configureAuth()
    configureContentNegotiation()
    configureRouting()
    configureStatusPages()
}
