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
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

// Иконки нарисованы на Canvas: отдельная библиотека иконок ради нескольких штук не нужна.
// Вкладки — залитые фигуры, как в нижней навигации референса; служебные (поиск, назад) — линиями.

enum class NavIcon { Home, Products, Catalog, Ready, Cart, Profile, List, Search, Back, ChevronDown, Close, Heart, Star, Comment }

@Composable
fun NavIconView(icon: NavIcon, color: Color, modifier: Modifier = Modifier, size: Dp = 24.dp, cutout: Color = FreshColors.Surface) {
    Canvas(modifier.size(size)) {
        val w = size.toPx()
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            NavIcon.Home -> home(w, color, stroke)
            NavIcon.Products -> apple(w, color)
            NavIcon.Catalog -> tiles(w, color)
            NavIcon.Ready -> bowl(w, color)
            NavIcon.Cart -> basket(w, color, cutout)
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

            NavIcon.Star -> {
                val path = Path()
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) w * 0.5f else w * 0.21f
                    val a = (-90.0 + i * 36.0) * kotlin.math.PI / 180.0
                    val x = w * 0.5f + r * kotlin.math.cos(a).toFloat()
                    val y = w * 0.54f + r * kotlin.math.sin(a).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color)
            }

            NavIcon.Comment -> {
                drawOval(color, topLeft = Offset(w * 0.04f, w * 0.1f), size = Size(w * 0.92f, w * 0.76f))
                val tail = Path().apply {
                    moveTo(w * 0.16f, w * 0.66f)
                    lineTo(w * 0.06f, w * 0.96f)
                    lineTo(w * 0.42f, w * 0.82f)
                    close()
                }
                drawPath(tail, color)
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

/** «Готовое»: миска с паром, как на вкладке приложения. */
private fun DrawScope.bowl(w: Float, color: Color) {
    val body = Path().apply {
        moveTo(w * 0.06f, w * 0.5f)
        lineTo(w * 0.94f, w * 0.5f)
        cubicTo(w * 0.94f, w * 0.76f, w * 0.74f, w * 0.92f, w * 0.5f, w * 0.92f)
        cubicTo(w * 0.26f, w * 0.92f, w * 0.06f, w * 0.76f, w * 0.06f, w * 0.5f)
        close()
    }
    drawPath(body, color)
    val steam = Stroke(w * 0.07f, cap = StrokeCap.Round)
    listOf(0.32f, 0.5f, 0.68f).forEach { x ->
        val path = Path().apply {
            moveTo(w * x, w * 0.4f)
            cubicTo(w * (x - 0.07f), w * 0.31f, w * (x + 0.07f), w * 0.22f, w * x, w * 0.1f)
        }
        drawPath(path, color, style = steam)
    }
}

private fun DrawScope.basket(w: Float, color: Color, cutout: Color) {
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
    // Прорези в корзине — цветом фона под иконкой.
    listOf(0.38f, 0.62f).forEach { x ->
        drawLine(cutout, Offset(w * x, w * 0.52f), Offset(w * x, w * 0.76f), w * 0.08f, StrokeCap.Round)
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
