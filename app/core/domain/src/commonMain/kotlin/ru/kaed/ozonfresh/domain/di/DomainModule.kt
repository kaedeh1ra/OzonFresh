package ru.kaed.ozonfresh.domain.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentEngine
import ru.kaed.ozonfresh.domain.usecase.demo.DemoControlsUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.AddFromFeedUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.LaterUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.ObserveReplenishmentDiagnosticsUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.ObserveReplenishmentFeedUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.RegisterFeedImpressionUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.StillHaveUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.AddToCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ChangeCartQuantityUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.GetCatalogUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObservePurchasedProductsUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveTodayUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.PlaceOrderUseCase

/** Use case'ы без состояния: каждый экран получает свой экземпляр, репозитории под ними общие (см. dataModule). */
val domainModule = module {
    factoryOf(::ReplenishmentEngine)

    factoryOf(::ObserveReplenishmentFeedUseCase)
    factoryOf(::ObserveReplenishmentDiagnosticsUseCase)
    factoryOf(::RegisterFeedImpressionUseCase)
    factoryOf(::AddFromFeedUseCase)
    factoryOf(::StillHaveUseCase)
    factoryOf(::LaterUseCase)

    factoryOf(::GetCatalogUseCase)
    factoryOf(::ObservePurchasedProductsUseCase)
    factoryOf(::ObserveCartUseCase)
    factoryOf(::AddToCartUseCase)
    factoryOf(::ChangeCartQuantityUseCase)
    factoryOf(::ObserveTodayUseCase)
    factoryOf(::PlaceOrderUseCase)

    factoryOf(::DemoControlsUseCase)
}
