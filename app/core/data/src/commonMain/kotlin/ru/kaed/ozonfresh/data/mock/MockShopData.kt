package ru.kaed.ozonfresh.data.mock

import ru.kaed.ozonfresh.domain.model.Category
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.Purchase
import ru.kaed.ozonfresh.domain.model.PurchaseStatus
import ru.kaed.ozonfresh.domain.model.Stock

/**
 * Моковые данные прототипа: каталог, остатки по адресу демо-клиента и его история за 4 месяца.
 *
 * На [DEMO_TODAY] (20 ноября 2026) лента показывает 5 товаров:
 * - молоко (срок 20.11), яйца (22.11), кофе (17.11), бананы (19.11), туалетная бумага (21.11);
 * - корм для кошек в окне (срок 18.11), но его нет в наличии — фильтр по остаткам;
 * - средство для посуды доставлено только дважды (третий заказ отменён) — в ленту не попадает;
 * - у кофе есть возврат 10.11: он не считается, иначе срок уехал бы на декабрь;
 * - хлеб (окно с 21.11), вода и йогурт (с 22.11) появляются, если листать дни вперёд.
 */
object MockShopData {
    val DEMO_TODAY: Day = Day.of(2026, 11, 20)
    const val ADDRESS = "ул. Садовая, 18, кв. 7"

    val categories = listOf(
        Category("dairy", "Молочное", "🥛"),
        Category("bakery", "Хлеб", "🍞"),
        Category("fruits", "Овощи и фрукты", "🍎"),
        Category("grocery", "Бакалея", "🍝"),
        Category("drinks", "Вода и напитки", "💧"),
        Category("coffee", "Кофе и чай", "☕"),
        Category("pets", "Для животных", "🐈"),
        Category("household", "Дом", "🧽"),
    )

    val products = listOf(
        product("milk", "Молоко 3,2%", "dairy", "930 мл", 99, "🥛", "Пастеризованное, из фермерского хозяйства."),
        product("eggs", "Яйца куриные С1", "dairy", "10 шт", 129, "🥚", "Отборные, категория С1."),
        product("cheese", "Сыр полутвёрдый", "dairy", "200 г", 239, "🧀"),
        product("yogurt", "Йогурт греческий 2%", "dairy", "140 г", 69, "🥣"),
        product("kefir", "Кефир 2,5%", "dairy", "900 мл", 89, "🥛"),
        product("butter", "Масло сливочное 82,5%", "dairy", "180 г", 199, "🧈"),
        product("cottage", "Творог 5%", "dairy", "300 г", 149, "🫙"),
        product("bread", "Хлеб бородинский", "bakery", "300 г", 59, "🍞", "Ржаной, с кориандром."),
        product("baguette", "Багет французский", "bakery", "250 г", 79, "🥖"),
        product("croissant", "Круассаны сливочные", "bakery", "4 шт", 189, "🥐"),
        product("bananas", "Бананы", "fruits", "1 кг", 139, "🍌", "Спелые, Эквадор."),
        product("apples", "Яблоки Гала", "fruits", "1 кг", 169, "🍎"),
        product("tomatoes", "Томаты черри", "fruits", "250 г", 159, "🍅"),
        product("cucumbers", "Огурцы среднеплодные", "fruits", "450 г", 119, "🥒"),
        product("avocado", "Авокадо Хасс", "fruits", "2 шт", 249, "🥑"),
        product("pasta", "Спагетти", "grocery", "450 г", 109, "🍝"),
        product("buckwheat", "Гречка ядрица", "grocery", "800 г", 119, "🌾"),
        product("oats", "Овсяные хлопья", "grocery", "500 г", 89, "🥣"),
        product("water", "Вода питьевая", "drinks", "5 л", 129, "💧", "Негазированная, бутыль 5 л."),
        product("juice", "Сок апельсиновый", "drinks", "1 л", 189, "🧃"),
        product("coffee", "Кофе в зёрнах", "coffee", "1 кг", 1290, "☕", "Арабика 100%, средняя обжарка."),
        product("tea", "Чай чёрный листовой", "coffee", "100 г", 249, "🍵"),
        product("catfood", "Влажный корм для кошек", "pets", "12 × 75 г", 459, "🐈", "Кусочки в соусе, с курицей."),
        product("litter", "Наполнитель древесный", "pets", "5 л", 299, "🪵"),
        product("tissue", "Туалетная бумага", "household", "8 рулонов", 349, "🧻", "Трёхслойная."),
        product("dishsoap", "Средство для посуды", "household", "450 мл", 159, "🧴"),
        product("sponges", "Губки для посуды", "household", "5 шт", 99, "🧽"),
        product("capsules", "Капсулы для стирки", "household", "30 шт", 749, "🫧"),
    )

    val weeklyHitIds = listOf("croissant", "avocado", "tomatoes", "juice", "cheese", "apples", "baguette", "tea")

    /** Остатки на складе адреса. Корм для кошек закончился. */
    val stock: List<Stock> = products.map { product ->
        Stock(product.id, if (product.id == "catfood") 0 else 12 + product.id.length * 3)
    }

    val purchases: List<Purchase> = buildHistory {
        every("milk", from = "2026-08-14", stepDays = 14, count = 7, quantity = 2)
        at("eggs", "2026-08-24", "2026-09-03", "2026-09-14", "2026-09-23", "2026-10-03",
            "2026-10-13", "2026-10-24", "2026-11-02", "2026-11-12")
        at("coffee", "2026-08-19", "2026-09-18", "2026-10-18")
        at("coffee", "2026-11-10", status = PurchaseStatus.RETURNED)
        every("bananas", from = "2026-09-03", stepDays = 7, count = 11)
        at("tissue", "2026-08-10", "2026-09-01", "2026-09-22", "2026-10-13", "2026-10-31")
        every("water", from = "2026-09-13", stepDays = 8, count = 9, quantity = 2)
        at("catfood", "2026-08-26", "2026-09-16", "2026-10-07", "2026-10-28")
        at("dishsoap", "2026-09-20", "2026-10-18")
        at("dishsoap", "2026-11-01", status = PurchaseStatus.CANCELLED)
        every("bread", from = "2026-09-28", stepDays = 4, count = 14)
        at("cheese", "2026-08-30", "2026-09-20", "2026-10-15", "2026-11-08")
        every("yogurt", from = "2026-09-21", stepDays = 8, count = 8, quantity = 4)
        at("kefir", "2026-10-05", "2026-11-02")
        at("butter", "2026-09-10", "2026-10-08", "2026-11-05")
        at("apples", "2026-09-12", "2026-10-01", "2026-10-20", "2026-11-09")
        at("litter", "2026-08-26", "2026-09-30", "2026-11-04")
        at("juice", "2026-10-03", "2026-10-17", "2026-10-31", "2026-11-14")
        at("tomatoes", "2026-10-30")
        at("pasta", "2026-09-05", "2026-10-10")
        at("capsules", "2026-08-15", "2026-10-01")
        at("sponges", "2026-09-20")
    }

    /** Новинки витрины: подборка «Новинки» на главной. */
    private val NEW_IDS get() = setOf("avocado", "cottage", "kefir", "capsules", "oats", "butter")

    /** Зачёркнутые цены на витрине: у части хитов, как скидки в приложении. Функция, а не поле: её зовут при инициализации [products]. */
    private fun oldPriceOf(id: String): Int? = when (id) {
        "croissant" -> 219
        "cheese" -> 279
        "tea" -> 299
        "apples" -> 189
        "yogurt" -> 79
        else -> null
    }

    private fun product(
        id: String,
        name: String,
        category: String,
        unit: String,
        price: Int,
        emoji: String,
        description: String = "",
    ): Product {
        // Оценка и отзывы стабильны для товара: выводятся из id, а не случайны.
        val seed = id.fold(7) { acc, c -> acc * 31 + c.code }.let { if (it < 0) -it else it }
        return Product(
            id, name, category, unit, price, emoji, description,
            rating = 4.5 + (seed % 5) / 10.0,
            reviews = 300 + seed % 25_000,
            oldPrice = oldPriceOf(id),
            isNew = id in NEW_IDS,
        )
    }
}

/** Строки истории по товарам; номера заказов раздаются по датам: всё купленное в один день — один заказ. */
private class HistoryBuilder {
    private val lines = mutableListOf<Triple<String, Day, Pair<Int, PurchaseStatus>>>()

    fun at(productId: String, vararg dates: String, quantity: Int = 1, status: PurchaseStatus = PurchaseStatus.DELIVERED) {
        dates.forEach { lines += Triple(productId, Day.parse(it), quantity to status) }
    }

    fun every(productId: String, from: String, stepDays: Int, count: Int, quantity: Int = 1) {
        val start = Day.parse(from)
        repeat(count) { i -> lines += Triple(productId, start + i * stepDays, quantity to PurchaseStatus.DELIVERED) }
    }

    fun build(): List<Purchase> {
        val orderIds = lines.map { it.second }.distinct().sorted()
            .withIndex().associate { (index, day) -> day to "FR-${1001 + index}" }
        return lines.map { (productId, day, rest) ->
            Purchase(orderIds.getValue(day), productId, day, rest.first, rest.second)
        }.sortedBy { it.date }
    }
}

private fun buildHistory(block: HistoryBuilder.() -> Unit): List<Purchase> =
    HistoryBuilder().apply(block).build()
