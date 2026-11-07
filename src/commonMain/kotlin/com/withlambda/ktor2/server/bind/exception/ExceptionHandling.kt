package com.withlambda.ktor2.server.bind.exception

import com.withlambda.ktor2.server.bind.config.LogLevel
import com.withlambda.ktor2.server.bind.config.Logging
import com.withlambda.ktor2.server.bind.exception.ExceptionHandling.defaultPredefinedBadExceptionsHandlerBuilder
import com.withlambda.ktor2.server.bind.exception.ExceptionHandling.logger
import com.withlambda.ktor2.server.bind.exception.ExceptionHandling.predefinedBadRequestExceptionsHandler
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.util.logging.KtorSimpleLogger

/**
 * Set up exception handling
 * @property logger The logger instance for the exception handling
 * @property defaultPredefinedBadExceptionsHandlerBuilder The default exception handler builder for
 * predefined bad request exceptions. This builder logs the exceptions at
 * the [Logging.LogLevel.ExceptionHandling.handlingClientExceptions] level and responds the call with
 * [HttpStatusCode.BadRequest] status code and a response message which is set to the log message.
 * @property StatusPagesConfig.predefinedBadRequestExceptionsHandler The status pages configuration for
 * predefined bad request exceptions
 */
object ExceptionHandling {
    /**
     * The logger instance for the exception handling
     */
    private val logger = KtorSimpleLogger(ExceptionHandling::class.simpleName ?: "ExceptionHandling")

    private fun logAtLevel(level: LogLevel): (String) -> Unit = { message ->
        when (level) {
            LogLevel.TRACE -> logger.trace(message)
            LogLevel.DEBUG -> logger.debug(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
        }
    }

    /**
     * The default exception handler builder for
     * predefined bad request exceptions. This builder logs the exceptions at
     * the [Logging.LogLevel.ExceptionHandling.handlingClientExceptions] level and responds the call with
     * [HttpStatusCode.BadRequest] status code and a response message which is set to the log message.
     */
    val defaultPredefinedBadExceptionsHandlerBuilder = ExceptionHandlerBuilderImpl(
        httpStatusCode = HttpStatusCode.BadRequest,
        log = logAtLevel(Logging.LogLevel.ExceptionHandling.handlingClientExceptions),
    ) { _, logMessage -> logMessage }

    /**
     * The status pages configuration for predefined bad request exceptions such as
     * [MissingRequestParameterException], [ParameterConversionException] and [MissingHeaderException].
     * @param predefinedBadExceptionsHandlerBuilder The builder of the exception handler, defaults
     * to [defaultPredefinedBadExceptionsHandlerBuilder]
     */
    fun StatusPagesConfig.predefinedBadRequestExceptionsHandler(
        predefinedBadExceptionsHandlerBuilder: ExceptionHandlerBuilder = defaultPredefinedBadExceptionsHandlerBuilder
    ) {
        listOf(
            MissingRequestParameterException::class,
            ParameterConversionException::class,
            MissingHeaderException::class
        ).forEach { exceptionClass ->
            exception(exceptionClass, predefinedBadExceptionsHandlerBuilder.build())
        }
    }
}
