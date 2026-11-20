package com.example.app.presentation.home

import androidx.navigation.NavGraphBuilder
import com.example.app.navigation.Destinations
import com.example.app.navigation.FeatureEntry
import com.example.app.navigation.FeatureHost
import com.example.app.navigation.FeaturePlugin
import com.example.app.navigation.screen
import com.example.app.presentation.designsystem.components.NavIcon
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal class HomePlugin : FeaturePlugin {
    override val routes = listOf(Destinations.HOME)
    override val entry = FeatureEntry.Tab("Главная", NavIcon.Home, order = 0)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.HOME) { HomeRoute() }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `homeFeatureModule` в этом пакете. */
val homeFeatureModule = module {
    viewModelOf(::HomeViewModel)
    singleOf(::HomePlugin) bind FeaturePlugin::class
}
