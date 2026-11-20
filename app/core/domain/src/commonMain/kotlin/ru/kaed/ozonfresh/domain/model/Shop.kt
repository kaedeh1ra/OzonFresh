package ru.kaed.ozonfresh.domain.model

/** Раздел каталога: чипсы на главной и фильтр каталога. */
data class Category(
    val id: String,
    val title: String,
    val emoji: String,
)

/**
 * Товар. [unit] — вес или объём для подписи («930 мл»), [price] — цена в рублях.
 * [emoji] — картинка-заглушка вместо фото товара.
 */
data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val unit: String,
    val price: Int,
    val emoji: String,
    val description: String = "",
    /** Оценка и число отзывов для карточки витрины; на ленту и расчёты не влияют. */
    val rating: Double? = null,
    val reviews: Int? = null,
    /** Зачёркнутая цена на витрине. Покупают по [price]: «Пора пополнить» и корзина скидок не знают. */
    val oldPrice: Int? = null,
    /** Метка «Новинка» на витрине. */
    val isNew: Boolean = false,
)

/** Скидка в процентах для бейджа «−21%»; null — цена без зачёркнутой. */
val Product.discountPercent: Int?
    get() = oldPrice?.takeIf { it > price }?.let { 100 - price * 100 / it }

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

/** События пилота: из них считаются метрики (показы, клики по кнопкам, оплаты с товарами из ленты). */
enum class PilotEventType(val title: String) {
    FEED_SHOWN("Показ ленты"),
    FEED_HIDDEN_CONTROL("Лента скрыта (контроль)"),
    ADD("Добавить"),
    STILL_HAVE("Ещё есть"),
    LATER("Позже"),
    ORDER_WITH_FEED("Оплата с товарами из ленты"),
    ORDER("Оплата заказа"),
    OUT_OF_STOCK_AT_CHECKOUT("Закончился перед оплатой"),
}

data class PilotEvent(
    val id: Int,
    val day: Day,
    val type: PilotEventType,
    val group: ExperimentGroup,
    val details: String,
    val episode: Int? = null,
)

/** «1 290 ₽»: разряды через неразрывный пробел. */
fun Int.rub(): String {
    val digits = toString()
    val grouped = digits.reversed().chunked(3).joinToString("\u00A0").reversed()
    return "$grouped\u00A0₽"
}
