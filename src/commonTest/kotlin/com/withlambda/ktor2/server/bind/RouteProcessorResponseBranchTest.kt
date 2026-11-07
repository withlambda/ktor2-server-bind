package com.withlambda.ktor2.server.bind

import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.GetHelloWorld
import com.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.HeadHelloWorld
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteProcessorResponseBranchTest {

    private val noAuth = RoleBasedAuthorizationConfigurationImpl(
        enabled = false,
        requiredRolesAllOf = setOf("dummy")
    )

    @Test
    fun `RouteProcessorBase customizedResponse takes responseMessage branch when responseMessage is not null`() {
        testApplication {
            application {
                routing {
                    val processor = HeadHelloWorld.RouteProcessorImpl(
                        roleBasedAuthorizationConfiguration = noAuth,
                        processRequest = {
                            HeadHelloWorld.ResponseDataImpl(
                                httpStatusCode = HttpStatusCode.OK,
                                responseBody = null,
                                responseMessage = "head message",
                                responseHeaders = null
                            )
                        }
                    )
                    with(processor) { install() }
                }
            }
            val response = client.head(
                "/helloworld/1/value?requiredQueryParam1=3&requiredQueryParam2=4"
            ) {
                header("header2", "value")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `RouteProcessor customizedResponse takes responseMessage branch when body is null and message is not null`() {
        testApplication {
            application {
                routing {
                    val processor = GetHelloWorld.RouteProcessorImpl(
                        roleBasedAuthorizationConfiguration = noAuth,
                        processRequest = {
                            GetHelloWorld.ResponseDataImpl(
                                httpStatusCode = HttpStatusCode.OK,
                                responseBody = null,
                                responseMessage = "message only",
                                responseHeaders = null
                            )
                        }
                    )
                    with(processor) { install() }
                }
            }
            val response = client.get(
                "/helloworld/1/value?requiredQueryParam1=3&requiredQueryParam2=4"
            ) {
                header("header2", "value")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `RouteProcessor customizedResponse takes else branch when both responseBody and responseMessage are null`() {
        testApplication {
            application {
                routing {
                    val processor = GetHelloWorld.RouteProcessorImpl(
                        roleBasedAuthorizationConfiguration = noAuth,
                        processRequest = {
                            GetHelloWorld.ResponseDataImpl(
                                httpStatusCode = HttpStatusCode.NoContent,
                                responseBody = null,
                                responseMessage = null,
                                responseHeaders = null
                            )
                        }
                    )
                    with(processor) { install() }
                }
            }
            val response = client.get(
                "/helloworld/1/value?requiredQueryParam1=3&requiredQueryParam2=4"
            ) {
                header("header2", "value")
            }
            assertEquals(HttpStatusCode.NoContent, response.status)
        }
    }
}
