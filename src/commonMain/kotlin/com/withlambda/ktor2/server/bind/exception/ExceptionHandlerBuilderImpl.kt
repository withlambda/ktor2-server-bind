package com.withlambda.ktor2.server.bind.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import kotlin.reflect.KClass

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

    /**
     * Type-keyed map of registered exception handlers populated via [exception].
     * Mutation is algorithmic (DSL accumulation) and intentionally confined to this class.
     */
    private val handlers: MutableMap<KClass<out Throwable>, ExceptionHandler<*>> = mutableMapOf()

    override fun <ExceptionType : Throwable> registerExceptionHandler(
        exceptionClass: KClass<ExceptionType>,
        handler: ExceptionHandler<ExceptionType>
    ) {
        handlers[exceptionClass] = handler
    }

    override suspend fun tryHandle(call: ApplicationCall, cause: Throwable): Boolean {
        val handler = handlers.entries.firstOrNull { (kClass, _) -> kClass.isInstance(cause) }?.value
            ?: return false
        // Safe cast: the map key guarantees the handler accepts Throwable
        @Suppress("UNCHECKED_CAST")
        (handler as ExceptionHandler<Throwable>)(call, cause)
        return true
    }

    override fun <ExceptionType : Throwable> build(): ExceptionHandler<ExceptionType> {
        return { call, cause ->
            val logMessage = getLogMessage(cause)
            val response = getCallResponseContent(cause, logMessage)
            log?.invoke(logMessage)
            call.respondText(status = httpStatusCode, text = response)
        }
    }
}
