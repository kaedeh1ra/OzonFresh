package ru.kaed.ozonfresh.presentation.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Палитра в духе приложения Ozon fresh (цвета сняты со скриншотов): тёмная шапка, серый фон между
 * белыми секциями, бирюзовые кнопки, розовые бейджи. Ассеты бренда (логотип, фото) не используются.
 */
object FreshColors {
    /** Шапка с адресом и полоса статус-бара. */
    val Dark = Color(0xFF070707)
    val DarkButton = Color(0xFF1D2025)

    /** Фон между белыми секциями. */
    val Canvas = Color(0xFFEEF2F5)

    /** Поиск, плитки категорий, фон картинок товаров. */
    val Card = Color(0xFFF6F7FB)
    val Line = Color(0xFFE6E9EE)

    val Teal = Color(0xFF00C2C2)
    /** Бирюзовый для текста на белом: основной слишком светлый для мелких надписей. */
    val TealText = Color(0xFF00898A)
    val TealSoft = Color(0xFFE0F7F6)

    val Pink = Color(0xFFFF4A91)
    val PinkSoft = Color(0xFFFFE8F1)
    val PinkDark = Color(0xFFD81B66)
    val Red = Color(0xFFF2453D)

    val Ink = Color(0xFF0F0F0F)
    val Muted = Color(0xFF7C838D)
    val NavInactive = Color(0xFF9AA0A8)
    val White = Color.White
}

private val LightColors = lightColorScheme(
    primary = FreshColors.Teal,
    onPrimary = FreshColors.White,
    primaryContainer = FreshColors.TealSoft,
    onPrimaryContainer = FreshColors.TealText,
    secondary = FreshColors.Pink,
    onSecondary = FreshColors.White,
    secondaryContainer = FreshColors.PinkSoft,
    onSecondaryContainer = FreshColors.PinkDark,
    tertiary = FreshColors.TealText,
    background = FreshColors.Canvas,
    onBackground = FreshColors.Ink,
    surface = FreshColors.White,
    onSurface = FreshColors.Ink,
    surfaceVariant = FreshColors.Card,
    onSurfaceVariant = FreshColors.Muted,
    surfaceContainerLowest = FreshColors.White,
    surfaceContainerLow = FreshColors.White,
    surfaceContainer = FreshColors.White,
    surfaceContainerHigh = FreshColors.White,
    surfaceContainerHighest = FreshColors.Card,
    outline = Color(0xFFC3C8CF),
    outlineVariant = FreshColors.Line,
    error = FreshColors.Red,
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

/** Светлая тема магазина: одна, как у референса. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = FreshShapes,
        typography = FreshTypography,
        content = content,
    )
}
