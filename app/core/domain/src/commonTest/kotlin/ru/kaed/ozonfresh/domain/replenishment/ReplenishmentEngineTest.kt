package ru.kaed.ozonfresh.domain.replenishment

import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.Purchase
import ru.kaed.ozonfresh.domain.model.PurchaseStatus
import ru.kaed.ozonfresh.domain.model.Stock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReplenishmentEngineTest {
    private val engine = ReplenishmentEngine()

    private val milk = Product(
        id = "milk",
        name = "Молоко 3,2%",
        categoryId = "dairy",
        unit = "930 мл",
        price = 99,
        emoji = "🥛",
    )
    private val products = listOf(milk)
    private val inStock = listOf(Stock("milk", 20))

    /** Контрольный пример: 1 октября 2 уп., 15 октября 2 уп., 29 октября 1 уп. */
    private val milkHistory = listOf(
        Purchase("o1", "milk", day("2026-10-01"), 2),
        Purchase("o2", "milk", day("2026-10-15"), 2),
        Purchase("o3", "milk", day("2026-10-29"), 1),
    )

    private fun day(iso: String) = Day.parse(iso)

    private fun feed(
        today: String,
        purchases: List<Purchase> = milkHistory,
        stock: List<Stock> = inStock,
        feedback: Map<String, ReplenishmentFeedback> = emptyMap(),
    ) = engine.feed(day(today), products, purchases, stock, feedback)

    private fun milkItem(
        today: String,
        purchases: List<Purchase> = milkHistory,
        stock: List<Stock> = inStock,
        feedback: Map<String, ReplenishmentFeedback> = emptyMap(),
    ) = engine.evaluate(day(today), products, purchases, stock, feedback).single()

    @Test
    fun milkExampleGivesDueOnNovember5AndWindowFrom3To10() {
        val forecast = engine.forecast("milk", milkHistory)!!
        // 14 дней на 2 упаковки → 7 дней на одну; в последней покупке 1 упаковка → интервал 7 дней.
        assertEquals(7.0, forecast.daysPerUnit)
        assertEquals(7, forecast.intervalDays)

        val item = milkItem("2026-11-05")
        assertEquals(day("2026-11-05"), item.dueDate)
        assertEquals(day("2026-11-03"), item.windowStart)
        assertEquals(day("2026-11-10"), item.windowEnd)

        assertEquals(listOf("milk"), feed("2026-11-03").map { it.product.id })
        assertEquals(listOf("milk"), feed("2026-11-10").map { it.product.id })
    }

    @Test
    fun fewerThanThreeDeliveriesGiveNoFeed() {
        val twoPurchases = milkHistory.take(2)
        assertNull(engine.forecast("milk", twoPurchases))
        assertTrue(feed("2026-11-05", purchases = twoPurchases).isEmpty())
    }

    @Test
    fun returnsAndCancellationsAreNotCounted() {
        val history = milkHistory.take(2) + listOf(
            Purchase("o3", "milk", day("2026-10-29"), 1, PurchaseStatus.RETURNED),
            Purchase("o4", "milk", day("2026-10-30"), 1, PurchaseStatus.CANCELLED),
        )
        assertNull(engine.forecast("milk", history))
        assertTrue(feed("2026-11-05", purchases = history).isEmpty())
    }

    @Test
    fun outOfStockProductIsNotShown() {
        assertTrue(feed("2026-11-05", stock = listOf(Stock("milk", 0))).isEmpty())
        assertTrue(feed("2026-11-05", stock = emptyList()).isEmpty())
        assertEquals(
            ReplenishmentStatus.OUT_OF_STOCK,
            milkItem("2026-11-05", stock = listOf(Stock("milk", 0))).status,
        )
    }

    @Test
    fun outsideWindowProductIsNotShown() {
        assertTrue(feed("2026-11-02").isEmpty())
        assertTrue(feed("2026-11-11").isEmpty())
        assertEquals(ReplenishmentStatus.TOO_EARLY, milkItem("2026-11-02").status)
        assertEquals(ReplenishmentStatus.TOO_LATE, milkItem("2026-11-11").status)
    }

    @Test
    fun stillHaveShiftsDueByHalfInterval() {
        val shown = milkItem("2026-11-05")
        val feedback = engine.stillHave(day("2026-11-05"), shown, current = null)
        val answers = mapOf("milk" to feedback)

        // Интервал 7 дней, половина — 3,5 → 4 дня: срок 5 → 9 ноября, окно 7–14 ноября.
        assertEquals(4, feedback.extraDays)
        val next = milkItem("2026-11-06", feedback = answers)
        assertEquals(day("2026-11-09"), next.dueDate)
        assertEquals(ReplenishmentStatus.HIDDEN_STILL_HAVE, next.status)

        assertTrue(feed("2026-11-06", feedback = answers).isEmpty())
        assertEquals(listOf("milk"), feed("2026-11-07", feedback = answers).map { it.product.id })
        assertEquals(listOf("milk"), feed("2026-11-14", feedback = answers).map { it.product.id })
        assertTrue(feed("2026-11-15", feedback = answers).isEmpty())
    }

    @Test
    fun laterHidesForTwoDaysWithoutChangingDue() {
        val shown = milkItem("2026-11-05")
        val answers = mapOf("milk" to engine.later(day("2026-11-05"), shown, current = null))

        assertTrue(feed("2026-11-05", feedback = answers).isEmpty())
        assertTrue(feed("2026-11-06", feedback = answers).isEmpty())
        assertEquals(ReplenishmentStatus.HIDDEN_LATER, milkItem("2026-11-06", feedback = answers).status)

        val back = milkItem("2026-11-07", feedback = answers)
        assertEquals(ReplenishmentStatus.SHOWN, back.status)
        assertEquals(day("2026-11-05"), back.dueDate)
    }

    @Test
    fun newPurchaseRecalculatesDueAndDropsOldAnswers() {
        val shown = milkItem("2026-11-05")
        val answers = mapOf("milk" to engine.stillHave(day("2026-11-05"), shown, current = null))

        // Клиент всё-таки заказал 2 упаковки 6 ноября: 8 дней / 1 уп. добавились к выборке (7, 7, 8).
        val history = milkHistory + Purchase("o5", "milk", day("2026-11-06"), 2)
        val forecast = engine.forecast("milk", history)!!
        assertEquals(7.0, forecast.daysPerUnit)
        assertEquals(14, forecast.intervalDays)

        val item = milkItem("2026-11-07", purchases = history, feedback = answers)
        // Срок по новой истории, сдвиг от «Ещё есть» к старой покупке больше не применяется.
        assertEquals(day("2026-11-20"), item.dueDate)
        assertEquals(ReplenishmentStatus.TOO_EARLY, item.status)
        assertEquals(listOf("milk"), feed("2026-11-18", purchases = history, feedback = answers).map { it.product.id })
    }

    @Test
    fun repeatedShowInSameWindowIsNotNewEpisode() {
        val shown = feed("2026-11-05")
        val first = ReplenishmentEpisodes.newWindows(shown, seenWindows = emptySet())
        assertEquals(setOf("milk@2026-11-05"), first)

        assertTrue(ReplenishmentEpisodes.newWindows(feed("2026-11-07"), seenWindows = first).isEmpty())
    }

    @Test
    fun dayArithmeticMatchesCalendar() {
        assertEquals("2026-11-05", (day("2026-10-29") + 7).toString())
        assertEquals("2027-01-01", (day("2026-12-31") + 1).toString())
        assertEquals(5, day("2026-11-20").dayOfWeek) // пятница
        assertEquals(366, day("2025-01-01").daysSince(day("2024-01-01")))
    }
}
