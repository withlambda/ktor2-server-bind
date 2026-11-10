package com.withlambda.ktor2.server.bind.plugins

import com.withlambda.ktor2.server.bind.config.LogLevel
import com.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import com.withlambda.ktor2.server.bind.fixtures.plugins.configureAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RoleBasedAuthorizationTest {

    private fun makeToken(vararg roles: String) = roles.joinToString(",")

    @Test
    fun `authorizedWithRole allows request when principal has the required role`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            authorizedWithRole("admin")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `authorizedWithRole returns 403 when principal has no roles (empty token)`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            authorizedWithRole("admin")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `authorizedWithAllRoles returns 200 when principal has all required roles`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            authorizedWithAllRoles("admin", "superuser")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") {
                header("Authorization", "Bearer ${makeToken("admin", "superuser")}")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `authorizedWithAllRoles returns 403 when principal is missing one required role`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            authorizedWithAllRoles("admin", "superuser")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `authorizedWithAnyRole allows request when principal has at least one required role`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            authorizedWithAnyRole("admin", "editor")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("editor")}") }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `authorizedWithAnyRole returns 403 when principal has none of the required anyOf roles`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            authorizedWithAnyRole("admin", "editor")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `unauthorizedWithRole returns 403 when principal has a disallowed role`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            unauthorizedWithRole("banned")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin", "banned")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `unauthorizedWithRole allows request when principal does not have the disallowed role`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            unauthorizedWithRole("banned")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `unauthorizedWithAnyRole returns 403 when principal has any disallowed role`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            unauthorizedWithAnyRole("banned", "suspended")
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") {
                header("Authorization", "Bearer ${makeToken("admin", "suspended")}")
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `roleBasedAuthorization with enabled false allows any authenticated request`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = false,
                                    requiredRolesAllOf = setOf("admin")
                                )
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `roleBasedAuthorization with logLevel WARN enforces authorization correctly`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = true,
                                    requiredRolesAllOf = setOf("admin"),
                                    logLevel = LogLevel.WARN
                                )
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val denied = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.Forbidden, denied.status)

            val allowed = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.OK, allowed.status)
        }
    }

    @Test
    fun `roleBasedAuthorization with logLevel DEBUG enforces authorization correctly`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = true,
                                    requiredRolesAllOf = setOf("admin"),
                                    logLevel = LogLevel.DEBUG
                                )
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `roleBasedAuthorization with logLevel TRACE enforces authorization correctly`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = true,
                                    requiredRolesAllOf = setOf("admin"),
                                    logLevel = LogLevel.TRACE
                                )
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `roleBasedAuthorization with logLevel INFO enforces authorization correctly`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = true,
                                    requiredRolesAllOf = setOf("admin"),
                                    logLevel = LogLevel.INFO
                                )
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `roleBasedAuthorization with logLevel ERROR enforces authorization correctly`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = true,
                                    requiredRolesAllOf = setOf("admin"),
                                    logLevel = LogLevel.ERROR
                                )
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("viewer")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    @Test
    fun `roleBasedAuthorization throws IllegalStateException when no roles are configured`() {
        assertFailsWith<IllegalStateException> {
            testApplication {
                application {
                    routing {
                        route("/test") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl(
                                    enabled = true,
                                    requiredRolesAllOf = emptySet()
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
