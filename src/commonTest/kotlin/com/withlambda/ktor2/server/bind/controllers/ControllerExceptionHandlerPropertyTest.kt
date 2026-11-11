package io.withlambda.ktor2.server.bind.controllers

import io.withlambda.ktor2.server.bind.Controller
import io.withlambda.ktor2.server.bind.RouteProcessorBase
import io.withlambda.ktor2.server.bind.config.auth.AuthenticationConfiguration
import io.withlambda.ktor2.server.bind.exception.ExceptionHandlerBuilder
import io.withlambda.ktor2.server.bind.exception.exception
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests that the [Controller] interface exposes the [Controller.exceptionHandler] property with a
 * `null` default and that implementors can override it with a non-null DSL block.
 */
class ControllerExceptionHandlerPropertyTest {

    private val disabledAuth = object : AuthenticationConfiguration {
        override val enable = false
        override val providers: Array<String?> = emptyArray()
        override val optional = false
    }

    @Test
    fun `Controller defaults exceptionHandler to null`() {
        val controller = object : Controller {
            override val endpoints: List<RouteProcessorBase<*, *, *>> = emptyList()
            override val authenticationConfiguration = disabledAuth
        }
        assertNull(controller.exceptionHandler)
    }

    @Test
    fun `Controller allows overriding exceptionHandler with a non-null DSL block`() {
        val controller = object : Controller {
            override val endpoints: List<RouteProcessorBase<*, *, *>> = emptyList()
            override val authenticationConfiguration = disabledAuth
            override val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit) = {
                // No-op handler — this test only checks that the property can be overridden
                exception<IllegalStateException> { _, _ -> }
            }
        }
        assertNotNull(controller.exceptionHandler)
    }
}
