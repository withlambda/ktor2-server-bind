package com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld

import com.withlambda.ktor2.server.bind.HeadRouteConfig
import com.withlambda.ktor2.server.bind.HeadRouteProcessor
import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.fixtures.EmptyRequestCookies
import com.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldResponseDTO
import com.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import com.withlambda.ktor2.server.bind.request.PathParameters
import com.withlambda.ktor2.server.bind.request.RequestCookies
import com.withlambda.ktor2.server.bind.request.RequestData
import com.withlambda.ktor2.server.bind.request.RequestHeaders
import com.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import com.withlambda.ktor2.server.bind.request.mapping.HeadRequestMapping
import com.withlambda.ktor2.server.bind.response.ResponseCookies
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
 * Route configuration and nested types for the HEAD `/helloworld/{pathParam1}/{pathParam2}` endpoint.
 *
 * Implements [HeadRouteConfig] and declares all inner classes needed for the head operation:
 * path parameters, optional/required query parameters, request headers, response headers, and
 * response data. Per HTTP HEAD semantics no response body is sent, and the [RouteProcessorImpl]
 * does not override `respondWithBody`.
 *
 * Role-based authorization is enabled and requires **both** `headHelloWorld1` and
 * `headHelloWorld2` roles to be present.
 */
object HeadHelloWorld : HeadRouteConfig {
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
     * Optional query parameters for the head endpoint.
     *
     * @property optionalQueryParam1 An optional integer query parameter; `null` when absent.
     * @property optionalQueryParam2 An optional string query parameter; `null` when absent.
     */
    class OptionalQueryParametersImpl(
        val optionalQueryParam1: Int?,
        val optionalQueryParam2: String?
    ) : OptionalQueryParameters

    /**
     * Required query parameters for the head endpoint.
     *
     * @property requiredQueryParam1 A mandatory integer query parameter.
     * @property requiredQueryParam2 A mandatory string query parameter.
     */
    class RequiredQueryParametersImpl(
        val requiredQueryParam1: Int,
        val requiredQueryParam2: String
    ) : RequiredQueryParameters

    /**
     * Request headers for the head endpoint.
     *
     * @property header1 An optional integer request header.
     * @property header2 A mandatory string request header.
     */
    class RequestHeadersImpl(
        val header1: Int?,
        val header2: String
    ) : RequestHeaders

    /**
     * Aggregated request data for the head endpoint. Carries no request body.
     *
     * @property pathParameter Extracted path parameters.
     * @property optionalQueryParameter Extracted optional query parameters.
     * @property requiredQueryParameter Extracted required query parameters.
     * @property headers Extracted request headers.
     * @property cookies Extracted request cookies.
     */
    class RequestDataImpl(
        override val pathParameter: PathParametersImpl,
        override val optionalQueryParameter: OptionalQueryParametersImpl,
        override val requiredQueryParameter: RequiredQueryParametersImpl,
        override val headers: RequestHeadersImpl,
        override val cookies: RequestCookies
    ) : RequestData

    /**
     * Response headers returned by the head endpoint.
     *
     * @property responseHeader1 An integer response header value.
     * @property responseHeader2 A string response header value.
     */
    class ResponseHeadersImpl(
        val responseHeader1: Int,
        val responseHeader2: String,
    ) : ResponseHeaders {

        override val mapOfHeaders: Map<String, Any> = mapOf(
            "responseHeader1" to responseHeader1,
            "responseHeader2" to responseHeader2
        )
    }

    /**
     * Response data for the head endpoint. The [responseBody] is always `null` per HEAD semantics.
     *
     * @property httpStatusCode The HTTP status code to respond with.
     * @property responseBody Always `null`; HEAD responses carry no body.
     * @property responseMessage Optional plain-text response message.
     * @property responseHeaders Optional response headers to append.
     * @property responseCookies Optional response cookies to append.
     */
    class ResponseDataImpl(
        override val httpStatusCode: HttpStatusCode,
        override val responseBody: HelloWorldResponseDTO?,
        override val responseMessage: String?,
        override val responseHeaders: ResponseHeadersImpl?,
        override val responseCookies: ResponseCookies?
    ) : ResponseDataWithBody<HelloWorldResponseDTO>

    /** Defines the HEAD method and route path for this endpoint. */
    override val requestMapping = HeadRequestMapping(
        name = "headHelloWorld",
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
     * Extracts and returns [EmptyRequestCookies] from the current call's request cookies.
     */
    override suspend fun ApplicationCall.getRequestCookies(): RequestCookies = EmptyRequestCookies

    /**
     * Enables role-based authorization requiring **both** `headHelloWorld1` and
     * `headHelloWorld2` roles to be present in the caller's
     * [com.withlambda.ktor2.server.bind.config.auth.PrincipalRoles].
     */
    override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl(
        enabled = true,
        requiredRolesAllOf = setOf("headHelloWorld1", "headHelloWorld2")
    )

    /**
     * Concrete [HeadRouteProcessor] for the head Hello World endpoint.
     *
     * Accepts an injected [processRequest] lambda so that the service function can be swapped
     * during testing without subclassing. Does not override `respondWithBody` since HEAD
     * responses must not include a body.
     *
     * @property requestMapping The HEAD request mapping; defaults to [HeadHelloWorld.requestMapping].
     * @property roleBasedAuthorizationConfiguration Authorization config; defaults to
     *   [HeadHelloWorld.roleBasedAuthorizationConfiguration].
     * @property processRequest Suspending function that receives the pipeline context and the
     *   fully-bound [RequestDataImpl] and returns a [ResponseDataImpl].
     */
    class RouteProcessorImpl(
        override val requestMapping: HeadRequestMapping = HeadHelloWorld.requestMapping,
        override val roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfigurationImpl =
            HeadHelloWorld.roleBasedAuthorizationConfiguration,
        override val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(
            RequestDataImpl
        ) -> ResponseDataImpl
    ) : HeadRouteProcessor<RequestDataImpl, ResponseDataImpl> {

        /**
         * Assembles a [RequestDataImpl] by extracting path parameters, query parameters,
         * and request headers from the current call (no request body).
         */
        override suspend fun ApplicationCall.prepareRequestData(): RequestDataImpl {
            return RequestDataImpl(
                getPathParameters(),
                getOptionalQueryParameters(),
                getRequiredQueryParameters(),
                getRequestHeaders(),
                getRequestCookies()
            )
        }
    }
}
