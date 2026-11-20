package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Размеры элементов одинаковы на любом экране (сняты со скриншотов телефона), меняется только раскладка:
 * на широком экране (раскрытый складной телефон, планшет, десктоп) больше колонок, а одиночные блоки
 * не растягиваются шире [READABLE_WIDTH].
 */
enum class WindowSize {
    /** Телефон: две колонки товаров. */
    Compact,

    /** Раскрытый складной телефон и планшет. */
    Medium,

    /** Десктоп и планшет в альбомной ориентации. */
    Wide;

    companion object {
        fun forWidth(width: Dp): WindowSize = when {
            width < 600.dp -> Compact
            width < 960.dp -> Medium
            else -> Wide
        }
    }
}

val LocalWindowSize = staticCompositionLocalOf { WindowSize.Compact }

/** Ширина окна приложения: по ней считаются колонки сеток. */
val LocalWindowWidth = staticCompositionLocalOf { 393.dp }

val windowSize: WindowSize
    @Composable @ReadOnlyComposable
    get() = LocalWindowSize.current

/** Отступ контента секций от края экрана, как в приложении: заголовки, баннер, купон, плитки. */
val ScreenPadding = 16.dp

/** Предельная ширина одиночных блоков (форм, купона, пустых состояний) на широком экране. */
val READABLE_WIDTH = 600.dp

/** Карточка товара в сетке: 189 dp на телефоне шириной 394 dp, края 4 dp, промежуток 8 dp. */
val PRODUCT_CARD_WIDTH = 189.dp
val GRID_EDGE = 4.dp
val GRID_GAP = 8.dp

/** Колонок в сетке товаров: на телефоне 2, на раскрытом складном 4, карточки остаются того же размера. */
fun productColumns(width: Dp): Int =
    ((width - GRID_EDGE * 2 + GRID_GAP) / (PRODUCT_CARD_WIDTH + GRID_GAP)).toInt().coerceIn(2, 8)

val productColumns: Int
    @Composable @ReadOnlyComposable
    get() = productColumns(LocalWindowWidth.current)

/** Блок по центру не шире [maxWidth]: на телефоне занимает всю ширину. */
@Composable
fun Centered(modifier: Modifier = Modifier, maxWidth: Dp = READABLE_WIDTH, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/** Карточка в горизонтальной подборке («Пора пополнить», «Вы покупали»): 126 dp, как в каруселях приложения. */
val CAROUSEL_CARD_WIDTH = 126.dp
