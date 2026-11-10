package com.withlambda.ktor2.server.bind.util

import com.withlambda.ktor2.server.bind.exception.CookieConversionException
import com.withlambda.ktor2.server.bind.exception.MissingCookieException
import com.withlambda.ktor2.server.bind.request.RequestCookies
import io.ktor.util.StringValues
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import kotlin.reflect.KProperty

/**
 * The abstract class that serves as the base class of all instantiatable
 * class implementations of cookie handler
 * @property stringValues The stored cookies
 * @property throwConversionException Function with the sole purpose to throw the specified conversion exception
 * in case a cookie value could not be cast to the target type
 */
abstract class AbstractCookiesHandler : AbstractStringValuesHandler(), StringValuesHandler {

    /**
     * The stored cookies
     */
    abstract override val stringValues: StringValues

    /**
     * Function with the sole purpose to throw the specified conversion exception
     * in case a cookie value could not be cast to the target type
     * @param name Name of the cookie
     * @param type Name of the type this cookie value is unable to convert to
     * @param cause The originally thrown exception
     * @return returns [Nothing], only throws
     * @throws CookieConversionException
     */
    @Throws(CookieConversionException::class)
    override fun throwConversionException(name: String, type: String, cause: Throwable?): Nothing {
        throw CookieConversionException(name, type, cause)
    }
}

/**
 * Handler for required HTTP cookies
 * @param stringValues Instance of [StringValues] of which values are read and converted
 * @param conversionService Instance of an [ConversionService]
 */
class RequiredCookiesHandler(
    override val stringValues: StringValues,
    override val conversionService: ConversionService = DefaultConversionService
) : AbstractCookiesHandler(), RequestCookies {

    /**
     * Delegate operator to retrieve the cookie value
     * @param R The type of the cookie value
     * @param thisRef Instance of the property (cookie) owner
     * @param property The kProperty instance
     * @return The cookie value of type [R]
     * @throws MissingCookieException when the cookie is not available
     */
    @Throws(MissingCookieException::class)
    inline operator fun <reified R> getValue(thisRef: Any?, property: KProperty<*>): R {
        return getAndConvertOrNull(property.name, typeInfo<R>())
            ?: throw MissingCookieException(property.name)
    }
}

/**
 * Handler for optional HTTP cookies
 * @param stringValues Instance of [StringValues] of which values are read and converted
 * @param conversionService Instance of an [ConversionService]
 */
class OptionalCookiesHandler(
    override val stringValues: StringValues,
    override val conversionService: ConversionService = DefaultConversionService
) : AbstractCookiesHandler(), RequestCookies {

    /**
     * Delegate operator to retrieve the cookie value
     * @param R The type of the cookie value
     * @param thisRef Instance of the property (cookie) owner
     * @param property The kProperty instance
     * @return The cookie value of type [R] or null if not available
     */
    inline operator fun <reified R> getValue(
        @Suppress("UNUSED_PARAMETER") thisRef: Any?,
        property: KProperty<*>
    ): R? {
        return getAndConvertOrNull(property.name, typeInfo<R>())
    }
}
