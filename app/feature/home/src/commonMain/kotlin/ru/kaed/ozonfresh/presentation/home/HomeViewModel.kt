package ru.kaed.ozonfresh.presentation.home

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.kaed.ozonfresh.domain.model.formatLong
import ru.kaed.ozonfresh.domain.usecase.replenishment.AddFromFeedUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.LaterUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.ObserveReplenishmentFeedUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.RegisterFeedImpressionUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.StillHaveUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.AddToCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ChangeCartQuantityUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.GetCatalogUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObservePurchasedProductsUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveTodayUseCase
import ru.kaed.ozonfresh.presentation.home.HomeContract.Effect
import ru.kaed.ozonfresh.presentation.home.HomeContract.Intent
import ru.kaed.ozonfresh.presentation.home.HomeContract.State
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

class HomeViewModel(
    catalog: GetCatalogUseCase,
    today: ObserveTodayUseCase,
    observeFeed: ObserveReplenishmentFeedUseCase,
    observePurchased: ObservePurchasedProductsUseCase,
    observeCart: ObserveCartUseCase,
    private val registerImpression: RegisterFeedImpressionUseCase,
    private val addFromFeed: AddFromFeedUseCase,
    private val stillHave: StillHaveUseCase,
    private val later: LaterUseCase,
    private val addToCart: AddToCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<State, Intent, Effect>(
    State(
        address = today.address,
        today = today().value,
        categories = catalog.categories,
        hits = catalog.weeklyHits,
    ),
) {

    init {
        today().onEach { setState { copy(today = it) } }.launchIn(viewModelScope)
        observeFeed().onEach { setState { copy(feed = it) } }.launchIn(viewModelScope)
        observePurchased().onEach { setState { copy(purchased = it) } }.launchIn(viewModelScope)
        observeCart().onEach { cart ->
            setState { copy(inCart = cart.items.associate { it.product.id to it.quantity }, cartTotal = cart.itemsTotal) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            Intent.FeedShown -> currentState.feed?.let(registerImpression::invoke)
            is Intent.AddFromFeed -> addFromFeed(intent.item)
            is Intent.StillHave -> {
                val newDue = stillHave(intent.item)
                sendEffect(
                    Effect.ShowMessage("Хорошо, напомним про «${intent.item.product.name}» ближе к ${newDue.formatLong()}"),
                )
            }

            is Intent.Later -> {
                later(intent.item)
                sendEffect(Effect.ShowMessage("Скрыли «${intent.item.product.name}» на 2 дня"))
            }

            is Intent.Add -> addToCart(intent.productId)
            is Intent.ChangeQuantity -> changeQuantity(intent.productId, intent.quantity)
            is Intent.NotInPrototype -> sendEffect(Effect.ShowMessage("«${intent.title}» в прототипе не работает"))
        }
    }
}
