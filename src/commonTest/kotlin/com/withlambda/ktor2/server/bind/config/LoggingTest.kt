package io.withlambda.ktor2.server.bind.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LoggingTest {

    @Test
    fun `Logging LogLevel Plugin roleBasedAuthorization defaults to INFO`() {
        assertEquals(LogLevel.INFO, Logging.LogLevel.Plugin.roleBasedAuthorization)
    }

    @Test
    fun `Logging LogLevel Plugin roleBasedAuthorization can be changed`() {
        val original = Logging.LogLevel.Plugin.roleBasedAuthorization
        try {
            Logging.LogLevel.Plugin.roleBasedAuthorization = LogLevel.DEBUG
            assertEquals(LogLevel.DEBUG, Logging.LogLevel.Plugin.roleBasedAuthorization)
        } finally {
            Logging.LogLevel.Plugin.roleBasedAuthorization = original
        }
    }

    @Test
    fun `Logging LogLevel ExceptionHandling handlingClientExceptions is not null`() {
        assertNotNull(Logging.LogLevel.ExceptionHandling.handlingClientExceptions)
    }

    @Test
    fun `Logging LogLevel ExceptionHandling handlingClientExceptions defaults to INFO`() {
        assertEquals(LogLevel.INFO, Logging.LogLevel.ExceptionHandling.handlingClientExceptions)
    }

    @Test
    fun `Logging object is accessible as a singleton value`() {
        val logging = Logging
        assertNotNull(logging)
    }

    @Test
    fun `Logging LogLevel object is accessible as a singleton value`() {
        val logLevel = Logging.LogLevel
        assertNotNull(logLevel)
    }
}
