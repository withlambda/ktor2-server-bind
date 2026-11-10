package com.withlambda.ktor2.server.bind.request

import io.ktor.http.ContentType
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class FileItemDTOTest {
    @Test
    fun testFileItemDTO() {
        val content = "Hello, World!"
        val fileName = "test.txt"
        val contentType = ContentType.Text.Plain

        // This will fail to compile because FileItemDTO does not exist yet
        val fileItem = FileItemDTO(fileName, contentType) {
            ByteReadPacket(content.toByteArray())
        }

        assertEquals(fileName, fileItem.fileName)
        assertEquals(contentType, fileItem.contentType)
        val stream = fileItem.openStream()
        assertEquals(content, stream.readBytes().decodeToString())
    }
}
