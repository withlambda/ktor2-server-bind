package io.withlambda.ktor2.server.bind

import io.ktor.http.HttpStatusCode
import io.withlambda.ktor2.server.bind.response.ResponseDataWithBytes
import io.withlambda.ktor2.server.bind.response.ResponseDataWithFile
import io.withlambda.ktor2.server.bind.response.ResponseDataWithOutputStream
import io.withlambda.ktor2.server.bind.response.ResponseDataWithSSE
import io.withlambda.ktor2.server.bind.response.ServerSentEvent
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SpecializedResponseDataTest {

    @Test
    fun `ResponseDataWithBytes holds byte array and inherits ResponseData properties`() {
        val bytes = byteArrayOf(72, 101, 108, 108, 111)
        val data = object : ResponseDataWithBytes {
            override val httpStatusCode = HttpStatusCode.OK
            override val responseMessage: String? = null
            override val responseHeaders = null
            override val responseCookies = null
            override val bytes = bytes
        }
        assertEquals(bytes, data.bytes)
        assertEquals(HttpStatusCode.OK, data.httpStatusCode)
    }

    @Test
    fun `ResponseDataWithFile holds file and inherits ResponseData properties`() {
        val file = java.io.File.createTempFile("test", ".txt")
        try {
            val data = object : ResponseDataWithFile {
                override val httpStatusCode = HttpStatusCode.OK
                override val responseMessage: String? = null
                override val responseHeaders = null
                override val responseCookies = null
                override val file = file
            }
            assertEquals(file, data.file)
            assertEquals(HttpStatusCode.OK, data.httpStatusCode)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `ResponseDataWithOutputStream holds producer and inherits ResponseData properties`() {
        val producer: suspend (java.io.OutputStream) -> Unit = { it.write(byteArrayOf(1, 2, 3)) }
        val data = object : ResponseDataWithOutputStream {
            override val httpStatusCode = HttpStatusCode.OK
            override val responseMessage: String? = null
            override val responseHeaders = null
            override val responseCookies = null
            override val producer = producer
        }
        assertNotNull(data.producer)
        assertEquals(HttpStatusCode.OK, data.httpStatusCode)
    }

    @Test
    fun `ResponseDataWithSSE holds flow of events and inherits ResponseData properties`() {
        val events = flowOf(ServerSentEvent("hello"), ServerSentEvent("world"))
        val data = object : ResponseDataWithSSE {
            override val httpStatusCode = HttpStatusCode.OK
            override val responseMessage: String? = null
            override val responseHeaders = null
            override val responseCookies = null
            override val events = events
        }
        assertNotNull(data.events)
        assertEquals(HttpStatusCode.OK, data.httpStatusCode)
    }

    @Test
    fun `ServerSentEvent holds data string`() {
        val event = ServerSentEvent("test data")
        assertEquals("test data", event.data)
    }
}
