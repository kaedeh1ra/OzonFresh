package ru.kaed.ozonfresh.domain.usecase.replenishment

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.kaed.ozonfresh.domain.model.CartSource
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.ExperimentGroup
import ru.kaed.ozonfresh.domain.model.PilotEventType
import ru.kaed.ozonfresh.domain.model.formatLong
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentEngine
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentEpisodes
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentItem
import ru.kaed.ozonfresh.domain.replenishment.ShortHistory
import ru.kaed.ozonfresh.domain.repository.CartRepository
import ru.kaed.ozonfresh.domain.repository.CatalogRepository
import ru.kaed.ozonfresh.domain.repository.DemoSettingsRepository
import ru.kaed.ozonfresh.domain.repository.PilotEventRepository
import ru.kaed.ozonfresh.domain.repository.PurchaseRepository
import ru.kaed.ozonfresh.domain.repository.ReplenishmentFeedbackRepository
import ru.kaed.ozonfresh.domain.repository.StockRepository

/**
 * Лента на «сегодня». [eligible] — что движок отобрал для клиента; [visible] — что видит клиент:
 * в контрольной группе A/B-теста лента не показывается, но кандидаты считаются для сравнения групп.
 */
data class ReplenishmentFeed(
    val today: Day,
    val group: ExperimentGroup,
    val eligible: List<ReplenishmentItem>,
) {
    val visible: List<ReplenishmentItem>
        get() = if (group == ExperimentGroup.TEST) eligible else emptyList()
}

/** Все кандидаты с причинами показа или скрытия: для демо-панели. */
data class ReplenishmentDiagnostics(
    val today: Day,
    val candidates: List<ReplenishmentItem>,
    val shortHistory: List<ShortHistory>,
)

class ObserveReplenishmentFeedUseCase(
    private val engine: ReplenishmentEngine,
    private val catalog: CatalogRepository,
    private val purchases: PurchaseRepository,
    private val stock: StockRepository,
    private val feedback: ReplenishmentFeedbackRepository,
    private val settings: DemoSettingsRepository,
) {
    operator fun invoke(): Flow<ReplenishmentFeed> = combine(
        settings.today,
        settings.group,
        purchases.purchases,
        stock.stock,
        feedback.feedback,
    ) { today, group, history, stockNow, answers ->
        ReplenishmentFeed(today, group, engine.feed(today, catalog.products, history, stockNow, answers))
    }
}

class ObserveReplenishmentDiagnosticsUseCase(
    private val engine: ReplenishmentEngine,
    private val catalog: CatalogRepository,
    private val purchases: PurchaseRepository,
    private val stock: StockRepository,
    private val feedback: ReplenishmentFeedbackRepository,
    private val settings: DemoSettingsRepository,
) {
    operator fun invoke(): Flow<ReplenishmentDiagnostics> = combine(
        settings.today,
        purchases.purchases,
        stock.stock,
        feedback.feedback,
    ) { today, history, stockNow, answers ->
        ReplenishmentDiagnostics(
            today = today,
            candidates = engine.evaluate(today, catalog.products, history, stockNow, answers),
            shortHistory = engine.shortHistory(catalog.products, history),
        )
    }
}

/**
 * Экран показал блок ленты (или, в контроле, мог бы показать). Эпизод создаётся, только если
 * в ленте есть товар в окне, которого клиент ещё не видел; повторный показ того же набора — не эпизод.
 */
class RegisterFeedImpressionUseCase(private val events: PilotEventRepository) {
    operator fun invoke(feed: ReplenishmentFeed) {
        if (feed.eligible.isEmpty()) return
        val newWindows = ReplenishmentEpisodes.newWindows(feed.eligible, events.seenWindows)
        if (newWindows.isEmpty()) return
        val episode = events.startEpisode(newWindows)
        val type = if (feed.group == ExperimentGroup.TEST) {
            PilotEventType.FEED_SHOWN
        } else {
            PilotEventType.FEED_HIDDEN_CONTROL
        }
        val names = feed.eligible.joinToString { it.product.name }
        events.log(type, feed.today, feed.group, "${feed.eligible.size} шт.: $names", episode)
    }
}

/** «Добавить»: обычная корзина, без автопокупки и скидок. */
class AddFromFeedUseCase(
    private val cart: CartRepository,
    private val events: PilotEventRepository,
    private val settings: DemoSettingsRepository,
) {
    operator fun invoke(item: ReplenishmentItem) {
        cart.add(item.product.id, CartSource.REPLENISHMENT)
        events.log(PilotEventType.ADD, settings.today.value, settings.group.value, item.product.name)
    }
}

/** «Ещё есть»: скрыть товар и удлинить срок на половину интервала. */
class StillHaveUseCase(
    private val engine: ReplenishmentEngine,
    private val feedback: ReplenishmentFeedbackRepository,
    private val events: PilotEventRepository,
    private val settings: DemoSettingsRepository,
) {
    operator fun invoke(item: ReplenishmentItem) {
        val today = settings.today.value
        val answer = engine.stillHave(today, item, feedback.feedback.value[item.product.id])
        feedback.put(answer)
        val newDue = item.forecast.baseDueDate + answer.extraDays
        events.log(
            PilotEventType.STILL_HAVE,
            today,
            settings.group.value,
            "${item.product.name}: срок ${item.dueDate.formatLong()} → ${newDue.formatLong()}",
        )
    }
}

/** «Позже»: скрыть товар на 2 дня, срок прежний. */
class LaterUseCase(
    private val engine: ReplenishmentEngine,
    private val feedback: ReplenishmentFeedbackRepository,
    private val events: PilotEventRepository,
    private val settings: DemoSettingsRepository,
) {
    operator fun invoke(item: ReplenishmentItem) {
        val today = settings.today.value
        val answer = engine.later(today, item, feedback.feedback.value[item.product.id])
        feedback.put(answer)
        events.log(
            PilotEventType.LATER,
            today,
            settings.group.value,
            "${item.product.name}: скрыт до ${answer.hiddenUntil?.formatLong()}",
        )
    }
}
