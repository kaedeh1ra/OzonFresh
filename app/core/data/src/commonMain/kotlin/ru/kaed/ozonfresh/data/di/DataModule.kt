package ru.kaed.ozonfresh.data.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.kaed.ozonfresh.data.platform.KeyValueStorage
import ru.kaed.ozonfresh.data.platform.PlatformKeyValueStorage
import ru.kaed.ozonfresh.data.platform.apiBaseUrl
import ru.kaed.ozonfresh.data.remote.createHttpClient
import ru.kaed.ozonfresh.data.repository.InMemoryCartRepository
import ru.kaed.ozonfresh.data.repository.InMemoryDemoSettingsRepository
import ru.kaed.ozonfresh.data.repository.InMemoryPilotEventRepository
import ru.kaed.ozonfresh.data.repository.InMemoryPurchaseRepository
import ru.kaed.ozonfresh.data.repository.InMemoryReplenishmentFeedbackRepository
import ru.kaed.ozonfresh.data.repository.InMemoryStockRepository
import ru.kaed.ozonfresh.data.repository.MockCatalogRepository
import ru.kaed.ozonfresh.domain.repository.CartRepository
import ru.kaed.ozonfresh.domain.repository.CatalogRepository
import ru.kaed.ozonfresh.domain.repository.DemoSettingsRepository
import ru.kaed.ozonfresh.domain.repository.PilotEventRepository
import ru.kaed.ozonfresh.domain.repository.PurchaseRepository
import ru.kaed.ozonfresh.domain.repository.ReplenishmentFeedbackRepository
import ru.kaed.ozonfresh.domain.repository.StockRepository

/** Всё — single: хранилище, HTTP-клиент и репозитории общие для всех экранов. */
val dataModule = module {
    single<KeyValueStorage> { PlatformKeyValueStorage }
    // Бэкенда у прототипа нет, клиент оставлен из шаблона для будущего API.
    single { createHttpClient(apiBaseUrl) }

    // Моковые данные в памяти: см. MockShopData.
    singleOf(::MockCatalogRepository) bind CatalogRepository::class
    singleOf(::InMemoryPurchaseRepository) bind PurchaseRepository::class
    singleOf(::InMemoryStockRepository) bind StockRepository::class
    singleOf(::InMemoryCartRepository) bind CartRepository::class
    singleOf(::InMemoryDemoSettingsRepository) bind DemoSettingsRepository::class
    singleOf(::InMemoryReplenishmentFeedbackRepository) bind ReplenishmentFeedbackRepository::class
    singleOf(::InMemoryPilotEventRepository) bind PilotEventRepository::class
}
