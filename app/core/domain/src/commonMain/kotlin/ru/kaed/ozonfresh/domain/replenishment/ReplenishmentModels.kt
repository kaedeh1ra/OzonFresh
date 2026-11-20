package ru.kaed.ozonfresh.domain.replenishment

import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.Product

/** Правила ленты «Пора пополнить». Числа из описания фичи собраны здесь, чтобы их было легко найти и поменять. */
object ReplenishmentRules {
    /** Товар становится кандидатом, если клиент получил его хотя бы столько раз. */
    const val MIN_DELIVERED_PURCHASES = 3

    /** Окно показа: за сколько дней до срока товар появляется в ленте… */
    const val DAYS_BEFORE_DUE = 2

    /** …и сколько дней после срока ещё держится. */
    const val DAYS_AFTER_DUE = 5

    /** «Ещё есть» сдвигает срок на эту долю рассчитанного интервала. */
    const val STILL_HAVE_SHIFT_RATIO = 0.5

    /** «Позже» скрывает товар на столько дней. */
    const val LATER_HIDE_DAYS = 2
}

/** Почему кандидат показан или не показан на заданную дату. */
enum class ReplenishmentStatus {
    SHOWN,

    /** Срок ещё далеко: окно не началось. */
    TOO_EARLY,

    /** Окно закончилось: клиент, видимо, купил в другом месте или передумал. */
    TOO_LATE,

    /** Нет в наличии по текущему адресу. */
    OUT_OF_STOCK,

    /** Скрыт кнопкой «Ещё есть». */
    HIDDEN_STILL_HAVE,

    /** Скрыт кнопкой «Позже». */
    HIDDEN_LATER,
}

/** Какой кнопкой клиент скрыл товар. */
enum class FeedbackKind { STILL_HAVE, LATER }

/**
 * Ответ клиента по товару. Привязан к последней покупке [basePurchase]: после новой покупки ответ
 * устаревает и движок его не применяет, срок считается заново по истории.
 *
 * @param extraDays на сколько дней «Ещё есть» удлинил срок (копится, если нажимать несколько раз).
 * @param hiddenUntil товар скрыт до этого дня, в сам день снова виден.
 */
data class ReplenishmentFeedback(
    val productId: String,
    val basePurchase: Day,
    val extraDays: Int = 0,
    val hiddenUntil: Day? = null,
    val hiddenBy: FeedbackKind? = null,
)

/**
 * Прогноз расхода товара по истории доставленных покупок.
 *
 * @param daysPerUnit медиана «дней на одну штуку» по соседним покупкам.
 * @param intervalDays рассчитанный интервал: дни на штуку × количество в последней покупке, округлено до дня.
 */
data class ConsumptionForecast(
    val productId: String,
    val deliveredPurchases: Int,
    val daysPerUnit: Double,
    val lastPurchase: Day,
    val lastQuantity: Int,
    val intervalDays: Int,
) {
    /** Срок без учёта ответов клиента. */
    val baseDueDate: Day get() = lastPurchase + intervalDays
}

/** Товар-кандидат ленты на конкретную дату: прогноз, срок, окно и решение, показывать ли. */
data class ReplenishmentItem(
    val product: Product,
    val forecast: ConsumptionForecast,
    val dueDate: Day,
    val windowStart: Day,
    val windowEnd: Day,
    val status: ReplenishmentStatus,
) {
    val isShown: Boolean get() = status == ReplenishmentStatus.SHOWN

    /** Положительное — до срока ещё есть дни, отрицательное — срок прошёл. */
    fun daysUntilDue(today: Day): Int = dueDate.daysSince(today)

    /**
     * Окно показа конкретного товара. Эпизоды считаются по этим ключам: тот же товар в том же окне —
     * повторный показ, а не новое предложение.
     */
    val windowKey: String get() = "${product.id}@$dueDate"
}

/** Товар, который клиент покупал, но меньше [ReplenishmentRules.MIN_DELIVERED_PURCHASES] раз. */
data class ShortHistory(val product: Product, val deliveredPurchases: Int)
