package com.withlambda.ktor2.server.bind.util

import com.withlambda.ktor2.server.bind.exception.MissingAttributeException
import com.withlambda.ktor2.server.bind.request.RequestAttributes
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AttributesHandlerTest {

    private fun buildAttributes(vararg pairs: Pair<AttributeKey<*>, Any>): Attributes {
        val attrs = Attributes()
        @Suppress("UNCHECKED_CAST")
        pairs.forEach { (key, value) -> attrs.put(key as AttributeKey<Any>, value) }
        return attrs
    }

    @Test
    fun `RequiredAttributesHandler returns correct String attribute`() {
        val attrs = buildAttributes(AttributeKey<String>("transactionId") to "tx-123")

        class MyAttrs(a: Attributes) : RequestAttributes {
            val transactionId: String by RequiredAttributesHandler(a)
        }

        assertEquals("tx-123", MyAttrs(attrs).transactionId)
    }

    @Test
    fun `RequiredAttributesHandler returns correct Boolean attribute`() {
        val attrs = buildAttributes(AttributeKey<Boolean>("isAdmin") to true)

        class MyAttrs(a: Attributes) : RequestAttributes {
            val isAdmin: Boolean by RequiredAttributesHandler(a)
        }

        assertEquals(true, MyAttrs(attrs).isAdmin)
    }

    @Test
    fun `RequiredAttributesHandler returns correct custom object attribute`() {
        data class UserId(val value: String)
        val attrs = buildAttributes(AttributeKey<UserId>("userId") to UserId("u-99"))

        class MyAttrs(a: Attributes) : RequestAttributes {
            val userId: UserId by RequiredAttributesHandler(a)
        }

        assertEquals(UserId("u-99"), MyAttrs(attrs).userId)
    }

    @Test
    fun `RequiredAttributesHandler throws MissingAttributeException for absent attribute`() {
        val attrs = Attributes()

        class MyAttrs(a: Attributes) : RequestAttributes {
            val missing: String by RequiredAttributesHandler(a)
        }

        assertFailsWith<MissingAttributeException> {
            MyAttrs(attrs).missing
        }
    }

    @Test
    fun `OptionalAttributesHandler returns correct String attribute`() {
        val attrs = buildAttributes(AttributeKey<String>("locale") to "en-US")

        class MyAttrs(a: Attributes) : RequestAttributes {
            val locale: String? by OptionalAttributesHandler(a)
        }

        assertEquals("en-US", MyAttrs(attrs).locale)
    }

    @Test
    fun `OptionalAttributesHandler returns null for absent attribute`() {
        val attrs = Attributes()

        class MyAttrs(a: Attributes) : RequestAttributes {
            val locale: String? by OptionalAttributesHandler(a)
        }

        assertNull(MyAttrs(attrs).locale)
    }

    @Test
    fun `RequiredAttributesHandler implements RequestAttributes`() {
        val handler: RequestAttributes = RequiredAttributesHandler(Attributes())
        assertNotNull(handler)
    }

    @Test
    fun `OptionalAttributesHandler implements RequestAttributes`() {
        val handler: RequestAttributes = OptionalAttributesHandler(Attributes())
        assertNotNull(handler)
    }
}
