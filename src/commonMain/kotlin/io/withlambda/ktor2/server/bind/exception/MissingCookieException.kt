package io.withlambda.ktor2.server.bind.exception

import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * This exception is thrown when a required cookie with name [cookieName] is missing
 * @property cookieName Name of the cookie which is missing in the request
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MissingCookieException(
    @Suppress("MemberVisibilityCanBePrivate") val cookieName: String,
    cause: Throwable? = null
) : BadRequestException("Cookie $cookieName is missing", cause),
    CopyableThrowable<MissingCookieException> {

    override fun createCopy(): MissingCookieException =
        MissingCookieException(cookieName, this)
}
