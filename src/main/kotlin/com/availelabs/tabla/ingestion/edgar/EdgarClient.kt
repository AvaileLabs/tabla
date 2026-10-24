package com.availelabs.tabla.ingestion.edgar

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.InputStream
import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration.Companion.milliseconds

@Component
class EdgarClient(
    builder: RestClient.Builder,
    properties: EdgarProperties
) {
    private val lock = ReentrantLock()

    private val client = builder
        .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent)
        .requestInterceptor { request, body, execution ->
            lock.withLock {
                // Very conservative Edgar throttler; fine at current scale of ingestion.
                Thread.sleep(110.milliseconds.inWholeMilliseconds)
                execution.execute(request, body)
            }
        }
        .build()

    fun <T : Any> withResponseBody(
        uri: URI,
        block: (InputStream) -> T
    ): T =
        client.get()
            .uri(uri)
            .exchangeForRequiredValue { _, response ->
                if (response.statusCode.isError) {
                    throw response.createException()
                }
                block(response.body)
            }
}