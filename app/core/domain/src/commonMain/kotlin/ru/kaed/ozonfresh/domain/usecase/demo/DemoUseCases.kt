package ru.kaed.ozonfresh.domain.usecase.demo

import kotlinx.coroutines.flow.StateFlow
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.ExperimentGroup
import ru.kaed.ozonfresh.domain.model.PilotEvent
import ru.kaed.ozonfresh.domain.model.Stock
import ru.kaed.ozonfresh.domain.repository.CartRepository
import ru.kaed.ozonfresh.domain.repository.DemoSettingsRepository
import ru.kaed.ozonfresh.domain.repository.PilotEventRepository
import ru.kaed.ozonfresh.domain.repository.PurchaseRepository
import ru.kaed.ozonfresh.domain.repository.ReplenishmentFeedbackRepository
import ru.kaed.ozonfresh.domain.repository.StockRepository

/** Управление демо для защиты: дата, группа теста, остатки и сброс. */
class DemoControlsUseCase(
    private val settings: DemoSettingsRepository,
    private val stockRepository: StockRepository,
    private val eventLog: PilotEventRepository,
    private val purchases: PurchaseRepository,
    private val feedback: ReplenishmentFeedbackRepository,
    private val cart: CartRepository,
) {
    val today: StateFlow<Day> get() = settings.today
    val group: StateFlow<ExperimentGroup> get() = settings.group
    val stock: StateFlow<List<Stock>> get() = stockRepository.stock
    val events: StateFlow<List<PilotEvent>> get() = eventLog.events

    fun shiftDay(days: Int) = settings.setToday(settings.today.value + days)
    fun setGroup(group: ExperimentGroup) = settings.setGroup(group)
    fun setStock(productId: String, quantity: Int) = stockRepository.set(productId, quantity)

    /** Всё к исходным моковым данным: дата, история, остатки, ответы в ленте, корзина и лог. */
    fun reset() {
        settings.reset()
        purchases.reset()
        stockRepository.reset()
        feedback.reset()
        cart.clear()
        eventLog.reset()
    }
}
