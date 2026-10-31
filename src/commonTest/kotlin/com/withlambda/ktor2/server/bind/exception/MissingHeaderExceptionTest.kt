package com.withlambda.ktor2.server.bind.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MissingHeaderExceptionTest {

    @Test
    fun `MissingHeaderException has correct message for headerName`() {
        val exception = MissingHeaderException(headerName = "X-My-Header")
        assertEquals("Header X-My-Header is missing", exception.message)
    }

    @Test
    fun `MissingHeaderException stores headerName property`() {
        val exception = MissingHeaderException(headerName = "Authorization")
        assertEquals("Authorization", exception.headerName)
    }

    @Test
    fun `createCopy returns a new instance with the same headerName and original as cause`() {
        val original = MissingHeaderException("X-Missing")
        val copy = original.createCopy()
        assertEquals("X-Missing", copy.headerName)
        assertNotNull(copy.cause)
        assertEquals(original, copy.cause)
    }
}
