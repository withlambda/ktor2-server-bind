package com.withlambda.ktor2.server.bind.fixtures.api.models

import kotlinx.serialization.Serializable

/**
 * Request body used by all Hello World endpoints that accept a body (POST, PUT, PATCH).
 *
 * @property name A name value included in the request body, echoed back in [HelloWorldResponseDTO.name].
 */
@Serializable
data class HelloWorldDTO(
    val name: String,
)
