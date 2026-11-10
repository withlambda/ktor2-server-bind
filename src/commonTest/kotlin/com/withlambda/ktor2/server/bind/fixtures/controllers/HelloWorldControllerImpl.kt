package com.withlambda.ktor2.server.bind.fixtures.controllers

import com.withlambda.ktor2.server.bind.config.auth.AUTHENTICATION_CONFIGURATION_DEFAULT
import com.withlambda.ktor2.server.bind.config.auth.AuthenticationConfiguration
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.CreateHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.DeleteHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.GetHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.HeadHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.HelloWorldController
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.OptionsHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.PatchHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.UpdateHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.services.HelloWorldService

/**
 * Concrete test implementation of [HelloWorldController] that wires each route processor to the
 * corresponding [HelloWorldService] handler function.
 *
 * This singleton uses [AUTHENTICATION_CONFIGURATION_DEFAULT] as its authentication configuration
 * and delegates all business logic to [HelloWorldService], making it the entry point for the
 * full fixture stack exercised by the controller integration tests.
 */
object HelloWorldControllerImpl : HelloWorldController {

    /** Route processor for the POST `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val createHelloWorld = CreateHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::createHelloWorld
    )

    /** Route processor for the PUT `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val updateHelloWorld = UpdateHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::updateHelloWorld
    )

    /** Route processor for the PATCH `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val patchHelloWorld = PatchHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::patchHelloWorld
    )

    /** Route processor for the DELETE `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val deleteHelloWorld = DeleteHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::deleteHelloWorld
    )

    /** Route processor for the GET `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val getHelloWorld = GetHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::getHelloWorld
    )

    /** Route processor for the HEAD `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val headHelloWorld = HeadHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::headHelloWorld
    )

    /** Route processor for the OPTIONS `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    override val optionsHelloWorld = OptionsHelloWorld.RouteProcessorImpl(
        processRequest = HelloWorldService::optionsHelloWorld
    )

    /** Uses the default authentication configuration (authentication required, no custom provider name). */
    override val authenticationConfiguration: AuthenticationConfiguration = AUTHENTICATION_CONFIGURATION_DEFAULT
}
