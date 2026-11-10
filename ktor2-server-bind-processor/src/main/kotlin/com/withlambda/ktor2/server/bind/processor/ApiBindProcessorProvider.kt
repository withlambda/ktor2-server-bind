package com.withlambda.ktor2.server.bind.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP [SymbolProcessorProvider] that creates an [ApiBindProcessor] for each compilation.
 *
 * Registered via `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`
 * so that the Kotlin compiler picks it up automatically when this artifact is declared as a
 * `ksp(…)` dependency.
 */
class ApiBindProcessorProvider : SymbolProcessorProvider {

    /**
     * Creates and returns a new [ApiBindProcessor] backed by the given [environment].
     *
     * @param environment Provides the logger, code generator, and processor options.
     * @return A fresh [ApiBindProcessor] instance for the current compilation.
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        ApiBindProcessor(environment)
}
