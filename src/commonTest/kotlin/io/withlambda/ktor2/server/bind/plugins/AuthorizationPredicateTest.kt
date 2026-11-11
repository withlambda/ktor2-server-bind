package io.withlambda.ktor2.server.bind.plugins

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.withlambda.ktor2.server.bind.config.auth.AuthorizationPredicate
import io.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import io.withlambda.ktor2.server.bind.config.auth.and
import io.withlambda.ktor2.server.bind.config.auth.not
import io.withlambda.ktor2.server.bind.config.auth.or
import io.withlambda.ktor2.server.bind.fixtures.plugins.configureAuth
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorizationPredicateTest {

    private fun makeToken(vararg roles: String) = roles.joinToString(",")

    // --- Task 1: custom predicate configuration ---

    @Test
    fun `roleBasedAuthorization with custom predicate allows request when predicate returns true`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply {
                                    predicate = { _, _ -> true }
                                }
                            )
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
    fun `roleBasedAuthorization with custom predicate denies request when predicate returns false`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply {
                                    predicate = { _, _ -> false }
                                }
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    // --- Task 2: DSL combinators ---

    @Test
    fun `and combinator allows request when both predicates return true`() {
        val alwaysTrue: AuthorizationPredicate = { _, _ -> true }
        val combined = alwaysTrue and alwaysTrue
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
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
    fun `and combinator denies request when first predicate returns false`() {
        val alwaysTrue: AuthorizationPredicate = { _, _ -> true }
        val alwaysFalse: AuthorizationPredicate = { _, _ -> false }
        val combined = alwaysFalse and alwaysTrue
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
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
    fun `and combinator denies request when second predicate returns false`() {
        val alwaysTrue: AuthorizationPredicate = { _, _ -> true }
        val alwaysFalse: AuthorizationPredicate = { _, _ -> false }
        val combined = alwaysTrue and alwaysFalse
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
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
    fun `or combinator allows request when first predicate returns true`() {
        val alwaysTrue: AuthorizationPredicate = { _, _ -> true }
        val alwaysFalse: AuthorizationPredicate = { _, _ -> false }
        val combined = alwaysTrue or alwaysFalse
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
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
    fun `or combinator allows request when second predicate returns true`() {
        val alwaysTrue: AuthorizationPredicate = { _, _ -> true }
        val alwaysFalse: AuthorizationPredicate = { _, _ -> false }
        val combined = alwaysFalse or alwaysTrue
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
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
    fun `or combinator denies request when both predicates return false`() {
        val alwaysFalse: AuthorizationPredicate = { _, _ -> false }
        val combined = alwaysFalse or alwaysFalse
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
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
    fun `not combinator inverts true predicate to false`() {
        val alwaysTrue: AuthorizationPredicate = { _, _ -> true }
        val inverted = alwaysTrue.not()
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = inverted }
                            )
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
    fun `not combinator inverts false predicate to true`() {
        val alwaysFalse: AuthorizationPredicate = { _, _ -> false }
        val inverted = alwaysFalse.not()
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = inverted }
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    // --- Task 3: plugin integration with predicate ---

    @Test
    fun `predicate checking custom header allows request when header is present`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply {
                                    predicate = { _, call -> call.request.header("X-Custom-Header") != null }
                                }
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") {
                header("Authorization", "Bearer ${makeToken("admin")}")
                header("X-Custom-Header", "present")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `predicate checking custom header denies request when header is absent`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply {
                                    predicate = { _, call -> call.request.header("X-Custom-Header") != null }
                                }
                            )
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
    fun `predicate combining role check and custom header requires both conditions`() {
        val hasAdminRole: AuthorizationPredicate = { roles, _ -> roles.roles.contains("admin") }
        val hasCustomHeader: AuthorizationPredicate = { _, call -> call.request.header("X-Custom-Header") != null }
        val combined = hasAdminRole and hasCustomHeader
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply { predicate = combined }
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val allowedResponse = client.get("/secured") {
                header("Authorization", "Bearer ${makeToken("admin")}")
                header("X-Custom-Header", "present")
            }
            assertEquals(HttpStatusCode.OK, allowedResponse.status)

            val deniedByRole = client.get("/secured") {
                header("Authorization", "Bearer ${makeToken("viewer")}")
                header("X-Custom-Header", "present")
            }
            assertEquals(HttpStatusCode.Forbidden, deniedByRole.status)

            val deniedByHeader = client.get("/secured") {
                header("Authorization", "Bearer ${makeToken("admin")}")
            }
            assertEquals(HttpStatusCode.Forbidden, deniedByHeader.status)
        }
    }

    @Test
    fun `predicate that always denies returns 403`() {
        testApplication {
            application {
                configureAuth()
                routing {
                    authenticate {
                        route("/secured") {
                            roleBasedAuthorization(
                                RoleBasedAuthorizationConfigurationImpl().apply {
                                    predicate = { _, _ -> false }
                                }
                            )
                            get { call.respond(HttpStatusCode.OK) }
                        }
                    }
                }
            }
            val response = client.get("/secured") { header("Authorization", "Bearer ${makeToken("admin")}") }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }
}
