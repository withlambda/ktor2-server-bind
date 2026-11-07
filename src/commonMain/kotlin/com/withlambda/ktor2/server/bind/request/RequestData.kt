package com.withlambda.ktor2.server.bind.request

/**
 * Interface to specify request data for a route such as path- and query parameters and headers
 * @property pathParameter Request path parameters of the route
 * @property optionalQueryParameter Request optional query parameters of the route
 * @property requiredQueryParameter Request required query parameters of the route
 * @property headers Request headers of the route
 * @property cookies Request cookies of the route
 */
interface RequestData {
    /**
     * Request path parameters of the route
     */
    val pathParameter: PathParameters

    /**
     * Request optional query parameters of the route
     */
    val optionalQueryParameter: OptionalQueryParameters

    /**
     * Request required query parameters of the route
     */
    val requiredQueryParameter: RequiredQueryParameters

    /**
     * Request headers of the route
     */
    val headers: RequestHeaders

    /**
     * Request cookies of the route
     */
    val cookies: RequestCookies
}

/**
 * Interface to specify request data for a route with request body
 * such as path- and query parameters, headers, and the request body
 * @property requestBody Request body of the route
 */
interface RequestWithBodyData<RequestBodyType> : RequestData {
    /**
     * Request body of the route
     */
    val requestBody: RequestBodyType
}
