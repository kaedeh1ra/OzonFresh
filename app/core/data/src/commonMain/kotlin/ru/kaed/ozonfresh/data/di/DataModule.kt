package ru.kaed.ozonfresh.data.di

import ru.kaed.ozonfresh.data.platform.KeyValueStorage
import ru.kaed.ozonfresh.data.platform.PlatformKeyValueStorage
import ru.kaed.ozonfresh.data.platform.apiBaseUrl
import ru.kaed.ozonfresh.data.remote.createHttpClient
import ru.kaed.ozonfresh.data.repository.CounterRepositoryImpl
import ru.kaed.ozonfresh.domain.repository.CounterRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Всё — single: хранилище, HTTP-клиент и репозитории общие для всех экранов. */
val dataModule = module {
    single<KeyValueStorage> { PlatformKeyValueStorage }
    // Готовый клиент для API: репозиторий берёт его через get() и ходит по относительным путям.
    single { createHttpClient(apiBaseUrl) }

    singleOf(::CounterRepositoryImpl) bind CounterRepository::class
}
