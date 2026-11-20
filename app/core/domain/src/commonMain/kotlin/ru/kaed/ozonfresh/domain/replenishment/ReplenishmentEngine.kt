package ru.kaed.ozonfresh.domain.replenishment

import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.Purchase
import ru.kaed.ozonfresh.domain.model.PurchaseStatus
import ru.kaed.ozonfresh.domain.model.Stock
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentRules.DAYS_AFTER_DUE
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentRules.DAYS_BEFORE_DUE
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentRules.LATER_HIDE_DAYS
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentRules.MIN_DELIVERED_PURCHASES
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentRules.STILL_HAVE_SHIFT_RATIO
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Правило ленты «Пора пополнить». Чистая функция от истории, остатков, ответов клиента и даты:
 * ни UI, ни часов, ни хранилища. Сама лента ничего не покупает: только подсказывает товары.
 *
 * 1. Кандидаты — товары, доставленные клиенту хотя бы 3 раза. Возвраты и отмены не считаются.
 * 2. Для каждой пары соседних покупок: дни между ними / штук в более ранней. Медиана — дни на штуку.
 * 3. Срок = последняя покупка + дни на штуку × штук в последней покупке.
 * 4. Окно показа: от 2 дней до срока до 5 дней после.
 * 5. Без наличия по текущему адресу товар не показывается.
 */
class ReplenishmentEngine {

    /** Прогноз по истории одного товара или null, если доставок меньше трёх. */
    fun forecast(productId: String, purchases: List<Purchase>): ConsumptionForecast? {
        val delivered = deliveries(productId, purchases)
        if (delivered.size < MIN_DELIVERED_PURCHASES) return null

        val daysPerUnitSamples = delivered.zipWithNext { earlier, later ->
            later.date.daysSince(earlier.date).toDouble() / earlier.quantity
        }
        val daysPerUnit = median(daysPerUnitSamples)
        val last = delivered.last()
        return ConsumptionForecast(
            productId = productId,
            deliveredPurchases = delivered.size,
            daysPerUnit = daysPerUnit,
            lastPurchase = last.date,
            lastQuantity = last.quantity,
            intervalDays = (daysPerUnit * last.quantity).roundToInt(),
        )
    }

    /**
     * Все кандидаты со статусом на дату [today]: и показанные, и отфильтрованные.
     * Нужен для демо-панели; в ленту идёт [feed].
     */
    fun evaluate(
        today: Day,
        products: List<Product>,
        purchases: List<Purchase>,
        stock: List<Stock>,
        feedback: Map<String, ReplenishmentFeedback> = emptyMap(),
    ): List<ReplenishmentItem> {
        val stockByProduct = stock.associate { it.productId to it.quantity }
        return products.mapNotNull { product ->
            val forecast = forecast(product.id, purchases) ?: return@mapNotNull null
            // Ответ, данный до последней покупки, устарел: срок считается заново по новой истории.
            val actual = feedback[product.id]?.takeIf { it.basePurchase == forecast.lastPurchase }
            val due = forecast.baseDueDate + (actual?.extraDays ?: 0)
            val windowStart = due - DAYS_BEFORE_DUE
            val windowEnd = due + DAYS_AFTER_DUE
            val hiddenUntil = actual?.hiddenUntil
            // Скрытие кнопкой проверяется первым: в демо-панели видно, почему товара нет.
            val status = when {
                hiddenUntil != null && today < hiddenUntil -> when (actual.hiddenBy) {
                    FeedbackKind.LATER -> ReplenishmentStatus.HIDDEN_LATER
                    else -> ReplenishmentStatus.HIDDEN_STILL_HAVE
                }

                today < windowStart -> ReplenishmentStatus.TOO_EARLY
                today > windowEnd -> ReplenishmentStatus.TOO_LATE
                (stockByProduct[product.id] ?: 0) <= 0 -> ReplenishmentStatus.OUT_OF_STOCK
                else -> ReplenishmentStatus.SHOWN
            }
            ReplenishmentItem(product, forecast, due, windowStart, windowEnd, status)
        }.sortedWith(compareBy({ it.dueDate }, { it.product.name }))
    }

    /** Лента на дату [today]: только показанные товары, ближайший срок первым. */
    fun feed(
        today: Day,
        products: List<Product>,
        purchases: List<Purchase>,
        stock: List<Stock>,
        feedback: Map<String, ReplenishmentFeedback> = emptyMap(),
    ): List<ReplenishmentItem> = evaluate(today, products, purchases, stock, feedback).filter { it.isShown }

    /**
     * «Ещё есть»: срок удлиняется на половину рассчитанного интервала, товар скрывается до начала
     * нового окна (но минимум до завтра, чтобы не вернуться в ту же минуту).
     */
    fun stillHave(
        today: Day,
        item: ReplenishmentItem,
        current: ReplenishmentFeedback?,
    ): ReplenishmentFeedback {
        val shift = max(1, (item.forecast.intervalDays * STILL_HAVE_SHIFT_RATIO).roundToInt())
        val extraDays = actualFor(item, current)?.extraDays.orZero() + shift
        val newDue = item.forecast.baseDueDate + extraDays
        val hiddenUntil = maxOf(newDue - DAYS_BEFORE_DUE, today + 1)
        return ReplenishmentFeedback(
            productId = item.product.id,
            basePurchase = item.forecast.lastPurchase,
            extraDays = extraDays,
            hiddenUntil = hiddenUntil,
            hiddenBy = FeedbackKind.STILL_HAVE,
        )
    }

    /** «Позже»: товар скрыт на 2 дня, срок не меняется. */
    fun later(
        today: Day,
        item: ReplenishmentItem,
        current: ReplenishmentFeedback?,
    ): ReplenishmentFeedback = ReplenishmentFeedback(
        productId = item.product.id,
        basePurchase = item.forecast.lastPurchase,
        extraDays = actualFor(item, current)?.extraDays.orZero(),
        hiddenUntil = today + LATER_HIDE_DAYS,
        hiddenBy = FeedbackKind.LATER,
    )

    /** Товары, которые клиент получал, но слишком редко для прогноза. Для демо-панели. */
    fun shortHistory(products: List<Product>, purchases: List<Purchase>): List<ShortHistory> =
        products.mapNotNull { product ->
            val count = deliveries(product.id, purchases).size
            if (count in 1 until MIN_DELIVERED_PURCHASES) ShortHistory(product, count) else null
        }

    /**
     * Доставленные покупки товара по дате. Две покупки в один день считаются одной: иначе интервал
     * между ними был бы нулевым и занижал медиану.
     */
    private fun deliveries(productId: String, purchases: List<Purchase>): List<Purchase> =
        purchases
            .filter { it.productId == productId && it.status == PurchaseStatus.DELIVERED && it.quantity > 0 }
            .groupBy { it.date }
            .map { (_, sameDay) -> sameDay.first().copy(quantity = sameDay.sumOf { it.quantity }) }
            .sortedBy { it.date }

    private fun actualFor(item: ReplenishmentItem, feedback: ReplenishmentFeedback?) =
        feedback?.takeIf { it.basePurchase == item.forecast.lastPurchase }

    private fun Int?.orZero() = this ?: 0

    private fun median(values: List<Double>): Double {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2
    }
}

/**
 * Эпизод — одно показанное клиенту предложение набора товаров. Новый эпизод появляется, только
 * когда в ленте есть товар в окне, которого клиент ещё не видел. Повторные показы в том же окне
 * (вернулся на главную, скрыл товар, добавил в корзину) нового эпизода не создают.
 */
object ReplenishmentEpisodes {
    /** Ключи окон, которые делают показ новым эпизодом, или пустое множество, если это повтор. */
    fun newWindows(shown: List<ReplenishmentItem>, seenWindows: Set<String>): Set<String> =
        shown.map { it.windowKey }.filterTo(mutableSetOf()) { it !in seenWindows }
}
