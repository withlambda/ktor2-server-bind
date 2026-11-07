@file:Suppress("UNUSED")
package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfiguration
import com.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import com.withlambda.ktor2.server.bind.request.PathParameters
import com.withlambda.ktor2.server.bind.request.RequestCookies
import com.withlambda.ktor2.server.bind.request.RequestHeaders
import com.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import com.withlambda.ktor2.server.bind.request.mapping.DeleteRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.GetRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.HeadRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.OptionsRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.PatchRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.PostRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.PutRequestMapping
import com.withlambda.ktor2.server.bind.request.mapping.RequestMapping
import io.konform.validation.Validation
import io.ktor.server.application.ApplicationCall
import io.ktor.util.converters.ConversionService

/**
 * Interface for the storage of the route configuration
 * @property requestMapping Base request information for this route
 * @property conversionService The conversion service to be used to convert values to target types
 * @property roleBasedAuthorizationConfiguration The role-based authorization configuration for this route
 * @property ApplicationCall.getPathParameters Method to retrieve the path parameters marked by [PathParameters] from
 * the [ApplicationCall]
 * @property ApplicationCall.getOptionalQueryParameters Method to retrieve the optional query parameters marked
 * by [OptionalQueryParameters] from the [ApplicationCall]
 * @property ApplicationCall.getRequiredQueryParameters Method to retrieve the required query parameters marked
 * by [RequiredQueryParameters] from the [ApplicationCall]
 * @property ApplicationCall.getRequestHeaders Method to retrieve the request headers marked by [RequestHeaders] from
 * the [ApplicationCall]
 */
@Suppress("EmptyMethod", "EmptyMethod")
sealed interface RouteConfig<T> {
    /**
     * The validation rules for this route
     */
    val validation: Validation<T>?

    /**
     * Base request information for this route
     */
    val requestMapping: RequestMapping

    /**
     * The conversion service to be used to convert values to target types
     */
    val conversionService: ConversionService

    /**
     * The role-based authorization configuration for this route
     */
    val roleBasedAuthorizationConfiguration: RoleBasedAuthorizationConfiguration

    /**
     * Method to retrieve the path parameters marked by [PathParameters] from the [ApplicationCall]
     */
    suspend fun ApplicationCall.getPathParameters(): PathParameters

    /**
     * Method to retrieve the optional query parameters marked by [OptionalQueryParameters] from the [ApplicationCall]
     */
    suspend fun ApplicationCall.getOptionalQueryParameters(): OptionalQueryParameters

    /**
     * Method to retrieve the required query parameters marked by [RequiredQueryParameters] from the [ApplicationCall]
     */
    suspend fun ApplicationCall.getRequiredQueryParameters(): RequiredQueryParameters

    /**
     * Method to retrieve the request headers marked by [RequestHeaders] from the [ApplicationCall]
     */
    suspend fun ApplicationCall.getRequestHeaders(): RequestHeaders

    /**
     * Method to retrieve the request cookies marked by [RequestCookies] from the [ApplicationCall]
     */
    suspend fun ApplicationCall.getRequestCookies(): RequestCookies
}

/**
 * Interface for the storage of the route configuration for a GET route
 */
interface GetRouteConfig : RouteConfig<Unit> {
    override val requestMapping: GetRequestMapping
    override val validation: Validation<Unit>?
        get() = null
}

/**
 * Interface for the storage of the route configuration for a POST route
 */
interface PostRouteConfig<T> : RouteConfig<T> {
    override val requestMapping: PostRequestMapping
}

/**
 * Interface for the storage of the route configuration for a PUT route
 */
interface PutRouteConfig<T> : RouteConfig<T> {
    override val requestMapping: PutRequestMapping
}

/**
 * Interface for the storage of the route configuration for a DELETE route
 */
interface DeleteRouteConfig : RouteConfig<Unit> {
    override val requestMapping: DeleteRequestMapping
    override val validation: Validation<Unit>?
        get() = null
}

/**
 * Interface for the storage of the route configuration for a PATCH route
 */
interface PatchRouteConfig<T> : RouteConfig<T> {
    override val requestMapping: PatchRequestMapping
}

/**
 * Interface for the storage of the route configuration for a HEAD route
 */
interface HeadRouteConfig : RouteConfig<Unit> {
    override val requestMapping: HeadRequestMapping
    override val validation: Validation<Unit>?
        get() = null
}

/**
 * Interface for the storage of the route configuration for an OPTIONS route
 */
interface OptionsRouteConfig : RouteConfig<Unit> {
    override val requestMapping: OptionsRequestMapping
    override val validation: Validation<Unit>?
        get() = null
}
