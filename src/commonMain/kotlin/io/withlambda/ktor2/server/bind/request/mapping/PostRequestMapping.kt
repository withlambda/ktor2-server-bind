package io.withlambda.ktor2.server.bind.request.mapping

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod

/**
 * Storage of base request information for an endpoint with [HttpMethod.Post] method
 */
class PostRequestMapping(
    override val method: HttpMethod = HttpMethod.Post,
    override val name: String,
    override val path: String,
    override val consumes: ContentType?,
    override val produces: ContentType?
) : RequestMappingWithRequestAndResponseBody
