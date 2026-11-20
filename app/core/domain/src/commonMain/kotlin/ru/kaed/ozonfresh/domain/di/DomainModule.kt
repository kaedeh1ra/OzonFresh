package ru.kaed.ozonfresh.domain.di

import ru.kaed.ozonfresh.domain.usecase.counter.IncrementCounterUseCase
import ru.kaed.ozonfresh.domain.usecase.counter.ObserveCounterUseCase
import ru.kaed.ozonfresh.domain.usecase.counter.ResetCounterUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Use case'ы без состояния: каждый экран получает свой экземпляр, репозитории под ними общие (см. dataModule). */
val domainModule = module {
    factoryOf(::ObserveCounterUseCase)
    factoryOf(::IncrementCounterUseCase)
    factoryOf(::ResetCounterUseCase)
}
