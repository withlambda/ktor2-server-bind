package com.withlambda.ktor2.server.bind.util

import io.ktor.http.Parameters
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import kotlin.reflect.KProperty

/**
 * The abstract class that serves as the base class of all instantiatable
 * class implementations of parameter handler
 * @property stringValues The stored parameters
 * @property throwConversionException Function with the sole purpose to throw the specified conversion exception
 * in case a parameter value could not be cast to the target type
 */
abstract class AbstractParametersHandler : AbstractStringValuesHandler(), StringValuesHandler {

    /**
     * The stored parameters
     */
    abstract override val stringValues: Parameters

    /**
     * Function with the sole purpose to throw the specified conversion exception
     * in case a parameter value could not be cast to the target type
     * @param name Name of the parameter
     * @param type Name of the type this parameter value is unable to convert to
     * @param cause The originally thrown exception
     * @return returns [Nothing], only throws
     * @throws ParameterConversionException
     */
    override fun throwConversionException(name: String, type: String, cause: Throwable?): Nothing {
        throw ParameterConversionException(name, type, cause)
    }
}

/**
 * Handler for required HTTP parameters
 * @param stringValues Instance of [Parameters] of which values are read and converted
 * @param conversionService Instance of an [ConversionService]
 */
class RequiredParametersHandler(
    override val stringValues: Parameters,
    override val conversionService: ConversionService = DefaultConversionService
) : AbstractParametersHandler(), StringValuesHandler {

    /**
     * Delegate operator to retrieve the parameter value
     * @param R The type of the parameter value
     * @param thisRef Instance of the property (parameter) owner
     * @param property The kProperty instance
     * @return The parameter value of type [R]
     * @throws MissingRequestParameterException when the parameter is not available
     */
    @Throws(MissingRequestParameterException::class)
    inline operator fun <reified R> getValue(thisRef: Any?, property: KProperty<*>): R {
        return getAndConvertOrNull(property.name, typeInfo<R>())
            ?: throw MissingRequestParameterException(property.name)
    }
}

/**
 * Handler for optional HTTP parameters
 * @param stringValues Instance of [Parameters] of which values are read and converted
 * @param conversionService Instance of an [ConversionService]
 */
class OptionalParametersHandler(
    override val stringValues: Parameters,
    override val conversionService: ConversionService = DefaultConversionService
) : AbstractParametersHandler(), StringValuesHandler {

    /**
     * Delegate operator to retrieve the parameter value
     * @param R The type of the parameter value
     * @param thisRef Instance of the property (parameter) owner
     * @param property The kProperty instance
     * @return The parameter value of type [R] or null if not available
     */
    inline operator fun <reified R> getValue(thisRef: Any?, property: KProperty<*>): R? {
        return getAndConvertOrNull(property.name, typeInfo<R>())
    }
}
