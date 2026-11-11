package io.withlambda.ktor2.server.bind.request.mapping

import io.ktor.http.HttpMethod

/**
 * Storage of base request information for an endpoint with [HttpMethod.Options] method
 */
class OptionsRequestMapping(
    override val method: HttpMethod = HttpMethod.Options,
    override val name: String,
    override val path: String,
) : RequestMapping
