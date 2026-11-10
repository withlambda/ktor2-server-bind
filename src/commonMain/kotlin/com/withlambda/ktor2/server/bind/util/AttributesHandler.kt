package com.withlambda.ktor2.server.bind.util

import com.withlambda.ktor2.server.bind.exception.MissingAttributeException
import com.withlambda.ktor2.server.bind.request.RequestAttributes
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import kotlin.reflect.KProperty

/**
 * Abstract base class for attribute handlers, providing access to Ktor's [Attributes] container.
 * Subclasses expose typed properties backed by the wrapped [Attributes] instance.
 *
 * @property ktorAttributes The Ktor [Attributes] instance from which values are read.
 *   Marked `@PublishedApi internal` so that public inline subclass operators can access it.
 */
abstract class AbstractAttributesHandler(
    @PublishedApi internal val ktorAttributes: Attributes
) : RequestAttributes

/**
 * Handler for required Ktor attributes.
 * Uses the property name to look up an [AttributeKey] by name in the wrapped [Attributes] container.
 * Throws [MissingAttributeException] when the attribute is absent.
 * @param ktorAttributes The Ktor [Attributes] instance from which values are read
 */
class RequiredAttributesHandler(
    ktorAttributes: Attributes
) : AbstractAttributesHandler(ktorAttributes) {
    /**
     * Delegate operator that retrieves a required attribute value by the delegating property's name.
     * @param T The expected type of the attribute value
     * @param thisRef Instance of the property owner
     * @param property The delegating property; its [KProperty.name] is used as the [AttributeKey] name
     * @return The attribute value of type [T]
     * @throws MissingAttributeException when no attribute with the given name exists
     */
    @Throws(MissingAttributeException::class)
    inline operator fun <reified T : Any> getValue(thisRef: Any?, property: KProperty<*>): T =
        ktorAttributes.getOrNull(AttributeKey(property.name))
            ?: throw MissingAttributeException(property.name)
}

/**
 * Handler for optional Ktor attributes.
 * Uses the property name to look up an [AttributeKey] by name in the wrapped [Attributes] container.
 * Returns null when the attribute is absent.
 * @param ktorAttributes The Ktor [Attributes] instance from which values are read
 */
class OptionalAttributesHandler(
    ktorAttributes: Attributes
) : AbstractAttributesHandler(ktorAttributes) {
    /**
     * Delegate operator that retrieves an optional attribute value by the delegating property's name.
     * @param T The expected type of the attribute value
     * @param thisRef Instance of the property owner
     * @param property The delegating property; its [KProperty.name] is used as the [AttributeKey] name
     * @return The attribute value of type [T], or null if absent
     */
    inline operator fun <reified T : Any> getValue(
        @Suppress("UNUSED_PARAMETER") thisRef: Any?,
        property: KProperty<*>
    ): T? = ktorAttributes.getOrNull(AttributeKey(property.name))
}
