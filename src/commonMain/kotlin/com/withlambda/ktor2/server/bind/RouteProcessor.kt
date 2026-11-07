@file:Suppress("Unused")
package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfiguration
import com.withlambda.ktor2.server.bind.plugins.roleBasedAuthorization
import com.withlambda.ktor2.server.bind.request.RequestData
import com.withlambda.ktor2.server.bind.request.RequestWithBodyData
import com.withlambda.ktor2.server.bind.request.mapping.DeleteRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.GetRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.HeadRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.OptionsRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.PatchRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.PostRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.PutRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.RequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.RequestMappingWithRequestAndResponseBody
import com.withlambda.ktor2.server.bind.request.mapping.RequestMappingWithResponseBody
import com.withlambda.ktor2.server.bind.response.ResponseData
import com.withlambda.ktor2.server.bind.response.ResponseDataWithBody
import com.withlambda.ktor2.server.bind.util.MultipartHandler
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.acceptItems
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.util.pipeline.PipelineContext

/**
 * Base interface of a route processor that stores
 * the request base information, request data, and
 * methods to process the request
 * @param SpecifiedRequestMapping The specified type of the [requestMapping]
 * @param SpecifiedRequestData The specified type of the request data
 * @param SpecifiedResponseData The specified type of the response data
 * @property requestMapping Base request information for this route
 * @property roleBasedAuthorizationConfiguration The role-based authorization configuration for this route
 * @property ApplicationCall.prepareRequestData Method to prepare the request data, marked by [SpecifiedRequestData],
 * of the route from [ApplicationCall]
 * @property processRequest Lambda method, stored in a constant, that process the request and produces the
 * response data which is marked by [SpecifiedResponseData]
 * @property ApplicationCall.customizedResponse Method that takes the responseData and prepares the call response
 * @property PipelineContext.isAcceptHeaderOfRequestValid Method to check whether the 'accept' header values of the
 * request are compatible with the `produces` property of the [SpecifiedRequestMapping]. If not compatible, a call
 * response with status [HttpStatusCode.NotAcceptable] is prepared and further processing stopped. Note that if
 * [SpecifiedRequestData] does not contain the `produces` argument, the method will always return true, indicating
 * validity
 * @property PipelineContext.isContentTypeHeaderOfRequestValid Method to check whether the contentType header value
 * of the request is compatible with the `consumes` property of the [SpecifiedRequestMapping]. If not compatible,
 * a call response with status [HttpStatusCode.UnsupportedMediaType] is prepared and further processing stopped.
 * Note that if [SpecifiedRequestData] does not contain the `consumes` argument, the method will always return true,
 * indicating validity
 * @property Route.install Method to install this route
 */
sealed interface RouteProcessorBase<
    SpecifiedRequestMapping : RequestMapping,
    SpecifiedRequestData : RequestData,
    SpecifiedResponseData : ResponseData
    > {

    /**
     * Base request information for this route
     */
    val requestMapping: SpecifiedRequestMapping

    /**
     * The role-based authorization configuration for this route
     */
    val roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfiguration

    /**
     * Method to prepare the request data, marked by [SpecifiedRequestData], of the route from [ApplicationCall]
     */
    suspend fun ApplicationCall.prepareRequestData(): SpecifiedRequestData

    /**
     * Lambda method, stored in a constant, that process the request and produces the response data which is marked
     * by [SpecifiedResponseData]
     */
    val processRequest: suspend PipelineContext<Unit, ApplicationCall>.(
        SpecifiedRequestData
    ) -> SpecifiedResponseData

    /**
     * Method that takes the responseData and prepares the call response
     * @param responseData The response data which is used as the basis to generate the call response
     */
    suspend fun ApplicationCall.customizedResponse(responseData: SpecifiedResponseData) {
        with(responseData) {
            responseHeaders?.mapOfHeaders?.forEach { (name, value) ->
                response.headers.append(name, value.toString())
            }

            responseCookies?.mapOfCookies?.values?.forEach {
                response.cookies.append(it)
            }

            when {
                responseMessage != null -> respondText(
                    status = httpStatusCode,
                    text = responseMessage!!
                )

                else -> respond(httpStatusCode)
            }
        }
    }

    /**
     * Method to check whether the 'accept' header values of the
     * request are compatible with the `produces` property of the [SpecifiedRequestMapping]. If not compatible,
     * a call response with status [HttpStatusCode.NotAcceptable] is prepared and further processing stopped.
     * Note that if [SpecifiedRequestData] does not contain the `produces` argument, the method will always return
     * true, indicating validity
     */
    suspend fun PipelineContext<Unit, ApplicationCall>.isAcceptHeaderOfRequestValid() = true

    /**
     * PipelineContext.isContentTypeHeaderOfRequestValid Method to check whether the contentType header value
     * of the request is compatible with the `consumes` property of the [SpecifiedRequestMapping]. If not compatible,
     * a call response with status [HttpStatusCode.UnsupportedMediaType] is prepared and further processing stopped.
     * Note that if [SpecifiedRequestData] does not contain the `consumes` argument, the method will always return true,
     * indicating validity
     */
    suspend fun PipelineContext<Unit, ApplicationCall>.isContentTypeHeaderOfRequestValid() = true

    /**
     * Method to install this route
     */
    fun Route.install() {
        with(requestMapping) {
            route(path = path, method = method) {
                roleBasedAuthorization(roleBasedAuthorizationConfiguration)
                handle {
                    listOf(
                        isAcceptHeaderOfRequestValid(),
                        isContentTypeHeaderOfRequestValid()
                    ).takeIf { isValidResults -> isValidResults.all { it } } ?: return@handle
                    val request = call.prepareRequestData()
                    call.customizedResponse(processRequest(request))
                }
            }
        }
    }
}

/**
 * Interface of a route processor for a route with a response body that stores
 * the request base information, request data, and methods to process the request
 * @param SpecifiedRequestMapping The specified type of the [requestMapping]
 * @param SpecifiedRequestData The specified type of the request data
 * @param ResponseBodyType The type of the produced response body
 * @param SpecifiedResponseData The specified type of the response data
 * @property requestMapping Base request information for this route
 * @property roleBasedAuthorizationConfiguration The role-based authorization configuration for this route
 * @property ApplicationCall.prepareRequestData Method to prepare the request data, marked by [SpecifiedRequestData],
 * of the route from [ApplicationCall]
 * @property processRequest Lambda method, stored in a constant, that process the request and produces the
 * response data which is marked by [SpecifiedResponseData]
 * @property ApplicationCall.customizedResponse Method that takes the responseData and prepares the call response
 * @property ApplicationCall.respondWithBody Method that prepares a call response with specified [HttpStatusCode]
 * and responseBody of type [ResponseBodyType]
 * @property PipelineContext.isAcceptHeaderOfRequestValid Method to check whether the 'accept' header values of the
 * request are compatible with the `produces` property of the [SpecifiedRequestMapping]. If not compatible, a call
 * response with status [HttpStatusCode.NotAcceptable] is prepared and further processing stopped. Note that if
 * [SpecifiedRequestData] does not contain the `produces` argument, the method will always return true, indicating
 * validity
 * @property PipelineContext.isContentTypeHeaderOfRequestValid Method to check whether the contentType header value
 * of the request is compatible with the `consumes` property of the [SpecifiedRequestMapping]. If not compatible,
 * a call response with status [HttpStatusCode.UnsupportedMediaType] is prepared and further processing stopped.
 * Note that if [SpecifiedRequestData] does not contain the `consumes` argument, the method will always return true,
 * indicating validity
 * @property Route.install Method to install this route
 */
sealed interface RouteProcessor<
    SpecifiedRequestMapping : RequestMappingWithResponseBody,
    SpecifiedRequestData : RequestData,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteProcessorBase<SpecifiedRequestMapping, SpecifiedRequestData, SpecifiedResponseData> {

    /**
     * Method that prepares a call response with specified [HttpStatusCode] and responseBody of type [ResponseBodyType]
     */
    suspend fun ApplicationCall.respondWithBody(httpStatusCode: HttpStatusCode, responseBody: ResponseBodyType)

    override suspend fun PipelineContext<Unit, ApplicationCall>.isAcceptHeaderOfRequestValid() =
        requestMapping.produces?.let { produces ->
            val requestAcceptHeaders = call.request.acceptItems().map { ContentType.parse(it.value) }
            val isValid = setOf(ContentType.Any, produces).any { requestAcceptHeaders.contains(it) }
            if (isValid.not()) call.respond(HttpStatusCode.NotAcceptable)
            isValid
        } ?: true

    override suspend fun ApplicationCall.customizedResponse(responseData: SpecifiedResponseData) {
        with(responseData) {
            responseHeaders?.mapOfHeaders?.forEach { (name, value) ->
                response.headers.append(name, value.toString())
            }

            responseCookies?.mapOfCookies?.values?.forEach {
                response.cookies.append(it)
            }

            when {
                responseBody != null -> respondWithBody(httpStatusCode, responseBody!!)

                responseMessage != null -> respondText(
                    status = httpStatusCode,
                    text = responseMessage!!
                )

                else -> respond(httpStatusCode)
            }
        }
    }
}

/**
 * Interface of a route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 * @param SpecifiedRequestMapping The specified type of the [requestMapping]
 * @param RequestBodyType The type of the request body
 * @param SpecifiedRequestData The specified type of the request data
 * @param ResponseBodyType The type of the produced response body
 * @param SpecifiedResponseData The specified type of the response data
 * @property requestMapping Base request information for this route
 * @property roleBasedAuthorizationConfiguration The role-based authorization configuration for this route
 * @property ApplicationCall.prepareRequestData Method to prepare the request data, marked by [SpecifiedRequestData],
 * of the route from [ApplicationCall]
 * @property processRequest Lambda method, stored in a constant, that process the request and produces the
 * response data which is marked by [SpecifiedResponseData]
 * @property ApplicationCall.customizedResponse Method that takes the responseData and prepares the call response
 * @property ApplicationCall.respondWithBody Method that prepares a call response with specified [HttpStatusCode]
 * and responseBody of type [ResponseBodyType]
 * @property PipelineContext.isAcceptHeaderOfRequestValid Method to check whether the 'accept' header values of the
 * request are compatible with the `produces` property of the [SpecifiedRequestMapping]. If not compatible, a call
 * response with status [HttpStatusCode.NotAcceptable] is prepared and further processing stopped. Note that if
 * [SpecifiedRequestData] does not contain the `produces` argument, the method will always return true, indicating
 * validity
 * @property PipelineContext.isContentTypeHeaderOfRequestValid Method to check whether the contentType header value
 * of the request is compatible with the `consumes` property of the [SpecifiedRequestMapping]. If not compatible,
 * a call response with status [HttpStatusCode.UnsupportedMediaType] is prepared and further processing stopped.
 * Note that if [SpecifiedRequestData] does not contain the `consumes` argument, the method will always return true,
 * indicating validity
 * @property Route.install Method to install this route
 */
sealed interface RouteWithRequestBodyProcessor<
    SpecifiedRequestMapping : RequestMappingWithRequestAndResponseBody,
    RequestBodyType,
    SpecifiedRequestData : RequestWithBodyData<RequestBodyType>,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteProcessor<SpecifiedRequestMapping, SpecifiedRequestData, ResponseBodyType, SpecifiedResponseData> {

    override suspend fun PipelineContext<Unit, ApplicationCall>.isContentTypeHeaderOfRequestValid(): Boolean =
        requestMapping.consumes?.let { consumes ->
            val requestContentType = call.request.contentType()
            val isValid = setOf(ContentType.Any, consumes).any { requestContentType.match(it) }
            if (isValid.not()) call.respond(HttpStatusCode.UnsupportedMediaType)
            isValid
        } ?: true
}

/**
 * Interface of a GET route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface GetRouteProcessor<
    SpecifiedRequestData : RequestData,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteProcessor<GetRequestMapping, SpecifiedRequestData, ResponseBodyType, SpecifiedResponseData>

/**
 * Interface of a POST route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface PostRouteProcessor<
    RequestBodyType,
    SpecifiedRequestData : RequestWithBodyData<RequestBodyType>,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteWithRequestBodyProcessor<
    PostRequestMapping,
    RequestBodyType,
    SpecifiedRequestData,
    ResponseBodyType,
    SpecifiedResponseData
    >

/**
 * Interface of a PUT route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface PutRouteProcessor<
    RequestBodyType,
    SpecifiedRequestData : RequestWithBodyData<RequestBodyType>,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteWithRequestBodyProcessor<
    PutRequestMapping,
    RequestBodyType,
    SpecifiedRequestData,
    ResponseBodyType,
    SpecifiedResponseData
    >

/**
 * Interface of a PATCH route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface PatchRouteProcessor<
    RequestBodyType,
    SpecifiedRequestData : RequestWithBodyData<RequestBodyType>,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteWithRequestBodyProcessor<
    PatchRequestMapping,
    RequestBodyType,
    SpecifiedRequestData,
    ResponseBodyType,
    SpecifiedResponseData
    >

/**
 * Interface of a DELETE route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface DeleteRouteProcessor<
    SpecifiedRequestData : RequestData,
    ResponseBodyType,
    SpecifiedResponseData : ResponseDataWithBody<ResponseBodyType>
    > : RouteProcessor<DeleteRequestMapping, SpecifiedRequestData, ResponseBodyType, SpecifiedResponseData>

/**
 * Interface of a HEAD route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface HeadRouteProcessor<
    SpecifiedRequestData : RequestData,
    SpecifiedResponseData : ResponseData
    > : RouteProcessorBase<HeadRequestMapping, SpecifiedRequestData, SpecifiedResponseData>

/**
 * Interface of an OPTIONS route processor for a route with request and response body that stores
 * the request base information, request data, and methods to process the request
 */
interface OptionsRouteProcessor<
    SpecifiedRequestData : RequestData,
    SpecifiedResponseData : ResponseData
    > : RouteProcessorBase<OptionsRequestMapping, SpecifiedRequestData, SpecifiedResponseData>

/**
 * Extension to receive multipart data and return a map of parts.
 */
suspend fun ApplicationCall.receiveMultipartData(): Map<String, Any> {
    val multipart = receiveMultipart()
    return MultipartHandler(multipart).parseAllParts()
}
