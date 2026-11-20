package ru.kaed.ozonfresh.presentation.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Палитра-заглушка: замените цвета бренда здесь, компоненты берут их из MaterialTheme.colorScheme.
private val Primary = Color(0xFF3F51B5)
private val PrimaryDark = Color(0xFFB4C0FF)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF0E1A5C),
)

@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
