package ru.kaed.ozonfresh.data

import ru.kaed.ozonfresh.data.mock.MockShopData
import ru.kaed.ozonfresh.data.repository.InMemoryCartRepository
import ru.kaed.ozonfresh.data.repository.InMemoryDemoSettingsRepository
import ru.kaed.ozonfresh.data.repository.InMemoryPilotEventRepository
import ru.kaed.ozonfresh.data.repository.InMemoryPurchaseRepository
import ru.kaed.ozonfresh.data.repository.InMemoryStockRepository
import ru.kaed.ozonfresh.data.repository.MockCatalogRepository
import ru.kaed.ozonfresh.domain.model.CartSource
import ru.kaed.ozonfresh.domain.model.PilotEventType
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentEngine
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentStatus
import ru.kaed.ozonfresh.domain.usecase.shop.OrderResult
import ru.kaed.ozonfresh.domain.usecase.shop.PlaceOrderUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Демо-сценарий для защиты держится на моках: если их поправить, тест подскажет, что сломалось. */
class MockScenarioTest {
    private val engine = ReplenishmentEngine()

    private fun evaluate() = engine.evaluate(
        MockShopData.DEMO_TODAY,
        MockShopData.products,
        MockShopData.purchases,
        MockShopData.stock,
    )

    @Test
    fun demoDateShowsFiveProducts() {
        val shown = evaluate().filter { it.isShown }.map { it.product.id }.toSet()
        assertEquals(setOf("milk", "eggs", "coffee", "bananas", "tissue"), shown)
    }

    @Test
    fun catFoodIsInWindowButOutOfStock() {
        val catFood = evaluate().single { it.product.id == "catfood" }
        assertEquals(ReplenishmentStatus.OUT_OF_STOCK, catFood.status)
    }

    @Test
    fun dishSoapBoughtTwiceIsNotCandidate() {
        assertTrue(evaluate().none { it.product.id == "dishsoap" })
        val short = engine.shortHistory(MockShopData.products, MockShopData.purchases)
        assertEquals(2, short.single { it.product.id == "dishsoap" }.deliveredPurchases)
    }

    @Test
    fun checkoutRemovesItemThatRanOutAndThenPlacesOrder() {
        val cart = InMemoryCartRepository()
        val stock = InMemoryStockRepository()
        val purchases = InMemoryPurchaseRepository()
        val events = InMemoryPilotEventRepository()
        val placeOrder = PlaceOrderUseCase(
            cart, MockCatalogRepository(), stock, purchases, events, InMemoryDemoSettingsRepository(),
        )
        cart.add("milk", CartSource.REPLENISHMENT)
        cart.add("bread", CartSource.CATALOG)
        stock.set("milk", 0)

        val first = placeOrder()
        assertIs<OrderResult.ItemsUnavailable>(first)
        assertEquals(listOf("milk"), first.products.map { it.id })
        assertEquals(listOf("bread"), cart.lines.value.map { it.productId })

        assertIs<OrderResult.Placed>(placeOrder())
        assertTrue(cart.lines.value.isEmpty())
        assertEquals(MockShopData.purchases.size + 1, purchases.purchases.value.size)
        assertEquals(
            listOf(PilotEventType.OUT_OF_STOCK_AT_CHECKOUT, PilotEventType.ORDER),
            events.events.value.map { it.type },
        )
    }
}
