package io.withlambda.ktor2.server.bind.request.mapping

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod

/**
 * Storage of base request information for an endpoint with [HttpMethod.Delete] method
 */
class DeleteRequestMapping(
    override val method: HttpMethod = HttpMethod.Delete,
    override val name: String,
    override val path: String,
    override val produces: ContentType?,
) : RequestMappingWithResponseBody
