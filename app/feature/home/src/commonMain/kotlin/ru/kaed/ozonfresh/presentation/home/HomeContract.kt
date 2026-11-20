package ru.kaed.ozonfresh.presentation.home

import ru.kaed.ozonfresh.domain.model.Category
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentItem
import ru.kaed.ozonfresh.domain.usecase.replenishment.ReplenishmentFeed

object HomeContract {
    data class State(
        val address: String = "",
        val today: Day? = null,
        val categories: List<Category> = emptyList(),
        val feed: ReplenishmentFeed? = null,
        val purchased: List<Product> = emptyList(),
        val hits: List<Product> = emptyList(),
        /** Сколько штук каждого товара в корзине: карточки показывают степпер вместо «+». */
        val inCart: Map<String, Int> = emptyMap(),
        /** Сумма товаров в корзине: купон «Доставка 0 ₽» показывает, сколько осталось. */
        val cartTotal: Int = 0,
    ) {
        /** Что видит клиент: в контрольной группе A/B-теста блока нет. */
        val visibleFeed: List<ReplenishmentItem> get() = feed?.visible.orEmpty()
    }

    sealed interface Intent {
        /** Главная на экране: блок ленты показан (или, в контроле, мог быть показан). */
        data object FeedShown : Intent
        data class AddFromFeed(val item: ReplenishmentItem) : Intent
        data class StillHave(val item: ReplenishmentItem) : Intent
        data class Later(val item: ReplenishmentItem) : Intent
        data class Add(val productId: String) : Intent
        data class ChangeQuantity(val productId: String, val quantity: Int) : Intent

        /** Промо-блок магазина, которого нет в прототипе: показываем подсказку. */
        data class NotInPrototype(val title: String) : Intent
    }

    sealed interface Effect {
        data class ShowMessage(val text: String) : Effect
    }
}
