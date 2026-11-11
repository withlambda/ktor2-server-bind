package io.withlambda.ktor2.server.bind

import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import io.ktor.server.application.ApplicationCall
import io.ktor.util.converters.DefaultConversionService
import io.withlambda.ktor2.server.bind.config.auth.RoleBasedAuthorizationConfigurationImpl
import io.withlambda.ktor2.server.bind.request.OptionalQueryParameters
import io.withlambda.ktor2.server.bind.request.PathParameters
import io.withlambda.ktor2.server.bind.request.RequestCookies
import io.withlambda.ktor2.server.bind.request.RequestHeaders
import io.withlambda.ktor2.server.bind.request.RequiredQueryParameters
import io.withlambda.ktor2.server.bind.request.mapping.PostRequestMapping
import kotlin.test.Test
import kotlin.test.assertNotNull

class RouteConfigValidationTest {
    @Test
    fun testValidationProperty() {
        val config = object : PostRouteConfig<String> {
            override val requestMapping =
                PostRequestMapping(name = "test", path = "/test", consumes = null, produces = null)
            override val conversionService = DefaultConversionService
            override val roleBasedAuthorizationConfiguration = RoleBasedAuthorizationConfigurationImpl()
            override val validation: Validation<String> = Validation {
                minLength(5)
            }

            override suspend fun ApplicationCall.getPathParameters(): PathParameters = TODO()
            override suspend fun ApplicationCall.getOptionalQueryParameters(): OptionalQueryParameters = TODO()
            override suspend fun ApplicationCall.getRequiredQueryParameters(): RequiredQueryParameters = TODO()
            override suspend fun ApplicationCall.getRequestHeaders(): RequestHeaders = TODO()
            override suspend fun ApplicationCall.getRequestCookies(): RequestCookies = TODO()
        }
        assertNotNull(config.validation)
    }
}
