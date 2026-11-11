package io.withlambda.ktor2.server.bind.util

import io.ktor.util.StringValues
import io.ktor.util.converters.ConversionService
import io.ktor.util.reflect.TypeInfo

/**
 * Interface to get the value of a [StringValues] instance
 * and convert it with an [ConversionService] instance.
 */
@Suppress("EmptyMethod")
interface StringValuesHandler {
    /**
     * Instance of Ktor's HTTP [StringValues]
     */
    val stringValues: StringValues

    /**
     * Instance of an [ConversionService]
     */
    val conversionService: ConversionService

    /**
     * Throws an exception indicating a failure to convert a value to the desired type.
     *
     * @param name the name of the string value attribute that failed value conversion
     * @param type the target type to which the conversion failed
     * @param cause the underlying cause of the conversion failure, if available
     * @return [Nothing] this function never returns as it always throws an exception
     */
    fun throwConversionException(name: String, type: String, cause: Throwable? = null): Nothing

    /**
     * Get a parameter/header value and convert to the specified type [R]
     * with the help of the [ConversionService] instance or
     * return null if parameter/header not available.
     * @param name parameter/header name
     * @param typeInfo the [TypeInfo] instance for the desired type [R]
     * @return parameter/header value converted to the desired type
     */
    fun <R> getAndConvertOrNull(
        name: String,
        typeInfo: TypeInfo,
    ): R?
}

/**
 * The abstract class of the [StringValuesHandler] interface
 * that serves as the base class of all instantiatable
 * class implementations of [StringValuesHandler]
 */
abstract class AbstractStringValuesHandler : StringValuesHandler {

    override fun <R> getAndConvertOrNull(
        name: String,
        typeInfo: TypeInfo,
    ): R? = stringValues.getAll(name)?.let { values ->
        try {
            @Suppress("UNCHECKED_CAST")
            conversionService.fromValues(values, typeInfo) as R
        } catch (@Suppress("TooGenericExceptionCaught") cause: Exception) {
            throwConversionException(name, typeInfo.type.simpleName ?: typeInfo.type.toString(), cause)
        }
    }
}
