package ru.kaed.ozonfresh.domain.model

import kotlin.jvm.JvmInline

/**
 * Календарный день без времени и часового пояса: число дней от 1970-01-01.
 * Своя маленькая дата вместо kotlinx-datetime: движку нужны только сложение дней и разница между датами.
 * Системные часы здесь не читаются: «сегодня» всегда передаётся параметром.
 */
@JvmInline
value class Day(val epochDay: Int) : Comparable<Day> {

    val year: Int get() = civil().first
    val month: Int get() = civil().second
    val dayOfMonth: Int get() = civil().third

    /** 1 — понедельник, 7 — воскресенье. */
    val dayOfWeek: Int get() = (epochDay + 3).mod(7) + 1

    operator fun plus(days: Int): Day = Day(epochDay + days)
    operator fun minus(days: Int): Day = Day(epochDay - days)

    /** Сколько дней от [other] до этого дня. */
    fun daysSince(other: Day): Int = epochDay - other.epochDay

    override fun compareTo(other: Day): Int = epochDay.compareTo(other.epochDay)

    override fun toString(): String =
        "$year-${month.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"

    // Алгоритм civil_from_days (H. Hinnant): григорианский календарь без таблиц.
    private fun civil(): Triple<Int, Int, Int> {
        val z = epochDay + 719_468
        val era = (if (z >= 0) z else z - 146_096) / 146_097
        val doe = z - era * 146_097
        val yoe = (doe - doe / 1_460 + doe / 36_524 - doe / 146_096) / 365
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = if (mp < 10) mp + 3 else mp - 9
        val y = yoe + era * 400 + if (m <= 2) 1 else 0
        return Triple(y, m, d)
    }

    companion object {
        fun of(year: Int, month: Int, day: Int): Day {
            require(month in 1..12 && day in 1..31) { "Некорректная дата $year-$month-$day" }
            val y = if (month <= 2) year - 1 else year
            val era = (if (y >= 0) y else y - 399) / 400
            val yoe = y - era * 400
            val doy = (153 * (month + if (month > 2) -3 else 9) + 2) / 5 + day - 1
            val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
            return Day(era * 146_097 + doe - 719_468)
        }

        /** «2026-11-05» → Day. */
        fun parse(iso: String): Day {
            val parts = iso.split("-").map { it.toInt() }
            require(parts.size == 3) { "Ожидается дата вида 2026-11-05: $iso" }
            return of(parts[0], parts[1], parts[2])
        }
    }
}

private val MONTHS_GENITIVE = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)
private val MONTHS_SHORT = listOf(
    "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек",
)
private val WEEKDAYS_SHORT = listOf("пн", "вт", "ср", "чт", "пт", "сб", "вс")

/** «5 ноября». */
fun Day.formatLong(): String = "$dayOfMonth ${MONTHS_GENITIVE[month - 1]}"

/** «5 ноя». */
fun Day.formatShort(): String = "$dayOfMonth ${MONTHS_SHORT[month - 1]}"

/** «пт». */
fun Day.weekdayShort(): String = WEEKDAYS_SHORT[dayOfWeek - 1]
