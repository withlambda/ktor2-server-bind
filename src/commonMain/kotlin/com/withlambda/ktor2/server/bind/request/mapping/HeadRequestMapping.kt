package com.withlambda.ktor2.server.bind.request.mapping

import io.ktor.http.HttpMethod

/**
 * Storage of base request information for an endpoint with [HttpMethod.Head] method
 */
class HeadRequestMapping(
    override val method: HttpMethod = HttpMethod.Head,
    override val name: String,
    override val path: String,
) : RequestMapping
