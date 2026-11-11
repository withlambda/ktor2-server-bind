package io.withlambda.ktor2.server.bind

/**
 * Annotates a [RouteConfig] property inside an [@ApiController][ApiController]-marked class
 * to supply the HTTP path that the KSP processor cannot read from the runtime-constructed
 * [io.withlambda.ktor2.server.bind.request.mapping.RequestMapping] initialiser.
 *
 * @param path The URL path of the endpoint (e.g. `"/users/{id}"`).
 * @param summary A short human-readable description used as the OpenAPI `summary` field.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ApiEndpoint(val path: String, val summary: String = "")
