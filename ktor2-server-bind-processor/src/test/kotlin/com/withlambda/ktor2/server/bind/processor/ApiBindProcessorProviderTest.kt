package com.withlambda.ktor2.server.bind.processor

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Verifies that the [ApiBindProcessorProvider] is correctly registered as a KSP
 * [com.google.devtools.ksp.processing.SymbolProcessorProvider] via the META-INF/services mechanism.
 */
class ApiBindProcessorProviderTest {

    /**
     * Asserts that the META-INF/services file for [com.google.devtools.ksp.processing.SymbolProcessorProvider]
     * is present on the classpath and references [ApiBindProcessorProvider].
     */
    @Test
    fun `SymbolProcessorProvider is registered via META-INF services`() {
        val serviceLoader = java.util.ServiceLoader.load(
            com.google.devtools.ksp.processing.SymbolProcessorProvider::class.java,
            ApiBindProcessorProvider::class.java.classLoader
        )
        val providers = serviceLoader.toList()
        assertNotNull(
            providers.filterIsInstance<ApiBindProcessorProvider>().firstOrNull(),
            "Expected ApiBindProcessorProvider to be registered as a SymbolProcessorProvider"
        )
    }
}
