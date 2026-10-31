package com.withlambda.ktor2.server.bind.fixtures.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Installs the Ktor [ContentNegotiation] plugin on the test application with a [Json] serializer.
 *
 * The [Json] instance is configured to match the settings used in [com.withlambda.ktor2.server.bind.fixtures.TestTemplates]:
 * - `prettyPrint = true` — produces human-readable JSON output.
 * - `isLenient = false` — strict JSON parsing; no unquoted keys or other lenient extensions.
 * - `ignoreUnknownKeys = false` — unknown JSON keys cause a parse error.
 * - `explicitNulls = false` — `null` properties are omitted from the serialized output.
 */
@OptIn(ExperimentalSerializationApi::class)
fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = false
                ignoreUnknownKeys = false
                explicitNulls = false
            }
        )
    }
}
