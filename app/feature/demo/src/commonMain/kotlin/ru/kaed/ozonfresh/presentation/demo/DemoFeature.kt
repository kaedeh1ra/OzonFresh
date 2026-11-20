package ru.kaed.ozonfresh.presentation.demo

import androidx.navigation.NavGraphBuilder
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.screen

/** Демо-панель без ссылки в навигации: долгое нажатие на адрес на главной или строка версии в профиле. */
internal class DemoPlugin : FeaturePlugin {
    override val routes = listOf(Destinations.DEMO)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.DEMO) { DemoRoute(host.navigator) }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `demoFeatureModule` в этом пакете. */
val demoFeatureModule = module {
    viewModelOf(::DemoViewModel)
    singleOf(::DemoPlugin) bind FeaturePlugin::class
}
