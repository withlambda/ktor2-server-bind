package io.withlambda.ktor2.server.bind.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class HeaderConversionExceptionTest {

    @Test
    fun `HeaderConversionException has correct message for headerName and type`() {
        val exception = HeaderConversionException(headerName = "X-My-Header", type = "Int")
        assertEquals("value of header X-My-Header couldn't be parsed/converted to Int", exception.message)
    }

    @Test
    fun `HeaderConversionException stores headerName and type properties`() {
        val exception = HeaderConversionException(headerName = "Content-Type", type = "Long")
        assertEquals("Content-Type", exception.headerName)
        assertEquals("Long", exception.type)
    }

    @Test
    fun `HeaderConversionException accepts an optional cause`() {
        val cause = RuntimeException("original cause")
        val exception = HeaderConversionException(headerName = "X-Header", type = "Int", cause = cause)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `createCopy returns a new instance with the same headerName and type and original as cause`() {
        val original = HeaderConversionException(headerName = "X-Custom", type = "Long")
        val copy = original.createCopy()
        assertEquals("X-Custom", copy.headerName)
        assertEquals("Long", copy.type)
        assertEquals(original, copy.cause)
    }
}
