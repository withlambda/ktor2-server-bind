package io.withlambda.ktor2.server.bind.util

import io.ktor.http.Headers
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import io.withlambda.ktor2.server.bind.exception.HeaderConversionException
import io.withlambda.ktor2.server.bind.exception.MissingHeaderException
import kotlin.reflect.KProperty

/**
 * The abstract class that serves as the base class of all instantiatable
 * class implementations of parameter handler
 * @property stringValues The stored headers
 * @property throwConversionException Function with the sole purpose to throw the specified conversion exception
 * in case a header value could not be cast to the target type
 */
abstract class AbstractHeadersHandler : AbstractStringValuesHandler(), StringValuesHandler {

    /**
     * The stored headers
     */
    abstract override val stringValues: Headers

    /**
     * Function with the sole purpose to throw the specified conversion exception
     * in case a header value could not be cast to the target type
     * @param name Name of the header
     * @param type Name of the type this header value is unable to convert to
     * @param cause The originally thrown exception
     * @return returns [Nothing], only throws
     * @throws HeaderConversionException
     */
    @Throws(HeaderConversionException::class)
    override fun throwConversionException(name: String, type: String, cause: Throwable?): Nothing {
        throw HeaderConversionException(name, type, cause)
    }
}

/**
 * Handler for required HTTP headers
 * @param stringValues Instance of [Headers] of which values are read and converted
 * @param conversionService Instance of an [ConversionService]
 */
class RequiredHeadersHandler(
    override val stringValues: Headers,
    override val conversionService: ConversionService = DefaultConversionService
) : AbstractHeadersHandler(), StringValuesHandler {

    /**
     * Delegate operator to retrieve the header value
     * @param R The type of the header value
     * @param thisRef Instance of the property (header) owner
     * @param property The kProperty instance
     * @return The header value of type [R]
     * @throws MissingHeaderException when the header is not available
     */
    @Throws(MissingHeaderException::class)
    inline operator fun <reified R> getValue(thisRef: Any?, property: KProperty<*>): R {
        return getAndConvertOrNull(property.name, typeInfo<R>())
            ?: throw MissingHeaderException(property.name)
    }
}

/**
 * Handler for optional HTTP headers
 * @param stringValues Instance of [Headers] of which values are read and converted
 * @param conversionService Instance of an [ConversionService]
 */
class OptionalHeadersHandler(
    override val stringValues: Headers,
    override val conversionService: ConversionService = DefaultConversionService
) : AbstractHeadersHandler(), StringValuesHandler {

    /**
     * Delegate operator to retrieve the header value
     * @param R The type of the header value
     * @param thisRef Instance of the property (header) owner
     * @param property The kProperty instance
     * @return The header value of type [R] or null if not available
     */
    inline operator fun <reified R> getValue(
        @Suppress("UNUSED_PARAMETER") thisRef: Any?,
        property: KProperty<*>
    ): R? {
        return getAndConvertOrNull(property.name, typeInfo<R>())
    }
}
