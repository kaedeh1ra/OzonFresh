package ru.kaed.ozonfresh.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.kaed.ozonfresh.domain.model.CartLine
import ru.kaed.ozonfresh.domain.model.CartSource
import ru.kaed.ozonfresh.domain.model.Category
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.ExperimentGroup
import ru.kaed.ozonfresh.domain.model.PilotEvent
import ru.kaed.ozonfresh.domain.model.PilotEventType
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.Purchase
import ru.kaed.ozonfresh.domain.model.Stock
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentFeedback

// Бэкенда нет: реализации в :app:core:data держат моковые данные в памяти, сброс возвращает исходные.

interface CatalogRepository {
    val categories: List<Category>
    val products: List<Product>

    /** Подборка «Хиты недели». */
    val weeklyHits: List<Product>

    fun product(id: String): Product?
}

/** История покупок демо-клиента. */
interface PurchaseRepository {
    val purchases: StateFlow<List<Purchase>>

    fun nextOrderId(): String
    fun add(purchases: List<Purchase>)
    fun reset()
}

/** Остатки на складе текущего адреса. */
interface StockRepository {
    val stock: StateFlow<List<Stock>>

    fun set(productId: String, quantity: Int)
    fun reset()
}

interface CartRepository {
    val lines: StateFlow<List<CartLine>>

    /** +1 штука. Источник запоминается при первом добавлении и не теряется при «+» из корзины. */
    fun add(productId: String, source: CartSource)
    fun setQuantity(productId: String, quantity: Int)
    fun clear()
}

/** Настройки демо: «сегодняшняя» дата, группа A/B-теста и адрес доставки. */
interface DemoSettingsRepository {
    val today: StateFlow<Day>
    val group: StateFlow<ExperimentGroup>
    val address: String

    fun setToday(day: Day)
    fun setGroup(group: ExperimentGroup)
    fun reset()
}

/** Ответы клиента в ленте: «Ещё есть» и «Позже». */
interface ReplenishmentFeedbackRepository {
    val feedback: StateFlow<Map<String, ReplenishmentFeedback>>

    fun put(feedback: ReplenishmentFeedback)
    fun reset()
}

/** Лог событий пилота и учёт эпизодов. */
interface PilotEventRepository {
    val events: StateFlow<List<PilotEvent>>

    /** Окна товаров, которые уже попали в какой-то эпизод. */
    val seenWindows: Set<String>

    /** Запоминает окна и возвращает номер нового эпизода. */
    fun startEpisode(windows: Set<String>): Int

    fun log(
        type: PilotEventType,
        day: Day,
        group: ExperimentGroup,
        details: String,
        episode: Int? = null,
    )

    fun reset()
}
