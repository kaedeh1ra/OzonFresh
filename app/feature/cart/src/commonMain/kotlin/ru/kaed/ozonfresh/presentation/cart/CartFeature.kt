package ru.kaed.ozonfresh.presentation.cart

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

/** Маршруты раздела. Оформление и «Заказ оформлен» открываются только из корзины. */
internal object CartRoutes {
    const val CHECKOUT = "checkout"
    const val ORDER_DONE = "order-done/{orderId}/{total}"
    fun orderDone(orderId: String, total: Int) = "order-done/$orderId/$total"
}

internal class CartPlugin : FeaturePlugin {
    override val routes = listOf(Destinations.CART, CartRoutes.CHECKOUT, CartRoutes.ORDER_DONE)
    override val entry = FeatureEntry.Tab("Корзина", NavIcon.Cart, order = 2)
    override val fullScreenRoutes = listOf(CartRoutes.CHECKOUT, CartRoutes.ORDER_DONE)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.CART) { CartRoute(host.navigator) }
        screen(CartRoutes.CHECKOUT) { CheckoutRoute(host.navigator) }
        screen(CartRoutes.ORDER_DONE) { entry ->
            OrderDoneScreen(
                orderId = entry.savedStateHandle.get<String>("orderId").orEmpty(),
                total = entry.savedStateHandle.get<String>("total")?.toIntOrNull() ?: 0,
                onHome = { host.navigator.openClean(Destinations.HOME) },
            )
        }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `cartFeatureModule` в этом пакете. */
val cartFeatureModule = module {
    viewModelOf(::CartViewModel)
    viewModelOf(::CheckoutViewModel)
    singleOf(::CartPlugin) bind FeaturePlugin::class
}
