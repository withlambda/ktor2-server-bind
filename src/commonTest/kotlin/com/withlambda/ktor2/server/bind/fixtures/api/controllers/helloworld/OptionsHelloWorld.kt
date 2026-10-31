package com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld

import com.withlambda.ktor2.server.bind.OptionsRouteConfig
import com.withlambda.ktor2.server.bind.OptionsRouteProcessor
import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldResponseDTO
import com.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import com.withlambda.ktor2.server.bind.request.PathParameters
import com.withlambda.ktor2.server.bind.request.RequestData
import com.withlambda.ktor2.server.bind.request.RequestHeaders
import com.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import com.withlambda.ktor2.server.bind.request.mapping.OptionsRequestMapping
import com.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import com.withlambda.ktor2.server.bind.response.ResponseHeaders
import com.withlambda.ktor2.server.bind.util.OptionalHeadersHandler
import com.withlambda.ktor2.server.bind.util.OptionalParametersHandler
import com.withlambda.ktor2.server.bind.util.RequiredHeadersHandler
import com.withlambda.ktor2.server.bind.util.RequiredParametersHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.pipeline.PipelineContext

/**
 * Route configuration and nested types for the OPTIONS `/helloworld/{pathParam1}/{pathParam2}` endpoint.
 *
 * Implements [OptionsRouteConfig] and declares all inner classes needed for the options operation:
 * path parameters, optional/required query parameters, request headers, response headers (including
 * the `Allow` header), response data, and the [RouteProcessorImpl] that ties them together.
 * This endpoint carries no request body and the [RouteProcessorImpl] does not override
 * `respondWithBody`.
 *
 * Role-based authorization is enabled and requires **both** `optionsHelloWorld1` and
 * `optionsHelloWorld2` roles to be present.
 */
object OptionsHelloWorld : OptionsRouteConfig {
    /**
     * Path parameters extracted from the route `/helloworld/{pathParam1}/{pathParam2}`.
     *
     * @property pathParam1 Integer segment at `{pathParam1}`.
     * @property pathParam2 String segment at `{pathParam2}`.
     */
    class PathParametersImpl(
        val pathParam1: Int,
        val pathParam2: String
    ) : PathParameters

    /**
     * Optional query parameters for the options endpoint.
     *
     * @property optionalQueryParam1 An optional integer query parameter; `null` when absent.
     * @property optionalQueryParam2 An optional string query parameter; `null` when absent.
     */
    class OptionalQueryParametersImpl(
        val optionalQueryParam1: Int?,
        val optionalQueryParam2: String?
    ) : OptionalQueryParameters

    /**
     * Required query parameters for the options endpoint.
     *
     * @property requiredQueryParam1 A mandatory integer query parameter.
     * @property requiredQueryParam2 A mandatory string query parameter.
     */
    class RequiredQueryParametersImpl(
        val requiredQueryParam1: Int,
        val requiredQueryParam2: String
    ) : RequiredQueryParameters

    /**
     * Request headers for the options endpoint.
     *
     * @property header1 An optional integer request header.
     * @property header2 A mandatory string request header.
     */
    class RequestHeadersImpl(
        val header1: Int?,
        val header2: String
    ) : RequestHeaders

    /**
     * Aggregated request data for the options endpoint. Carries no request body.
     *
     * @property pathParameter Extracted path parameters.
     * @property optionalQueryParameter Extracted optional query parameters.
     * @property requiredQueryParameter Extracted required query parameters.
     * @property headers Extracted request headers.
     */
    class RequestDataImpl(
        override val pathParameter: PathParametersImpl,
        override val optionalQueryParameter: OptionalQueryParametersImpl,
        override val requiredQueryParameter: RequiredQueryParametersImpl,
        override val headers: RequestHeadersImpl,
    ) : RequestData

    /**
     * Response headers returned by the options endpoint.
     *
     * @property responseHeader1 An integer response header value.
     * @property responseHeader2 A string response header value.
     * @property allow Comma-separated list of HTTP methods supported by the resource,
     *   sent as the standard `Allow` response header.
     */
    class ResponseHeadersImpl(
        val responseHeader1: Int,
        val responseHeader2: String,
        val allow: String
    ) : ResponseHeaders {

        override val mapOfHeaders: Map<String, Any> = mapOf(
            "responseHeader1" to responseHeader1,
            "responseHeader2" to responseHeader2,
            "Allow" to allow
        )
    }

    /**
     * Response data for the options endpoint. The [responseBody] is always `null`.
     *
     * @property httpStatusCode The HTTP status code to respond with (typically 204 No Content).
     * @property responseBody Always `null`; OPTIONS responses in this fixture carry no body.
     * @property responseMessage Optional plain-text response message.
     * @property responseHeaders Optional response headers (includes the `Allow` header).
     */
    class ResponseDataImpl(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: HelloWorldResponseDTO?,
        override val responseMessage: String?,
        override val responseHeaders: ResponseHeadersImpl?
    ) : ResponseDataWithBody<HelloWorldResponseDTO>

    /** Defines the OPTIONS method and route path for this endpoint. */
    override val requestMapping = OptionsRequestMapping(
        name = "optionsHelloWorld",
        path = "/helloworld/{pathParam1}/{pathParam2}",
    )

    /** Conversion service used to parse path and query parameter strings into typed values. */
    override val conversionService = DefaultConversionService

    /**
     * Extracts and returns [PathParametersImpl] from the current call's path parameters.
     * Both [PathParametersImpl.pathParam1] and [PathParametersImpl.pathParam2] are required.
     */
    override suspend fun ApplicationCall.getPathParameters(): PathParametersImpl {
        val pathParam1: Int by RequiredParametersHandler(parameters, conversionService)
        val pathParam2: String by RequiredParametersHandler(parameters, conversionService)

        return PathParametersImpl(
            pathParam1 = pathParam1,
            pathParam2 = pathParam2
        )
    }

    /**
     * Extracts and returns [OptionalQueryParametersImpl] from the current call's query parameters.
     * Both parameters are optional and will be `null` if absent.
     */
    override suspend fun ApplicationCall.getOptionalQueryParameters(): OptionalQueryParametersImpl {
        val optionalQueryParam1: Int? by OptionalParametersHandler(
            request.queryParameters,
            conversionService
        )
        val optionalQueryParam2: String? by OptionalParametersHandler(
            request.queryParameters,
            conversionService
        )

        return OptionalQueryParametersImpl(
            optionalQueryParam1 = optionalQueryParam1,
            optionalQueryParam2 = optionalQueryParam2
        )
    }

    /**
     * Extracts and returns [RequiredQueryParametersImpl] from the current call's query parameters.
     * Missing parameters will result in an exception handled by the status-pages configuration.
     */
    override suspend fun ApplicationCall.getRequiredQueryParameters(): RequiredQueryParametersImpl {
        val requiredQueryParam1: Int by RequiredParametersHandler(
            request.queryParameters,
            conversionService
        )
        val requiredQueryParam2: String by RequiredParametersHandler(
            request.queryParameters,
            conversionService
        )
        return RequiredQueryParametersImpl(
            requiredQueryParam1 = requiredQueryParam1,
            requiredQueryParam2 = requiredQueryParam2
        )
    }

    /**
     * Extracts and returns [RequestHeadersImpl] from the current call's request headers.
     * [RequestHeadersImpl.header1] is optional; [RequestHeadersImpl.header2] is required.
     */
    override suspend fun ApplicationCall.getRequestHeaders(): RequestHeadersImpl {
        val header1: Int? by OptionalHeadersHandler(request.headers, conversionService)
        val header2: String by RequiredHeadersHandler(request.headers, conversionService)

        return RequestHeadersImpl(
            header1 = header1,
            header2 = header2
        )
    }

    /**
     * Enables role-based authorization requiring **both** `optionsHelloWorld1` and
     * `optionsHelloWorld2` roles to be present in the caller's [com.withlambda.ktor2.server.bind.config.auth.PrincipalRoles].
     */
    override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAllOf = setOf("optionsHelloWorld1", "optionsHelloWorld2")
    )

    /**
     * Concrete [OptionsRouteProcessor] for the options Hello World endpoint.
     *
     * Accepts an injected [processRequest] lambda so that the service function can be swapped
     * during testing without subclassing. Does not override `respondWithBody` since the OPTIONS
     * response in this fixture carries no body.
     *
     * @property requestMapping The OPTIONS request mapping; defaults to [OptionsHelloWorld.requestMapping].
     * @property roleBasedAuthorizationConfiguration Authorization config; defaults to
     *   [OptionsHelloWorld.roleBasedAuthorizationConfiguration].
     * @property processRequest Suspending function that receives the pipeline context and the
     *   fully-bound [RequestDataImpl] and returns a [ResponseDataImpl].
     */
    class RouteProcessorImpl(
        override val requestMapping: OptionsRequestMapping = OptionsHelloWorld.requestMapping,
        override val roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfigurationImpl =
            OptionsHelloWorld.roleBasedAuthorizationConfiguration,
        override val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(
            RequestDataImpl
        ) -> ResponseDataImpl
    ) : OptionsRouteProcessor<RequestDataImpl, ResponseDataImpl> {

        /**
         * Assembles a [RequestDataImpl] by extracting path parameters, query parameters,
         * and request headers from the current call (no request body).
         */
        override suspend fun ApplicationCall.prepareRequestData(): RequestDataImpl {
            return RequestDataImpl(
                getPathParameters(),
                getOptionalQueryParameters(),
                getRequiredQueryParameters(),
                getRequestHeaders()
            )
        }
    }
}
