package com.withlambda.ktor2.server.bind.exception

import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * This exception is thrown when the value of a header with name [headerName] couldn't be converted to the specified
 * [type]
 * @property headerName Name of the header for which value conversion failed
 * @property type Name of the type this header value is unable to convert to
 * @property cause The originally thrown exception
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HeaderConversionException(
    @Suppress("MemberVisibilityCanBePrivate") val headerName: String,
    val type: String,
    cause: Throwable? = null
) : BadRequestException("value of header $headerName couldn't be parsed/converted to $type", cause),
    CopyableThrowable<HeaderConversionException> {

    override fun createCopy(): HeaderConversionException =
        HeaderConversionException(headerName, type, this)
}
