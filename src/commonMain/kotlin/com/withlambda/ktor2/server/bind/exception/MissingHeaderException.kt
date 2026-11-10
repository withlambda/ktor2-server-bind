package com.withlambda.ktor2.server.bind.exception

import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * This exception is thrown when a required header with name [headerName] is missing
 * @property headerName Name of the header which is missing in the request
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MissingHeaderException(
    @Suppress("MemberVisibilityCanBePrivate") val headerName: String,
    cause: Throwable? = null
) : BadRequestException("Header $headerName is missing", cause),
    CopyableThrowable<MissingHeaderException> {

    override fun createCopy(): MissingHeaderException =
        MissingHeaderException(headerName, this)
}
