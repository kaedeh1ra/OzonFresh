package ru.kaed.ozonfresh.presentation.demo

import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.ExperimentGroup
import ru.kaed.ozonfresh.domain.model.PilotEvent
import ru.kaed.ozonfresh.domain.model.PilotEventType
import ru.kaed.ozonfresh.domain.usecase.replenishment.ReplenishmentDiagnostics

object DemoContract {
    data class State(
        val today: Day? = null,
        val group: ExperimentGroup = ExperimentGroup.TEST,
        val diagnostics: ReplenishmentDiagnostics? = null,
        val stock: Map<String, Int> = emptyMap(),
        val events: List<PilotEvent> = emptyList(),
    ) {
        val metrics: Metrics get() = Metrics.from(events)
    }

    /** Метрики пилота из лога: то, что будем сравнивать между группами. */
    data class Metrics(
        val shownEpisodes: Int,
        val controlEpisodes: Int,
        val adds: Int,
        val stillHave: Int,
        val later: Int,
        val ordersWithFeed: Int,
        val outOfStockAtCheckout: Int,
    ) {
        companion object {
            fun from(events: List<PilotEvent>): Metrics {
                fun count(type: PilotEventType) = events.count { it.type == type }
                return Metrics(
                    shownEpisodes = count(PilotEventType.FEED_SHOWN),
                    controlEpisodes = count(PilotEventType.FEED_HIDDEN_CONTROL),
                    adds = count(PilotEventType.ADD),
                    stillHave = count(PilotEventType.STILL_HAVE),
                    later = count(PilotEventType.LATER),
                    ordersWithFeed = count(PilotEventType.ORDER_WITH_FEED),
                    outOfStockAtCheckout = count(PilotEventType.OUT_OF_STOCK_AT_CHECKOUT),
                )
            }
        }
    }

    sealed interface Intent {
        data class ShiftDay(val days: Int) : Intent
        data class SetGroup(val group: ExperimentGroup) : Intent
        data class SetInStock(val productId: String, val inStock: Boolean) : Intent
        data object Reset : Intent
    }

    sealed interface Effect {
        data class ShowMessage(val text: String) : Effect
    }
}
