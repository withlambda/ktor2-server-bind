package io.withlambda.ktor2.server.bind.config

import io.withlambda.ktor2.server.bind.config.Logging.LogLevel.ExceptionHandling.handlingClientExceptions
import io.withlambda.ktor2.server.bind.config.Logging.LogLevel.Plugin.roleBasedAuthorization

/**
 * Logging configuration object
 */
object Logging {
    /**
     * Object to store the [LogLevel]
     * of different Logger instance within this library
     */
    object LogLevel {
        /**
         * [LogLevel] configurations for plugins of this library
         * @property roleBasedAuthorization [LogLevel] parameter for the RoleBaseAuthorization plugin
         */
        object Plugin {
            var roleBasedAuthorization: io.withlambda.ktor2.server.bind.config.LogLevel =
                io.withlambda.ktor2.server.bind.config.LogLevel.INFO
        }

        /**
         * [LogLevel] configurations for exception handling
         * @property handlingClientExceptions [LogLevel] parameter for exceptions caused by the client
         */
        object ExceptionHandling {
            val handlingClientExceptions: io.withlambda.ktor2.server.bind.config.LogLevel =
                io.withlambda.ktor2.server.bind.config.LogLevel.INFO
        }
    }
}
