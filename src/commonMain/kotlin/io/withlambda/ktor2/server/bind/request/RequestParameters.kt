package io.withlambda.ktor2.server.bind.request

import io.withlambda.ktor2.server.bind.Parameters

/**
 * Marker interface for request parameters
 */
sealed interface RequestParameters : Parameters

/**
 * Marker interface for request path parameters
 */
interface PathParameters : RequestParameters

/**
 * Marker interface for optional request query parameters
 */
interface OptionalQueryParameters : RequestParameters

/**
 * Marker interface for required request query parameters
 */
interface RequiredQueryParameters : RequestParameters
