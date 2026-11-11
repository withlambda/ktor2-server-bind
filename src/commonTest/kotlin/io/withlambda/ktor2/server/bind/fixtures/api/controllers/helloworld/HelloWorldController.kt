package io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld

import io.withlambda.ktor2.server.bind.Controller
import io.withlambda.ktor2.server.bind.RouteProcessorBase
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.CreateHelloWorld.RouteProcessorImpl

/**
 * Controller interface that groups all Hello World route processors under a single [Controller].
 *
 * Implementors supply a [RouteProcessorImpl] for each of the seven supported HTTP methods
 * (POST, PUT, PATCH, DELETE, GET, HEAD, OPTIONS), all targeting the route path
 * `/helloworld/{pathParam1}/{pathParam2}`.
 *
 * The [endpoints] property is pre-implemented to return all seven processors in registration
 * order, so implementing classes only need to supply the individual processor properties.
 *
 * @see io.withlambda.ktor2.server.bind.fixtures.controllers.HelloWorldControllerImpl
 */
interface HelloWorldController : Controller {

    /** Route processor for the POST `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val createHelloWorld: RouteProcessorImpl

    /** Route processor for the PUT `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val updateHelloWorld: UpdateHelloWorld.RouteProcessorImpl

    /** Route processor for the PATCH `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val patchHelloWorld: PatchHelloWorld.RouteProcessorImpl

    /** Route processor for the DELETE `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val deleteHelloWorld: DeleteHelloWorld.RouteProcessorImpl

    /** Route processor for the GET `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val getHelloWorld: GetHelloWorld.RouteProcessorImpl

    /** Route processor for the HEAD `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val headHelloWorld: HeadHelloWorld.RouteProcessorImpl

    /** Route processor for the OPTIONS `/helloworld/{pathParam1}/{pathParam2}` endpoint. */
    val optionsHelloWorld: OptionsHelloWorld.RouteProcessorImpl

    /**
     * Returns all seven route processors in registration order:
     * create, update, patch, delete, get, head, options.
     */
    override val endpoints: List<RouteProcessorBase<*, *, *>>
        get() =
            listOf(
                createHelloWorld,
                updateHelloWorld,
                patchHelloWorld,
                deleteHelloWorld,
                getHelloWorld,
                headHelloWorld,
                optionsHelloWorld
            )
}
