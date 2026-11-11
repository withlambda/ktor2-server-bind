package io.withlambda.ktor2.server.bind.fixtures.services

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.CreateHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.DeleteHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.GetHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.HeadHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.OptionsHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.PatchHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.controllers.helloworld.UpdateHelloWorld
import io.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldResponseDTO
import io.withlambda.ktor2.server.bind.fixtures.services.HelloWorldService.optionsHelloWorld

/**
 * Stub service that handles all Hello World route operations used by the test fixtures.
 *
 * Each handler accesses every field of the incoming request data (path parameters, query
 * parameters, and headers) to exercise the full parameter-binding pipeline, then builds a
 * [HelloWorldResponseDTO] that echoes those values back so that tests can assert correctness.
 *
 * Response headers are always populated with fixed values (`responseHeader1 = 1`,
 * `responseHeader2 = "2"`), except for [optionsHelloWorld] which additionally sets the
 * `Allow` header.
 */
object HelloWorldService {

    /**
     * Handles a POST request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request], then returns a
     * [CreateHelloWorld.ResponseDataImpl] with HTTP 200 and a [HelloWorldResponseDTO] that echoes
     * all received values together with the request body's
     * [name][io.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldDTO.name].
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the create operation.
     * @return A response data object containing HTTP 200 and the echo response body.
     */
    fun createHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: CreateHelloWorld.RequestDataImpl
    ): CreateHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val requestBody = request.requestBody

        val responseHeaders = CreateHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2"
        )

        return CreateHelloWorld.ResponseDataImpl(
            HttpStatusCode.OK,
            HelloWorldResponseDTO(
                name = requestBody.name,
                count = 1,
                pathParam1 = request.pathParameter.pathParam1,
                pathParam2 = request.pathParameter.pathParam2,
                optionalQueryParam1 = request.optionalQueryParameter.optionalQueryParam1,
                optionalQueryParam2 = request.optionalQueryParameter.optionalQueryParam2,
                requiredQueryParam1 = request.requiredQueryParameter.requiredQueryParam1,
                requiredQueryParam2 = request.requiredQueryParameter.requiredQueryParam2,
                requestHeader1 = request.headers.header1,
                requestHeader2 = request.headers.header2
            ),
            null,
            responseHeaders,
            null
        )
    }

    /**
     * Handles a PUT request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request], then returns a
     * [UpdateHelloWorld.ResponseDataImpl] with HTTP 200 and a [HelloWorldResponseDTO] that echoes
     * all received values together with the request body's
     * [name][io.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldDTO.name].
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the update operation.
     * @return A response data object containing HTTP 200 and the echo response body.
     */
    fun updateHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: UpdateHelloWorld.RequestDataImpl
    ): UpdateHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val requestBody = request.requestBody

        val responseHeaders = UpdateHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2"
        )

        return UpdateHelloWorld.ResponseDataImpl(
            HttpStatusCode.OK,
            HelloWorldResponseDTO(
                name = requestBody.name,
                count = 1,
                pathParam1 = request.pathParameter.pathParam1,
                pathParam2 = request.pathParameter.pathParam2,
                optionalQueryParam1 = request.optionalQueryParameter.optionalQueryParam1,
                optionalQueryParam2 = request.optionalQueryParameter.optionalQueryParam2,
                requiredQueryParam1 = request.requiredQueryParameter.requiredQueryParam1,
                requiredQueryParam2 = request.requiredQueryParameter.requiredQueryParam2,
                requestHeader1 = request.headers.header1,
                requestHeader2 = request.headers.header2
            ),
            null,
            responseHeaders,
            null
        )
    }

    /**
     * Handles a PATCH request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request], then returns a
     * [PatchHelloWorld.ResponseDataImpl] with HTTP 200 and a [HelloWorldResponseDTO] that echoes
     * all received values together with the request body's
     * [name][io.withlambda.ktor2.server.bind.fixtures.api.models.HelloWorldDTO.name].
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the patch operation.
     * @return A response data object containing HTTP 200 and the echo response body.
     */
    fun patchHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: PatchHelloWorld.RequestDataImpl
    ): PatchHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val requestBody = request.requestBody

        val responseHeaders = PatchHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2"
        )

        return PatchHelloWorld.ResponseDataImpl(
            HttpStatusCode.OK,
            HelloWorldResponseDTO(
                name = requestBody.name,
                count = 1,
                pathParam1 = request.pathParameter.pathParam1,
                pathParam2 = request.pathParameter.pathParam2,
                optionalQueryParam1 = request.optionalQueryParameter.optionalQueryParam1,
                optionalQueryParam2 = request.optionalQueryParameter.optionalQueryParam2,
                requiredQueryParam1 = request.requiredQueryParameter.requiredQueryParam1,
                requiredQueryParam2 = request.requiredQueryParameter.requiredQueryParam2,
                requestHeader1 = request.headers.header1,
                requestHeader2 = request.headers.header2
            ),
            null,
            responseHeaders,
            null
        )
    }

    /**
     * Handles a DELETE request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request] (no request body),
     * then returns a [DeleteHelloWorld.ResponseDataImpl] with HTTP 200 and a fixed
     * [HelloWorldResponseDTO] that echoes the received parameter and header values.
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the delete operation.
     * @return A response data object containing HTTP 200 and the echo response body.
     */
    fun deleteHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: DeleteHelloWorld.RequestDataImpl
    ): DeleteHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val responseHeaders = DeleteHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2"
        )

        return DeleteHelloWorld.ResponseDataImpl(
            HttpStatusCode.OK,
            HelloWorldResponseDTO(
                name = "Test",
                count = 1,
                pathParam1 = request.pathParameter.pathParam1,
                pathParam2 = request.pathParameter.pathParam2,
                optionalQueryParam1 = request.optionalQueryParameter.optionalQueryParam1,
                optionalQueryParam2 = request.optionalQueryParameter.optionalQueryParam2,
                requiredQueryParam1 = request.requiredQueryParameter.requiredQueryParam1,
                requiredQueryParam2 = request.requiredQueryParameter.requiredQueryParam2,
                requestHeader1 = request.headers.header1,
                requestHeader2 = request.headers.header2
            ),
            null,
            responseHeaders,
            null
        )
    }

    /**
     * Handles a GET request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request] (no request body),
     * then returns a [GetHelloWorld.ResponseDataImpl] with HTTP 200 and a fixed
     * [HelloWorldResponseDTO] that echoes the received parameter and header values.
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the get operation.
     * @return A response data object containing HTTP 200 and the echo response body.
     */
    fun getHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: GetHelloWorld.RequestDataImpl
    ): GetHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val responseHeaders = GetHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2"
        )

        return GetHelloWorld.ResponseDataImpl(
            HttpStatusCode.OK,
            HelloWorldResponseDTO(
                name = "Test",
                count = 1,
                pathParam1 = request.pathParameter.pathParam1,
                pathParam2 = request.pathParameter.pathParam2,
                optionalQueryParam1 = request.optionalQueryParameter.optionalQueryParam1,
                optionalQueryParam2 = request.optionalQueryParameter.optionalQueryParam2,
                requiredQueryParam1 = request.requiredQueryParameter.requiredQueryParam1,
                requiredQueryParam2 = request.requiredQueryParameter.requiredQueryParam2,
                requestHeader1 = request.headers.header1,
                requestHeader2 = request.headers.header2
            ),
            null,
            responseHeaders,
            null
        )
    }

    /**
     * Handles a HEAD request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request] (no request body),
     * then returns a [HeadHelloWorld.ResponseDataImpl] with HTTP 200 and no response body,
     * as required by the HTTP HEAD method semantics.
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the head operation.
     * @return A response data object containing HTTP 200 and a `null` response body.
     */
    fun headHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: HeadHelloWorld.RequestDataImpl
    ): HeadHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val responseHeaders = HeadHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2"
        )

        return HeadHelloWorld.ResponseDataImpl(
            HttpStatusCode.OK,
            null,
            null,
            responseHeaders,
            null
        )
    }

    /**
     * Handles an OPTIONS request for the Hello World resource.
     *
     * Reads all path parameters, query parameters, and headers from [request] (no request body),
     * then returns an [OptionsHelloWorld.ResponseDataImpl] with HTTP 204 (No Content) and
     * an `Allow` response header listing all supported HTTP methods.
     *
     * @param context The Ktor pipeline context (unused by this stub).
     * @param request The fully bound request data for the options operation.
     * @return A response data object containing HTTP 204, a `null` response body, and an
     *   `Allow` header enumerating the supported methods.
     */
    fun optionsHelloWorld(
        @Suppress("UNUSED_PARAMETER") context: PipelineContext<Unit, ApplicationCall>,
        request: OptionsHelloWorld.RequestDataImpl
    ): OptionsHelloWorld.ResponseDataImpl {
        request.pathParameter.pathParam1
        request.pathParameter.pathParam2
        request.optionalQueryParameter.optionalQueryParam1
        request.optionalQueryParameter.optionalQueryParam2
        request.requiredQueryParameter.requiredQueryParam1
        request.requiredQueryParameter.requiredQueryParam2

        val responseHeaders = OptionsHelloWorld.ResponseHeadersImpl(
            responseHeader1 = 1,
            responseHeader2 = "2",
            allow = "OPTIONS, GET, HEAD, POST, PATCH, PUT, DELETE"
        )

        return OptionsHelloWorld.ResponseDataImpl(
            HttpStatusCode.NoContent,
            null,
            null,
            responseHeaders,
            null
        )
    }
}
