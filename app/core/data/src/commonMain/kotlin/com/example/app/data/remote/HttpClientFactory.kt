package com.example.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MS = 30_000L

/** JSON API: неизвестные поля не роняют разбор, чтобы бэкенд мог добавлять поля без релиза клиента. */
val ApiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

/**
 * HTTP-клиент для API: относительные пути запросов дописываются к [baseUrl].
 * [engine] нужен тестам; в приложении движок выбирается платформой (OkHttp на Android, fetch в браузере).
 */
fun createHttpClient(baseUrl: String, engine: HttpClientEngine? = null): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(ContentNegotiation) { json(ApiJson) }
        // В браузере у fetch нет своего тайм-аута.
        install(HttpTimeout) { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
        defaultRequest { url(baseUrl) }
    }
    return if (engine != null) HttpClient(engine, config) else HttpClient(config)
}
