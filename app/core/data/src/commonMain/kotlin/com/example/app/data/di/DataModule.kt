package com.example.app.data.di

import com.example.app.data.platform.KeyValueStorage
import com.example.app.data.platform.PlatformKeyValueStorage
import com.example.app.data.platform.apiBaseUrl
import com.example.app.data.remote.createHttpClient
import com.example.app.data.repository.CounterRepositoryImpl
import com.example.app.domain.repository.CounterRepository
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
