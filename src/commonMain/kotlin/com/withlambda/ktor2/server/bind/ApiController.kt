package com.withlambda.ktor2.server.bind

/**
 * Marks an interface or class as an API controller whose [RouteConfig] properties
 * should be picked up by the KSP processor and exported as OpenAPI endpoints.
 *
 * @param tag An optional grouping tag used to categorize the endpoints in the
 *   generated OpenAPI specification (maps to the OpenAPI `tags` field).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ApiController(val tag: String = "")
