package ru.kaed.ozonfresh.presentation.home

import androidx.navigation.NavGraphBuilder
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureEntry
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.screen
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon

internal class HomePlugin : FeaturePlugin {
    override val routes = listOf(Destinations.HOME)
    override val entry = FeatureEntry.Tab("Продукты", NavIcon.Products, order = 0)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.HOME) { HomeRoute(host.navigator) }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `homeFeatureModule` в этом пакете. */
val homeFeatureModule = module {
    viewModelOf(::HomeViewModel)
    singleOf(::HomePlugin) bind FeaturePlugin::class
}
