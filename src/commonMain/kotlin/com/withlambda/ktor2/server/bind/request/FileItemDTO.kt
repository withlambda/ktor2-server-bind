package com.withlambda.ktor2.server.bind.request

import io.ktor.http.ContentType
import io.ktor.utils.io.core.Input

class FileItemDTO(
    val fileName: String?,
    val contentType: ContentType?,
    private val provider: () -> Input
) {
    fun openStream(): Input = provider()
}
