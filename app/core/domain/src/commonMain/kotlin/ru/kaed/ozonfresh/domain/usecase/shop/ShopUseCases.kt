package ru.kaed.ozonfresh.domain.usecase.shop

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import ru.kaed.ozonfresh.domain.model.CartSource
import ru.kaed.ozonfresh.domain.model.Category
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.PilotEventType
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.Purchase
import ru.kaed.ozonfresh.domain.model.PurchaseStatus
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.repository.CartRepository
import ru.kaed.ozonfresh.domain.repository.CatalogRepository
import ru.kaed.ozonfresh.domain.repository.DemoSettingsRepository
import ru.kaed.ozonfresh.domain.repository.PilotEventRepository
import ru.kaed.ozonfresh.domain.repository.PurchaseRepository
import ru.kaed.ozonfresh.domain.repository.StockRepository

/** Бесплатная доставка от этой суммы, иначе [DELIVERY_FEE]. */
const val FREE_DELIVERY_FROM = 1_000
const val DELIVERY_FEE = 99

data class CartItem(val product: Product, val quantity: Int, val source: CartSource) {
    val total: Int get() = product.price * quantity
}

data class CartSummary(val items: List<CartItem>) {
    val count: Int get() = items.sumOf { it.quantity }
    val itemsTotal: Int get() = items.sumOf { it.total }
    val deliveryFee: Int get() = if (items.isEmpty() || itemsTotal >= FREE_DELIVERY_FROM) 0 else DELIVERY_FEE
    val total: Int get() = itemsTotal + deliveryFee
    fun quantityOf(productId: String): Int = items.firstOrNull { it.product.id == productId }?.quantity ?: 0
}

class GetCatalogUseCase(private val catalog: CatalogRepository) {
    val categories: List<Category> get() = catalog.categories
    val products: List<Product> get() = catalog.products
    val weeklyHits: List<Product> get() = catalog.weeklyHits
    fun product(id: String): Product? = catalog.product(id)
}

/** «Вы покупали»: что клиент получал чаще всего. */
class ObservePurchasedProductsUseCase(
    private val catalog: CatalogRepository,
    private val purchases: PurchaseRepository,
) {
    operator fun invoke(limit: Int = 10): Flow<List<Product>> = purchases.purchases.map { history ->
        history.filter { it.status == PurchaseStatus.DELIVERED }
            .groupingBy { it.productId }.eachCount()
            .entries.sortedByDescending { it.value }
            .mapNotNull { catalog.product(it.key) }
            .take(limit)
    }
}

class ObserveCartUseCase(
    private val cart: CartRepository,
    private val catalog: CatalogRepository,
) {
    operator fun invoke(): Flow<CartSummary> = cart.lines.map { lines ->
        CartSummary(lines.mapNotNull { line ->
            catalog.product(line.productId)?.let { CartItem(it, line.quantity, line.source) }
        })
    }

    fun count(): Flow<Int> = cart.lines.map { lines -> lines.sumOf { it.quantity } }
}

class AddToCartUseCase(private val cart: CartRepository) {
    operator fun invoke(productId: String) = cart.add(productId, CartSource.CATALOG)
}

class ChangeCartQuantityUseCase(private val cart: CartRepository) {
    operator fun invoke(productId: String, quantity: Int) = cart.setQuantity(productId, quantity)
}

class ObserveTodayUseCase(private val settings: DemoSettingsRepository) {
    operator fun invoke(): StateFlow<Day> = settings.today
    val address: String get() = settings.address
}

sealed interface OrderResult {
    data class Placed(val orderId: String, val total: Int, val count: Int, val day: Day) : OrderResult

    /** Перед оплатой часть товаров закончилась: их убрали из корзины, клиент подтверждает заново. */
    data class ItemsUnavailable(val products: List<Product>) : OrderResult

    data object EmptyCart : OrderResult
}

/**
 * Оплата заказа. Наличие проверяется ещё раз: закончившийся товар убирается из заказа, клиент
 * видит сообщение. Доставка в прототипе мгновенная: заказ сразу попадает в историю как доставленный,
 * поэтому срок для купленных товаров тут же пересчитывается по новой истории.
 */
class PlaceOrderUseCase(
    private val cart: CartRepository,
    private val catalog: CatalogRepository,
    private val stock: StockRepository,
    private val purchases: PurchaseRepository,
    private val events: PilotEventRepository,
    private val settings: DemoSettingsRepository,
) {
    operator fun invoke(): OrderResult {
        val lines = cart.lines.value
        if (lines.isEmpty()) return OrderResult.EmptyCart
        val today = settings.today.value
        val group = settings.group.value
        val available = stock.stock.value.associate { it.productId to it.quantity }

        val unavailable = lines.filter { (available[it.productId] ?: 0) <= 0 }
        if (unavailable.isNotEmpty()) {
            val products = unavailable.mapNotNull { catalog.product(it.productId) }
            unavailable.forEach { cart.setQuantity(it.productId, 0) }
            products.forEach {
                events.log(PilotEventType.OUT_OF_STOCK_AT_CHECKOUT, today, group, it.name)
            }
            return OrderResult.ItemsUnavailable(products)
        }

        val orderId = purchases.nextOrderId()
        val items = lines.mapNotNull { line -> catalog.product(line.productId)?.let { it to line } }
        val total = CartSummary(items.map { (product, line) -> CartItem(product, line.quantity, line.source) }).total
        purchases.add(lines.map { Purchase(orderId, it.productId, today, it.quantity, PurchaseStatus.DELIVERED) })
        lines.forEach { stock.set(it.productId, (available[it.productId] ?: 0) - it.quantity) }
        cart.clear()

        val fromFeed = items.filter { (_, line) -> line.source == CartSource.REPLENISHMENT }
        val count = lines.sumOf { it.quantity }
        if (fromFeed.isNotEmpty()) {
            events.log(
                PilotEventType.ORDER_WITH_FEED,
                today,
                group,
                "$orderId на ${total.rub()}: из ленты ${fromFeed.joinToString { it.first.name }}",
            )
        } else {
            events.log(PilotEventType.ORDER, today, group, "$orderId на ${total.rub()}, $count шт.")
        }
        return OrderResult.Placed(orderId, total, count, today)
    }
}
