package ru.kaed.ozonfresh.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

/**
 * Забытый `factoryOf` для нового use case падает здесь, а не у пользователя при открытии экрана.
 * Только JVM: проверка читает конструкторы рефлексией.
 */
class KoinVerifyTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun everyDefinitionHasItsDependencies() {
        module { includes(appModules) }.verify(
            // Параметры, которые приходят не из графа: настройки клиента внутри createHttpClient.
            extraTypes = listOf(HttpClientEngine::class, HttpClientConfig::class),
        )
    }
}
