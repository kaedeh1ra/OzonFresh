package ru.kaed.ozonfresh.data

import ru.kaed.ozonfresh.data.remote.createHttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpClientFactoryTest {

    @Serializable
    private data class Item(val id: Int)

    @Test
    fun resolvesRelativePathsAndParsesJson() = runTest {
        var requested = ""
        val engine = MockEngine { request ->
            requested = request.url.toString()
            respond(
                """{"id": 1, "extra": "ignored"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val item: Item =
            createHttpClient("https://api.example.com/v1/", engine).get("items/1").body()

        assertEquals("https://api.example.com/v1/items/1", requested)
        assertEquals(Item(1), item)
    }
}
