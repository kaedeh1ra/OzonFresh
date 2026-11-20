package com.example.app.di

import com.example.app.data.di.dataModule
import com.example.app.domain.di.domainModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.mp.KoinPlatform

/** Всё приложение: core-модули и фичи, найденные при сборке ([featureModules] генерирует Gradle). */
val appModules: List<Module> get() = listOf(dataModule, domainModule) + featureModules

/** Запускает Koin один раз на процесс. Android зовёт из Activity.onCreate: при пересоздании Activity граф остаётся прежним. */
fun initKoin() {
    if (KoinPlatform.getKoinOrNull() == null) startKoin { modules(appModules) }
}
