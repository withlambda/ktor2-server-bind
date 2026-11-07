package com.withlambda.ktor2.server.bind.request

/**
 * Interface to specify request data for a route with multipart/form-data
 * This interface will be implemented by DTOs containing fields of type String (form fields) or FileItemDTO.
 */
interface RequestFormData : RequestData
