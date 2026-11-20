package ru.kaed.ozonfresh.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.kaed.ozonfresh.data.mock.MockShopData
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
import ru.kaed.ozonfresh.domain.repository.CartRepository
import ru.kaed.ozonfresh.domain.repository.CatalogRepository
import ru.kaed.ozonfresh.domain.repository.DemoSettingsRepository
import ru.kaed.ozonfresh.domain.repository.PilotEventRepository
import ru.kaed.ozonfresh.domain.repository.PurchaseRepository
import ru.kaed.ozonfresh.domain.repository.ReplenishmentFeedbackRepository
import ru.kaed.ozonfresh.domain.repository.StockRepository

// Бэкенда нет: всё состояние прототипа живёт в памяти процесса и сбрасывается к MockShopData.

class MockCatalogRepository : CatalogRepository {
    override val categories: List<Category> = MockShopData.categories
    override val products: List<Product> = MockShopData.products
    private val byId = products.associateBy { it.id }
    override val weeklyHits: List<Product> = MockShopData.weeklyHitIds.mapNotNull { byId[it] }
    override fun product(id: String): Product? = byId[id]
}

class InMemoryPurchaseRepository : PurchaseRepository {
    private val _purchases = MutableStateFlow(MockShopData.purchases)
    override val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()
    private var nextNumber = FIRST_NEW_ORDER

    override fun nextOrderId(): String = "FR-${nextNumber++}"

    override fun add(purchases: List<Purchase>) = _purchases.update { it + purchases }

    override fun reset() {
        _purchases.value = MockShopData.purchases
        nextNumber = FIRST_NEW_ORDER
    }

    private companion object {
        const val FIRST_NEW_ORDER = 2001
    }
}

class InMemoryStockRepository : StockRepository {
    private val _stock = MutableStateFlow(MockShopData.stock)
    override val stock: StateFlow<List<Stock>> = _stock.asStateFlow()

    override fun set(productId: String, quantity: Int) = _stock.update { list ->
        list.map { if (it.productId == productId) it.copy(quantity = quantity.coerceAtLeast(0)) else it }
    }

    override fun reset() {
        _stock.value = MockShopData.stock
    }
}

class InMemoryCartRepository : CartRepository {
    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    override val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    override fun add(productId: String, source: CartSource) = _lines.update { lines ->
        if (lines.any { it.productId == productId }) {
            lines.map { if (it.productId == productId) it.copy(quantity = it.quantity + 1) else it }
        } else {
            lines + CartLine(productId, 1, source)
        }
    }

    override fun setQuantity(productId: String, quantity: Int) = _lines.update { lines ->
        if (quantity <= 0) {
            lines.filterNot { it.productId == productId }
        } else {
            lines.map { if (it.productId == productId) it.copy(quantity = quantity) else it }
        }
    }

    override fun clear() {
        _lines.value = emptyList()
    }
}

class InMemoryDemoSettingsRepository : DemoSettingsRepository {
    private val _today = MutableStateFlow(MockShopData.DEMO_TODAY)
    override val today: StateFlow<Day> = _today.asStateFlow()
    private val _group = MutableStateFlow(ExperimentGroup.TEST)
    override val group: StateFlow<ExperimentGroup> = _group.asStateFlow()
    override val address: String = MockShopData.ADDRESS

    override fun setToday(day: Day) {
        _today.value = day
    }

    override fun setGroup(group: ExperimentGroup) {
        _group.value = group
    }

    override fun reset() {
        _today.value = MockShopData.DEMO_TODAY
        _group.value = ExperimentGroup.TEST
    }
}

class InMemoryReplenishmentFeedbackRepository : ReplenishmentFeedbackRepository {
    private val _feedback = MutableStateFlow<Map<String, ReplenishmentFeedback>>(emptyMap())
    override val feedback: StateFlow<Map<String, ReplenishmentFeedback>> = _feedback.asStateFlow()

    override fun put(feedback: ReplenishmentFeedback) = _feedback.update { it + (feedback.productId to feedback) }

    override fun reset() {
        _feedback.value = emptyMap()
    }
}

class InMemoryPilotEventRepository : PilotEventRepository {
    private val _events = MutableStateFlow<List<PilotEvent>>(emptyList())
    override val events: StateFlow<List<PilotEvent>> = _events.asStateFlow()
    private val windows = mutableSetOf<String>()
    private var episodes = 0

    override val seenWindows: Set<String> get() = windows.toSet()

    override fun startEpisode(windows: Set<String>): Int {
        this.windows += windows
        return ++episodes
    }

    override fun log(type: PilotEventType, day: Day, group: ExperimentGroup, details: String, episode: Int?) =
        _events.update { it + PilotEvent(it.size + 1, day, type, group, details, episode) }

    override fun reset() {
        _events.value = emptyList()
        windows.clear()
        episodes = 0
    }
}
