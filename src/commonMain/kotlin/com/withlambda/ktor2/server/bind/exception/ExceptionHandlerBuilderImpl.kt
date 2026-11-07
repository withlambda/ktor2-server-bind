package com.withlambda.ktor2.server.bind.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText

/**
 * Implementation of [ExceptionHandlerBuilder] that stores the configuration
 * of builders for exception handlers and which produces the exception handler
 * by calling the [build] function.
 */
class ExceptionHandlerBuilderImpl(
    override val httpStatusCode: HttpStatusCode,
    override val log: ((String) -> Unit)? = null,
    override val getLogMessage: (exception: Throwable) -> String =
        ExceptionHandlerBuilder.Companion::defaultGetResponseMessage,
    override val getCallResponseContent: (exception: Throwable, logMessage: String) -> String
) : ExceptionHandlerBuilder {

    override fun withHttpStatusCode(
        httpStatusCode: HttpStatusCode
    ): ExceptionHandlerBuilderImpl = ExceptionHandlerBuilderImpl(
        httpStatusCode = httpStatusCode,
        log = this.log,
        getLogMessage = this.getLogMessage,
        getCallResponseContent = this.getCallResponseContent,
    )

    override fun withLog(
        log: ((String) -> Unit)?
    ): ExceptionHandlerBuilderImpl = ExceptionHandlerBuilderImpl(
        httpStatusCode = this.httpStatusCode,
        log = log,
        getLogMessage = this.getLogMessage,
        getCallResponseContent = this.getCallResponseContent,
    )

    override fun withGetLogMessage(
        getLogMessage: (exception: Throwable) -> String
    ): ExceptionHandlerBuilderImpl = ExceptionHandlerBuilderImpl(
        httpStatusCode = this.httpStatusCode,
        log = this.log,
        getLogMessage = getLogMessage,
        getCallResponseContent = this.getCallResponseContent,
    )

    override fun withGetCallResponseContent(
        getCallResponseContent: (exception: Throwable, logMessage: String) -> String
    ): ExceptionHandlerBuilderImpl = ExceptionHandlerBuilderImpl(
        httpStatusCode = this.httpStatusCode,
        log = this.log,
        getLogMessage = this.getLogMessage,
        getCallResponseContent = getCallResponseContent,
    )

    override fun <ExceptionType : Throwable> build(): ExceptionHandler<ExceptionType> {
        return { call, cause ->
            val logMessage = getLogMessage(cause)
            val response = getCallResponseContent(cause, logMessage)
            log?.invoke(logMessage)
            call.respondText(status = httpStatusCode, text = response)
        }
    }
}
