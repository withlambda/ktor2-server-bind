package io.withlambda.ktor2.server.bind.util

import io.ktor.http.headersOf
import io.withlambda.ktor2.server.bind.exception.HeaderConversionException
import io.withlambda.ktor2.server.bind.exception.MissingHeaderException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class HeadersHandlerTest {
    private val headers = headersOf(
        "header1" to listOf("1"),
        "header2" to listOf("2")
    )

    @Test
    fun `RequiredHeadersHandler returns correct parameter value for available headers`() {
        val header1: Int by RequiredHeadersHandler(headers)
        val header2: String by RequiredHeadersHandler(headers)
        assertEquals(1, header1)
        assertEquals("2", header2)
    }

    @Test
    fun `RequiredHeadersHandler throws MissingHeadersException for not available headers`() {
        @Suppress("UNUSED_EXPRESSION")
        assertFailsWith<MissingHeaderException> {
            val param3: Int by RequiredHeadersHandler(headers)
            param3
        }
    }

    @Test
    fun `OptionalHeadersHandler returns correct parameter value for available headers`() {
        val header1: Int? by OptionalHeadersHandler(headers)
        val header2: String? by OptionalHeadersHandler(headers)
        assertEquals(1, header1)
        assertEquals("2", header2)
    }

    @Test
    fun `OptionalHeadersHandler returns null for not available headers`() {
        val param3: Int? by OptionalHeadersHandler(headers)
        assertNull(param3)
    }

    @Test
    fun `RequiredHeadersHandler throws HeaderConversionException when header value cannot be converted`() {
        val badHeaders = headersOf("count" to listOf("not-a-number"))
        assertFailsWith<HeaderConversionException> {
            val count: Int by RequiredHeadersHandler(badHeaders)
            count
        }
    }

    @Test
    fun `OptionalHeadersHandler throws HeaderConversionException when header value cannot be converted`() {
        val badHeaders = headersOf("count" to listOf("not-a-number"))
        assertFailsWith<HeaderConversionException> {
            val count: Int? by OptionalHeadersHandler(badHeaders)
            count
        }
    }

    @Test
    fun `RequiredHeadersHandler throwConversionException without cause throws HeaderConversionException`() {
        val handler = RequiredHeadersHandler(headersOf())
        assertFailsWith<HeaderConversionException> {
            handler.throwConversionException("X-Test", "Int")
        }
    }
}
