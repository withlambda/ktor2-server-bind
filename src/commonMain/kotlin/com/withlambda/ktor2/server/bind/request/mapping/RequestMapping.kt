package com.withlambda.ktor2.server.bind.request.mapping

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod

/**
 * Interface of base request information for this route
 * @property method Http method of the route
 * @property name Name of the endpoint
 * @property path Path of the endpoint
 */
interface RequestMapping {
    /**
     * Http method of the route
     */
    val method: HttpMethod

    /**
     * Name of the endpoint
     */
    val name: String

    /**
     * Path of the endpoint
     */
    val path: String
}

/**
 * Interface of base request information of a route which specifies a response body
 * @property produces The [ContentType] of the defined response body of the route
 */
interface RequestMappingWithResponseBody : RequestMapping {
    /**
     * The [ContentType] of the defined response body of the route
     */
    val produces: ContentType?
}

/**
 * Interface of base request information of a route which specifies a request and response body
 * @property consumes The [ContentType] of the defined request body of the route
 */
interface RequestMappingWithRequestAndResponseBody : RequestMappingWithResponseBody {
    /**
     * The [ContentType] of the defined request body of the route
     */
    val consumes: ContentType?
}
