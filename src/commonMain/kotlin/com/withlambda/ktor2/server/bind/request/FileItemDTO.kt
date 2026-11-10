package com.withlambda.ktor2.server.bind.request

import io.ktor.http.ContentType
import io.ktor.utils.io.core.Input

/**
 * Data transfer object representing a single file item received in a multipart request.
 *
 * @property fileName The original file name provided by the client, or `null` if not present.
 * @property contentType The MIME type of the file item, or `null` if not provided.
 * @param provider A factory that opens a fresh [Input] stream over the file content.
 */
class FileItemDTO(
    val fileName: String?,
    val contentType: ContentType?,
    private val provider: () -> Input
) {
    /**
     * Opens and returns a new [Input] stream over the file content.
     *
     * @return A fresh [Input] for reading the uploaded file bytes.
     */
    fun openStream(): Input = provider()
}
