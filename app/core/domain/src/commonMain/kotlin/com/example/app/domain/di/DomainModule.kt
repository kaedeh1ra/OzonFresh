package com.example.app.domain.di

import com.example.app.domain.usecase.counter.IncrementCounterUseCase
import com.example.app.domain.usecase.counter.ObserveCounterUseCase
import com.example.app.domain.usecase.counter.ResetCounterUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Use case'ы без состояния: каждый экран получает свой экземпляр, репозитории под ними общие (см. dataModule). */
val domainModule = module {
    factoryOf(::ObserveCounterUseCase)
    factoryOf(::IncrementCounterUseCase)
    factoryOf(::ResetCounterUseCase)
}
