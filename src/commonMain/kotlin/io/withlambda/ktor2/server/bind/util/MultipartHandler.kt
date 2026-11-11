package io.withlambda.ktor2.server.bind.util

import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.withlambda.ktor2.server.bind.request.FileItemDTO

/**
 * Handler to extract form fields and files from [MultiPartData].
 */
class MultipartHandler(private val multiPartData: MultiPartData) {

    /**
     * Parses all parts from the multipart data and returns a map of part names to their values.
     * Form items are mapped to [String], and file items are mapped to [FileItemDTO].
     */
    suspend fun parseAllParts(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        while (true) {
            val part = multiPartData.readPart() ?: break
            val name = part.name
            if (name != null) {
                when (part) {
                    is PartData.FormItem -> {
                        result[name] = part.value
                        part.dispose()
                    }

                    is PartData.FileItem -> {
                        result[name] = FileItemDTO(
                            fileName = part.originalFileName,
                            contentType = part.contentType,
                            provider = part.provider
                        )
                        // We do not dispose FileItem here because it might be needed for streaming later.
                        // Note: In a real-world scenario, we might need a way to track and dispose these.
                    }

                    is PartData.BinaryItem -> {
                        part.dispose()
                    }

                    else -> {
                        part.dispose()
                    }
                }
            } else {
                part.dispose()
            }
        }
        return result
    }
}
