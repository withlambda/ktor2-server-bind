package com.withlambda.ktor2.server.bind.response

import kotlinx.coroutines.flow.Flow

/**
 * Specialized [ResponseData] for returning a file as the response body.
 *
 * @property file The [java.io.File] to send as the response.
 */
interface ResponseDataWithFile : ResponseData {
    /**
     * The file to send as the response body.
     */
    val file: java.io.File
}

/**
 * Specialized [ResponseData] for returning a raw byte array as the response body.
 *
 * @property bytes The byte array to send as the response body.
 */
interface ResponseDataWithBytes : ResponseData {
    /**
     * The byte array to send as the response body.
     */
    val bytes: ByteArray
}

/**
 * Specialized [ResponseData] for streaming the response body via an [java.io.OutputStream].
 *
 * @property producer A suspending function that writes content to the provided [java.io.OutputStream].
 */
interface ResponseDataWithOutputStream : ResponseData {
    /**
     * Suspending producer that writes the response content to an [java.io.OutputStream].
     */
    val producer: suspend (java.io.OutputStream) -> Unit
}

/**
 * A single Server-Sent Event to be streamed to the client.
 *
 * @property data The payload of the event, written as the `data:` field in the SSE stream.
 */
data class ServerSentEvent(
    /**
     * The payload of the event.
     */
    val data: String
)

/**
 * Specialized [ResponseData] for streaming Server-Sent Events (SSE) to the client.
 *
 * The response will use `Content-Type: text/event-stream` and emit each [ServerSentEvent]
 * from [events] as a `data: <value>\n\n` line.
 *
 * @property events A [Flow] of [ServerSentEvent] instances to emit to the client.
 */
interface ResponseDataWithSSE : ResponseData {
    /**
     * Flow of [ServerSentEvent] instances to stream to the client.
     */
    val events: Flow<ServerSentEvent>
}
