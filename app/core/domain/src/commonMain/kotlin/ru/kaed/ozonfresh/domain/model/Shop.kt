package ru.kaed.ozonfresh.domain.model

/** Раздел каталога: чипсы на главной и фильтр каталога. */
data class Category(
    val id: String,
    val title: String,
    val emoji: String,
)

/**
 * Товар. [unit] — вес или объём для подписи («930 мл»), [price] — цена в рублях.
 * [emoji] и [tint] — картинка-заглушка: цветной блок с эмодзи, без чужих фото.
 */
data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val unit: String,
    val price: Int,
    val emoji: String,
    val tint: Long,
    val description: String = "",
)

enum class PurchaseStatus {
    /** Клиент получил товар: только такие покупки учитывает «Пора пополнить». */
    DELIVERED,
    RETURNED,
    CANCELLED,
}

/** Строка заказа из истории клиента: сколько штук товара и когда. */
data class Purchase(
    val orderId: String,
    val productId: String,
    val date: Day,
    val quantity: Int,
    val status: PurchaseStatus = PurchaseStatus.DELIVERED,
)

/** Остаток товара на складе, который обслуживает текущий адрес. */
data class Stock(
    val productId: String,
    val quantity: Int,
)

/** Откуда товар попал в корзину: для метрик пилота считаем заказы с товарами из ленты. */
enum class CartSource { CATALOG, REPLENISHMENT }

data class CartLine(
    val productId: String,
    val quantity: Int,
    val source: CartSource = CartSource.CATALOG,
)

/** Группа A/B-теста: «Тест» видит ленту, «Контроль» нет. */
enum class ExperimentGroup(val title: String) {
    TEST("Тест"),
    CONTROL("Контроль"),
}
