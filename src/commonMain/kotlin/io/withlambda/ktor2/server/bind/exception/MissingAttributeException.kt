package io.withlambda.ktor2.server.bind.exception

import io.ktor.server.plugins.BadRequestException
import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * This exception is thrown when a required attribute with name [attributeName] is missing
 * @property attributeName Name of the attribute which is missing in the request
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MissingAttributeException(
    @Suppress("MemberVisibilityCanBePrivate") val attributeName: String,
    cause: Throwable? = null
) : BadRequestException("Attribute $attributeName is missing", cause),
    CopyableThrowable<MissingAttributeException> {
    override fun createCopy(): MissingAttributeException =
        MissingAttributeException(attributeName, this)
}
