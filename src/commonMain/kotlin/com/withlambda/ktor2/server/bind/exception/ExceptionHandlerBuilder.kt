package io.withlambda.ktor2.server.bind.exception

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import kotlin.reflect.KClass

/**
 * Function signature type of exception handlers
 */
typealias ExceptionHandler<ExceptionType> = suspend (call: ApplicationCall, exception: ExceptionType) -> Unit

/**
 * Interface to specify parameters, properties, and function
 * for implementations of builders for exception handlers
 * @property httpStatusCode The http status code that should be responded for the exception that are handled
 * by the built handler
 * @property log A logger function that needs to be specified if exception messages should be logged
 * @property getLogMessage A lambda function that takes the exception as input and produces as output the message
 * that should be logged
 * @property getCallResponseContent A lambda function that takes as input the exception and the log message and
 * produces as output the call response message or body
 * @property build Function that returns the exception handler of type [ExceptionHandler]
 */
interface ExceptionHandlerBuilder {
    /**
     * The http status code that should be responded for the exception that is handled by the built handler
     */
    val httpStatusCode: HttpStatusCode

    /**
     * A logger function that needs to be specified if exception messages should be logged
     */
    val log: ((String) -> Unit)?

    /**
     * A lambda function that takes the exception as input and produces as output the message that should be logged
     */
    val getLogMessage: (exception: Throwable) -> String

    /**
     * A lambda function that takes as input the exception and the log message and produces as output the call
     * response message or body
     */
    val getCallResponseContent: (exception: Throwable, logMessage: String) -> String

    /**
     * Return the instance of [ExceptionHandlerBuilder] with another
     * value for the [httpStatusCode] property
     * @param httpStatusCode Another value for the http status code
     * @return Instance of [ExceptionHandlerBuilder] with changed value
     */
    fun withHttpStatusCode(httpStatusCode: HttpStatusCode): ExceptionHandlerBuilder

    /**
     * Return the instance of [ExceptionHandlerBuilder] with another
     * value for the [log] property
     * @param log Another value for the log function
     * @return Instance of [ExceptionHandlerBuilder] with changed value
     */
    fun withLog(log: ((String) -> Unit)?): ExceptionHandlerBuilder

    /**
     * Return the instance of [ExceptionHandlerBuilder] with another
     * value for the [getLogMessage] lambda function
     * @param getLogMessage Another value for the get log message lambda function
     * @return Instance of [ExceptionHandlerBuilder] with changed value
     */
    fun withGetLogMessage(getLogMessage: (exception: Throwable) -> String): ExceptionHandlerBuilder

    /**
     * Return the instance of [ExceptionHandlerBuilder] with another
     * value for the [getCallResponseContent] lambda function
     * @param getCallResponseContent Another value for the get call response content lambda function
     * @return Instance of [ExceptionHandlerBuilder] with changed value
     */
    fun withGetCallResponseContent(
        getCallResponseContent: (exception: Throwable, logMessage: String) -> String
    ): ExceptionHandlerBuilder

    /**
     * Registers a handler for the given exception class within this builder's DSL scope.
     * When [tryHandle] is subsequently called, the registered handler is invoked if the
     * thrown exception is an instance of [exceptionClass].
     *
     * Prefer the inline reified [exception] extension function over calling this directly.
     * @param ExceptionType The exception type to register a handler for
     * @param exceptionClass The [KClass] of the exception to handle
     * @param handler The suspending handler invoked with the [ApplicationCall] and the exception
     */
    fun <ExceptionType : Throwable> registerExceptionHandler(
        exceptionClass: KClass<ExceptionType>,
        handler: ExceptionHandler<ExceptionType>
    )

    /**
     * Attempts to handle [cause] using a previously registered typed handler.
     * Returns `true` if a matching handler was found and invoked, `false` otherwise,
     * allowing the caller to re-throw and let global [io.ktor.server.plugins.statuspages.StatusPages] handle it.
     * @param call The current [ApplicationCall]
     * @param cause The exception to attempt to handle
     * @return `true` if handled, `false` if no registered handler matched
     */
    suspend fun tryHandle(call: ApplicationCall, cause: Throwable): Boolean

    /**
     * The function that returns the exception handler of type [ExceptionHandler]
     * @param ExceptionType Specified type of the exception to be handled
     * @return The exception handler specified by this builder
     */
    fun <ExceptionType : Throwable> build(): ExceptionHandler<ExceptionType>

    companion object {
        /**
         * A function that can be used to produce a default response and/or log message
         * of the occurred exception. In case the exception carries a message, the message is returned,
         * else the message specifies which type of exception occurred.
         * @param cause The occurred exception
         */
        fun <ExceptionType : Throwable> defaultGetResponseMessage(
            cause: ExceptionType
        ): String {
            return cause.message
                ?: ((cause::class.simpleName ?: "Unknown exception") + " occurred")
        }
    }
}

/**
 * Convenience inline reified overload that registers a typed exception handler on this
 * [ExceptionHandlerBuilder] without requiring an explicit [KClass] argument.
 *
 * Use this inside the [io.withlambda.ktor2.server.bind.Controller.exceptionHandler] DSL block:
 * ```
 * override val exceptionHandler: (ExceptionHandlerBuilder.() -> Unit) = {
 *     exception<MyException> { call, e -> call.respond(HttpStatusCode.NotFound, e.message) }
 * }
 * ```
 * @param ExceptionType The exception type to register a handler for
 * @param handler The suspending handler invoked with the [ApplicationCall] and the exception
 */
inline fun <reified ExceptionType : Throwable> ExceptionHandlerBuilder.exception(
    noinline handler: ExceptionHandler<ExceptionType>
) = registerExceptionHandler(ExceptionType::class, handler)
