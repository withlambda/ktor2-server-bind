package io.withlambda.ktor2.server.bind.util

import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.headersOf
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import io.withlambda.ktor2.server.bind.request.FileItemDTO
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MultipartHandlerTest {

    @Test
    fun testMultipartHandler() = runBlocking {
        val formFieldName = "field1"
        val formFieldValue = "value1"
        val fileName = "test.txt"
        val fileContent = "file content"
        val contentType = ContentType.Text.Plain

        val partDataList = listOf(
            PartData.FormItem(
                formFieldValue,
                {},
                headersOf(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition("form-data").withParameter(ContentDisposition.Parameters.Name, formFieldName)
                        .toString()
                )
            ),
            PartData.FileItem(
                { ByteReadPacket(fileContent.encodeToByteArray()) },
                {},
                Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File
                            .withParameter(ContentDisposition.Parameters.Name, "file1")
                            .withParameter(ContentDisposition.Parameters.FileName, fileName).toString()
                    )
                    append(HttpHeaders.ContentType, contentType.toString())
                }
            )
        )

        val multiPartData = object : MultiPartData {
            private var index = 0
            override suspend fun readPart(): PartData? {
                return if (index < partDataList.size) partDataList[index++] else null
            }
        }

        val handler = MultipartHandler(multiPartData)
        val parts = handler.parseAllParts()

        assertEquals(formFieldValue, parts[formFieldName])
        val fileItem = parts["file1"] as? FileItemDTO
        assertNotNull(fileItem)
        assertEquals(fileName, fileItem.fileName)
        assertEquals(contentType, fileItem.contentType?.withoutParameters())
        assertEquals(fileContent, fileItem.openStream().readBytes().decodeToString())
    }
}
