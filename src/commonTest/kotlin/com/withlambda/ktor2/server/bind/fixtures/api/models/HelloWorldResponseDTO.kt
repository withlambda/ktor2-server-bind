package com.withlambda.ktor2.server.bind.fixtures.api.models

import kotlinx.serialization.Serializable

/**
 * Response body returned by all Hello World endpoints.
 *
 * Every field is populated by the corresponding [com.withlambda.ktor2.server.bind.fixtures.services.HelloWorldService]
 * handler and echoes back the values received in the request, making it straightforward to assert
 * that the framework correctly extracts path parameters, query parameters, and headers.
 *
 * @property name The name value from the request body (echoed from [HelloWorldDTO.name]), or a
 *   fixed value such as `"Test"` for endpoints that carry no request body.
 * @property count A fixed counter-value (always `1`) returned by the service.
 * @property pathParam1 The integer path parameter extracted from `{pathParam1}` in the route path.
 * @property pathParam2 The string path parameter extracted from `{pathParam2}` in the route path.
 * @property optionalQueryParam1 An optional integer query parameter; `null` when not provided.
 * @property optionalQueryParam2 An optional string query parameter; `null` when not provided.
 * @property requiredQueryParam1 A mandatory integer query parameter.
 * @property requiredQueryParam2 A mandatory string query parameter.
 * @property requestHeader1 An optional integer request header; `null` when not present.
 * @property requestHeader2 A mandatory string request header.
 */
@Serializable
data class HelloWorldResponseDTO(
    val name: String,
    val count: Int,
    val pathParam1: Int,
    val pathParam2: String,
    val optionalQueryParam1: Int?,
    val optionalQueryParam2: String?,
    val requiredQueryParam1: Int,
    val requiredQueryParam2: String,
    val requestHeader1: Int?,
    val requestHeader2: String
)
