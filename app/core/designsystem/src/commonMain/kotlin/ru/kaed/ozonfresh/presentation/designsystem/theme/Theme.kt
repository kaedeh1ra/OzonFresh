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

/** Палитра прототипа: белый фон, розовый акцент, бирюзовый второй цвет, тёмно-зелёный текст, светло-серые карточки. */
object FreshColors {
    val Pink = Color(0xFFE5336F)
    val PinkSoft = Color(0xFFFDE8EF)
    val PinkDark = Color(0xFFB81E53)
    val Teal = Color(0xFF2A9690)
    val TealSoft = Color(0xFFE5F4F2)
    val TealDark = Color(0xFF1C6E69)
    val Ink = Color(0xFF0F3B3A)
    val Muted = Color(0xFF5E7675)
    val Card = Color(0xFFF2F5F5)
    val Line = Color(0xFFE3E9E9)
    val White = Color.White
}

private val LightColors = lightColorScheme(
    primary = FreshColors.Pink,
    onPrimary = FreshColors.White,
    primaryContainer = FreshColors.PinkSoft,
    onPrimaryContainer = FreshColors.PinkDark,
    secondary = FreshColors.Teal,
    onSecondary = FreshColors.White,
    secondaryContainer = FreshColors.TealSoft,
    onSecondaryContainer = FreshColors.TealDark,
    tertiary = FreshColors.Teal,
    background = FreshColors.White,
    onBackground = FreshColors.Ink,
    surface = FreshColors.White,
    onSurface = FreshColors.Ink,
    surfaceVariant = FreshColors.Card,
    onSurfaceVariant = FreshColors.Muted,
    surfaceContainerLowest = FreshColors.White,
    surfaceContainerLow = Color(0xFFF8FAFA),
    surfaceContainer = FreshColors.Card,
    surfaceContainerHigh = Color(0xFFEDF1F1),
    surfaceContainerHighest = Color(0xFFE7ECEC),
    outline = Color(0xFFB9C6C5),
    outlineVariant = FreshColors.Line,
)

private val FreshShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private val FreshTypography = Typography().run {
    copy(
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 21.sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

/** Светлая тема: интерфейс магазина сознательно один, как у референса. */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = FreshShapes,
        typography = FreshTypography,
        content = content,
    )
}
