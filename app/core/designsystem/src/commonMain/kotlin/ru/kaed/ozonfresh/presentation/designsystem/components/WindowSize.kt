package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize {
    /** Телефон: одна колонка, нижняя навигация. */
    Compact,

    /** Планшет и узкое окно: одна колонка, нижняя навигация. */
    Medium,

    /** Десктоп: широкий контент. */
    Wide;

    companion object {
        fun forWidth(width: Dp): WindowSize = when {
            width < 600.dp -> Compact
            width < 960.dp -> Medium
            else -> Wide
        }
    }
}

val LocalWindowSize = staticCompositionLocalOf { WindowSize.Wide }

/** Боковой отступ контента: на телефоне меньше, чтобы не терять ширину. */
val WindowSize.contentPadding: Dp
    get() = if (this == WindowSize.Compact) 10.dp else 24.dp

val windowSize: WindowSize
    @Composable @ReadOnlyComposable
    get() = LocalWindowSize.current
