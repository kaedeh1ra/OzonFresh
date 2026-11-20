package ru.kaed.ozonfresh.presentation.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Цвета, которые меняются вместе с темой. Значения сняты со скриншотов приложения в светлой и тёмной теме. */
class FreshPalette(
    /** Шапка с адресом и полоса статус-бара. */
    val dark: Color,
    val darkButton: Color,
    /** Фон между секциями. */
    val canvas: Color,
    /** Белые (в тёмной теме — графитовые) секции, шторки, нижняя панель. */
    val surface: Color,
    /** Поиск, плитки разделов, купон, блоки внутри секций. */
    val card: Color,
    val line: Color,
    val ink: Color,
    val muted: Color,
    val navInactive: Color,
    val tealText: Color,
    val tealSoft: Color,
    val pinkSoft: Color,
    val pinkDark: Color,
)

val LightPalette = FreshPalette(
    dark = Color(0xFF070707),
    darkButton = Color(0xFF1D2025),
    canvas = Color(0xFFEEF2F5),
    surface = Color.White,
    card = Color(0xFFF6F7FB),
    line = Color(0xFFE6E9EE),
    ink = Color(0xFF0F0F0F),
    muted = Color(0xFF7C838D),
    navInactive = Color(0xFF9AA0A8),
    tealText = Color(0xFF00898A),
    tealSoft = Color(0xFFE0F7F6),
    pinkSoft = Color(0xFFFFE8F1),
    pinkDark = Color(0xFFD81B66),
)

val DarkPalette = FreshPalette(
    dark = Color(0xFF070707),
    darkButton = Color(0xFF2F3135),
    canvas = Color(0xFF070707),
    surface = Color(0xFF1C2025),
    card = Color(0xFF2F3135),
    line = Color(0xFF333940),
    ink = Color(0xFFF2F3F5),
    muted = Color(0xFF8E9095),
    navInactive = Color(0xFF7E8187),
    tealText = Color(0xFF2CC5C5),
    tealSoft = Color(0xFF123A3B),
    pinkSoft = Color(0xFF3B1B2A),
    pinkDark = Color(0xFFFF6FA8),
)

/**
 * Палитра в духе приложения Ozon fresh: тёмная шапка, секции на фоне, бирюзовые кнопки, розовые бейджи.
 * Цвета темы читаются из [palette] — снимка состояния, поэтому работают и в обычных функциях (модификаторы,
 * рисование); [AppTheme] переключает его по теме системы. Ассеты бренда (логотип, фото) не используются.
 */
object FreshColors {
    var palette: FreshPalette by mutableStateOf(LightPalette)

    val Dark get() = palette.dark
    val DarkButton get() = palette.darkButton
    val Canvas get() = palette.canvas
    val Surface get() = palette.surface
    val Card get() = palette.card
    val Line get() = palette.line
    val Ink get() = palette.ink
    val Muted get() = palette.muted
    val NavInactive get() = palette.navInactive
    val TealText get() = palette.tealText
    val TealSoft get() = palette.tealSoft
    val PinkSoft get() = palette.pinkSoft
    val PinkDark get() = palette.pinkDark

    // Одинаковые в обеих темах.
    val Teal = Color(0xFF00C2C2)
    val Pink = Color(0xFFFF4A91)
    val Red = Color(0xFFF2453D)
    /** Полоска прогресса до бесплатной доставки. */
    val Progress = Color(0xFF28A388)
    /** Зелёная плашка карты рядом с ценой. */
    val Bank = Color(0xFF3DB566)
    /** Белый, который не меняется с темой: кнопка корзины, степпер на фото, текст на цветном. */
    val White = Color.White
    /** Чёрный на белом в обеих темах: иконки на кнопке корзины и степпере. */
    val OnWhite = Color(0xFF0F0F0F)
    /** Фон фото товаров: снимки светлые и в тёмной теме. */
    val PhotoBg = Color(0xFFF6F7FB)
}

private fun colorScheme(p: FreshPalette, dark: Boolean) = (if (dark) darkColorScheme() else lightColorScheme()).copy(
    primary = FreshColors.Teal,
    onPrimary = Color.White,
    primaryContainer = p.tealSoft,
    onPrimaryContainer = p.tealText,
    secondary = FreshColors.Pink,
    onSecondary = Color.White,
    secondaryContainer = p.pinkSoft,
    onSecondaryContainer = p.pinkDark,
    tertiary = p.tealText,
    background = p.canvas,
    onBackground = p.ink,
    surface = p.surface,
    onSurface = p.ink,
    surfaceVariant = p.card,
    onSurfaceVariant = p.muted,
    surfaceContainerLowest = p.surface,
    surfaceContainerLow = p.surface,
    surfaceContainer = p.surface,
    surfaceContainerHigh = p.surface,
    surfaceContainerHighest = p.card,
    outline = if (dark) Color(0xFF4A4F57) else Color(0xFFC3C8CF),
    outlineVariant = p.line,
    error = FreshColors.Red,
    inverseSurface = if (dark) Color(0xFFE6E8EB) else Color(0xFF2F3135),
    inverseOnSurface = if (dark) Color(0xFF0F0F0F) else Color.White,
)

private val FreshShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val FreshTypography = Typography().run {
    copy(
        // Размеры подогнаны по скриншотам приложения: оно плотнее стандартной шкалы Material.
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 30.sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 26.sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, lineHeight = 22.sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
        labelMedium = labelMedium.copy(fontSize = 11.sp),
        bodyLarge = bodyLarge.copy(fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
        bodyMedium = bodyMedium.copy(fontSize = 12.sp, lineHeight = 15.sp, letterSpacing = 0.sp),
        bodySmall = bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
    )
}

/** Тема магазина: светлая или тёмная по настройке системы, как у приложения. */
@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    // Палитра — состояние: экраны, уже прочитавшие цвета, перерисуются при смене темы.
    if (FreshColors.palette !== palette) FreshColors.palette = palette
    MaterialTheme(
        colorScheme = remember(darkTheme) { colorScheme(palette, darkTheme) },
        shapes = FreshShapes,
        typography = FreshTypography,
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.ink, content = content)
    }
}
