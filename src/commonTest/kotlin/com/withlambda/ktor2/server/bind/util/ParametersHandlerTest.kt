package com.withlambda.ktor2.server.bind.util

import io.ktor.http.parametersOf
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ParametersHandlerTest {
    private val parameters = parametersOf(
        "param1" to listOf("1"),
        "param2" to listOf("2")
    )

    @Test
    fun `RequiredParametersHandler returns correct parameter value for available parameters`() {
        val param1: Int by RequiredParametersHandler(parameters)
        val param2: String by RequiredParametersHandler(parameters)
        assertEquals(1, param1)
        assertEquals("2", param2)
    }

    @Test
    fun `RequiredParametersHandler throws MissingRequestParameterException for not available parameters`() {
        @Suppress("UNUSED_EXPRESSION")
        assertFailsWith<MissingRequestParameterException> {
            val param3: Int by RequiredParametersHandler(parameters)
            param3
        }
    }

    @Test
    fun `OptionalParametersHandler returns correct parameter value for available parameters`() {
        val param1: Int? by OptionalParametersHandler(parameters)
        val param2: String? by OptionalParametersHandler(parameters)
        assertEquals(1, param1)
        assertEquals("2", param2)
    }

    @Test
    fun `OptionalParametersHandler returns null for not available parameters`() {
        val param3: Int? by OptionalParametersHandler(parameters)
        assertNull(param3)
    }

    @Test
    fun `RequiredParametersHandler throws ParameterConversionException when value cannot be converted`() {
        val badParams = parametersOf("count" to listOf("not-a-number"))
        assertFailsWith<ParameterConversionException> {
            val count: Int by RequiredParametersHandler(badParams)
            count
        }
    }

    @Test
    fun `OptionalParametersHandler throws ParameterConversionException when value cannot be converted`() {
        val badParams = parametersOf("count" to listOf("not-a-number"))
        assertFailsWith<ParameterConversionException> {
            val count: Int? by OptionalParametersHandler(badParams)
            count
        }
    }
}
