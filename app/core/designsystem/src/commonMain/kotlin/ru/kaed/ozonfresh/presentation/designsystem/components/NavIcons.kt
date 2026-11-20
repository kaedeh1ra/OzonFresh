package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Иконки нарисованы на Canvas: отдельная библиотека иконок ради нескольких штук не нужна.
// Вкладки — залитые фигуры, как в нижней навигации референса; служебные (поиск, назад) — линиями.

enum class NavIcon { Home, Products, Catalog, Cart, Profile, List, Search, Back, ChevronDown, Close, Heart }

@Composable
fun NavIconView(icon: NavIcon, color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier.size(size)) {
        val w = size.toPx()
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            NavIcon.Home -> home(w, color, stroke)
            NavIcon.Products -> apple(w, color)
            NavIcon.Catalog -> tiles(w, color)
            NavIcon.Cart -> basket(w, color)
            NavIcon.Profile -> person(w, color)
            NavIcon.List -> list(w, color, stroke)
            NavIcon.Search -> {
                drawCircle(color, radius = w * 0.26f, center = Offset(w * 0.44f, w * 0.44f), style = Stroke(w * 0.12f))
                drawLine(color, Offset(w * 0.64f, w * 0.64f), Offset(w * 0.84f, w * 0.84f), w * 0.13f, StrokeCap.Round)
            }

            NavIcon.Back -> {
                val path = Path().apply {
                    moveTo(w * 0.62f, w * 0.2f)
                    lineTo(w * 0.32f, w * 0.5f)
                    lineTo(w * 0.62f, w * 0.8f)
                }
                drawPath(path, color, style = stroke)
            }

            NavIcon.Close -> {
                drawLine(color, Offset(w * 0.22f, w * 0.22f), Offset(w * 0.78f, w * 0.78f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.78f, w * 0.22f), Offset(w * 0.22f, w * 0.78f), stroke.width, StrokeCap.Round)
            }

            NavIcon.ChevronDown -> {
                val path = Path().apply {
                    moveTo(w * 0.22f, w * 0.38f)
                    lineTo(w * 0.5f, w * 0.66f)
                    lineTo(w * 0.78f, w * 0.38f)
                }
                drawPath(path, color, style = stroke)
            }

            NavIcon.Heart -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, w * 0.86f)
                    cubicTo(w * 0.1f, w * 0.6f, w * 0.02f, w * 0.36f, w * 0.18f, w * 0.2f)
                    cubicTo(w * 0.32f, w * 0.06f, w * 0.48f, w * 0.14f, w * 0.5f, w * 0.28f)
                    cubicTo(w * 0.52f, w * 0.14f, w * 0.68f, w * 0.06f, w * 0.82f, w * 0.2f)
                    cubicTo(w * 0.98f, w * 0.36f, w * 0.9f, w * 0.6f, w * 0.5f, w * 0.86f)
                    close()
                }
                drawPath(path, color)
            }
        }
    }
}

private fun DrawScope.home(w: Float, color: Color, stroke: Stroke) {
    val path = Path().apply {
        moveTo(w * 0.16f, w * 0.46f)
        lineTo(w * 0.5f, w * 0.18f)
        lineTo(w * 0.84f, w * 0.46f)
        moveTo(w * 0.26f, w * 0.38f)
        lineTo(w * 0.26f, w * 0.82f)
        lineTo(w * 0.74f, w * 0.82f)
        lineTo(w * 0.74f, w * 0.38f)
    }
    drawPath(path, color, style = stroke)
}

private fun DrawScope.apple(w: Float, color: Color) {
    val body = Path().apply {
        moveTo(w * 0.5f, w * 0.34f)
        cubicTo(w * 0.3f, w * 0.22f, w * 0.1f, w * 0.34f, w * 0.12f, w * 0.58f)
        cubicTo(w * 0.14f, w * 0.8f, w * 0.32f, w * 0.96f, w * 0.5f, w * 0.88f)
        cubicTo(w * 0.68f, w * 0.96f, w * 0.86f, w * 0.8f, w * 0.88f, w * 0.58f)
        cubicTo(w * 0.9f, w * 0.34f, w * 0.7f, w * 0.22f, w * 0.5f, w * 0.34f)
        close()
    }
    drawPath(body, color)
    val leaf = Path().apply {
        moveTo(w * 0.5f, w * 0.3f)
        cubicTo(w * 0.5f, w * 0.12f, w * 0.62f, w * 0.04f, w * 0.76f, w * 0.06f)
        cubicTo(w * 0.76f, w * 0.2f, w * 0.66f, w * 0.3f, w * 0.5f, w * 0.3f)
        close()
    }
    drawPath(leaf, color)
}

private fun DrawScope.tiles(w: Float, color: Color) {
    listOf(0.1f to 0.1f, 0.54f to 0.1f, 0.1f to 0.54f, 0.54f to 0.54f).forEach { (x, y) ->
        drawRoundRect(
            color,
            topLeft = Offset(w * x, w * y),
            size = Size(w * 0.36f, w * 0.36f),
            cornerRadius = CornerRadius(w * 0.11f),
        )
    }
}

private fun DrawScope.basket(w: Float, color: Color) {
    // Ручка корзины.
    val handle = Path().apply {
        moveTo(w * 0.3f, w * 0.38f)
        lineTo(w * 0.42f, w * 0.12f)
        moveTo(w * 0.7f, w * 0.38f)
        lineTo(w * 0.58f, w * 0.12f)
    }
    drawPath(handle, color, style = Stroke(w * 0.09f, cap = StrokeCap.Round))
    val body = Path().apply {
        moveTo(w * 0.06f, w * 0.36f)
        lineTo(w * 0.94f, w * 0.36f)
        lineTo(w * 0.82f, w * 0.86f)
        quadraticTo(w * 0.8f, w * 0.92f, w * 0.72f, w * 0.92f)
        lineTo(w * 0.28f, w * 0.92f)
        quadraticTo(w * 0.2f, w * 0.92f, w * 0.18f, w * 0.86f)
        close()
    }
    drawPath(body, color)
    // Прорези в корзине — цветом фона не закрасить, поэтому светлые полосы.
    listOf(0.38f, 0.62f).forEach { x ->
        drawLine(Color.White, Offset(w * x, w * 0.52f), Offset(w * x, w * 0.76f), w * 0.08f, StrokeCap.Round)
    }
}

private fun DrawScope.person(w: Float, color: Color) {
    drawCircle(color, radius = w * 0.19f, center = Offset(w * 0.5f, w * 0.3f))
    val body = Path().apply {
        moveTo(w * 0.12f, w * 0.92f)
        cubicTo(w * 0.14f, w * 0.6f, w * 0.86f, w * 0.6f, w * 0.88f, w * 0.92f)
        close()
    }
    drawPath(body, color)
}

private fun DrawScope.list(w: Float, color: Color, stroke: Stroke) {
    drawRoundRect(
        color,
        topLeft = Offset(w * 0.16f, w * 0.18f),
        size = Size(w * 0.68f, w * 0.64f),
        cornerRadius = CornerRadius(w * 0.1f),
        style = stroke,
    )
    listOf(0.38f, 0.52f, 0.66f).forEachIndexed { i, y ->
        val end = if (i == 2) 0.54f else 0.68f
        drawLine(color, Offset(w * 0.32f, w * y), Offset(w * end, w * y), stroke.width, StrokeCap.Round)
    }
}
