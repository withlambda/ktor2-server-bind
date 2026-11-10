package com.withlambda.ktor2.server.bind.exception

import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * This exception is thrown when a cookie value could not be converted to the target type [targetType]
 * @property cookieName Name of the cookie
 * @property targetType Name of the target type
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CookieConversionException(
    @Suppress("MemberVisibilityCanBePrivate") val cookieName: String,
    @Suppress("MemberVisibilityCanBePrivate") val targetType: String,
    cause: Throwable? = null
) : BadRequestException("Cookie $cookieName could not be converted to $targetType", cause),
    CopyableThrowable<CookieConversionException> {

    override fun createCopy(): CookieConversionException =
        CookieConversionException(cookieName, targetType, this)
}
