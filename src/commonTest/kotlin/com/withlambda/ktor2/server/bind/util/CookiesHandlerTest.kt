package io.withlambda.ktor2.server.bind.util

import io.ktor.util.StringValues
import io.withlambda.ktor2.server.bind.exception.CookieConversionException
import io.withlambda.ktor2.server.bind.exception.MissingCookieException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CookiesHandlerTest {
    private val cookies = StringValues.build {
        append("cookie1", "1")
        append("cookie2", "true")
        append("cookie3", "value")
    }

    @Test
    fun `RequiredCookiesHandler returns correct cookie value for available cookies`() {
        val cookie1: Int by RequiredCookiesHandler(cookies)
        val cookie2: Boolean by RequiredCookiesHandler(cookies)
        val cookie3: String by RequiredCookiesHandler(cookies)
        assertEquals(1, cookie1)
        assertEquals(true, cookie2)
        assertEquals("value", cookie3)
    }

    @Test
    fun `RequiredCookiesHandler throws MissingCookieException for not available cookies`() {
        assertFailsWith<MissingCookieException> {
            val cookie4: Int by RequiredCookiesHandler(cookies)
            cookie4
        }
    }

    @Test
    fun `OptionalCookiesHandler returns correct cookie value for available cookies`() {
        val cookie1: Int? by OptionalCookiesHandler(cookies)
        val cookie2: Boolean? by OptionalCookiesHandler(cookies)
        assertEquals(1, cookie1)
        assertEquals(true, cookie2)
    }

    @Test
    fun `OptionalCookiesHandler returns null for not available cookies`() {
        val cookie4: Int? by OptionalCookiesHandler(cookies)
        assertNull(cookie4)
    }

    @Test
    fun `RequiredCookiesHandler throws CookieConversionException when cookie value cannot be converted`() {
        val badCookies = StringValues.build {
            append("count", "not-a-number")
        }
        assertFailsWith<CookieConversionException> {
            val count: Int by RequiredCookiesHandler(badCookies)
            count
        }
    }

    @Test
    fun `OptionalCookiesHandler throws CookieConversionException when cookie value cannot be converted`() {
        val badCookies = StringValues.build {
            append("count", "not-a-number")
        }
        assertFailsWith<CookieConversionException> {
            val count: Int? by OptionalCookiesHandler(badCookies)
            count
        }
    }
}
