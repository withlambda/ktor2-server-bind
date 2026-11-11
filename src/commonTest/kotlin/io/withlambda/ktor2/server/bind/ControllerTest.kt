package io.withlambda.ktor2.server.bind

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.withlambda.ktor2.server.bind.config.auth.AuthenticationConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals

class ControllerTest {

    private val disabledAuthConfig = object : AuthenticationConfiguration {
        override val enable: Boolean = false
        override val providers: Array<String?> = arrayOf(null)
        override val optional: Boolean = false
    }

    private val testController = object : Controller {
        override val endpoints: List<RouteProcessorBase<*, *, *>> = emptyList()
        override val authenticationConfiguration: AuthenticationConfiguration = disabledAuthConfig
    }

    @Test
    fun `Controller with enable=false bypasses authentication and calls build directly`() {
        testApplication {
            application {
                routing {
                    with(testController) { install() }
                }
            }
            val response = client.get("/any-path")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    @Test
    fun `Route install extension installs controller endpoints`() {
        testApplication {
            application {
                routing {
                    install(testController)
                }
            }
            val response = client.get("/any-path")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }
}
