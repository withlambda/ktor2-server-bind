package com.withlambda.ktor2.server.bind.request

import kotlin.test.Test
import kotlin.test.assertNotNull

class RequestAttributesTest {
    @Test
    fun `RequestData has attributes property`() {
        val requestData = object : RequestData {
            override val pathParameter = object : PathParameters {}
            override val optionalQueryParameter = object : OptionalQueryParameters {}
            override val requiredQueryParameter = object : RequiredQueryParameters {}
            override val headers = object : RequestHeaders {}
            override val cookies = object : RequestCookies {}
            override val attributes = object : RequestAttributes {}
        }
        assertNotNull(requestData.attributes)
    }
}
