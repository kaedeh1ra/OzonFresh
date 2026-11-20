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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Иконки нижней навигации, нарисованные линиями: отдельная библиотека иконок ради пары штук не нужна.

enum class NavIcon { Home, List, Profile }

@Composable
fun NavIconView(icon: NavIcon, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            NavIcon.Home -> {
                val path = Path().apply {
                    moveTo(w * 0.16f, h * 0.46f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.84f, h * 0.46f)
                    moveTo(w * 0.26f, h * 0.38f)
                    lineTo(w * 0.26f, h * 0.82f)
                    lineTo(w * 0.74f, h * 0.82f)
                    lineTo(w * 0.74f, h * 0.38f)
                }
                drawPath(path, color, style = stroke)
            }

            NavIcon.List -> {
                drawRoundRect(
                    color,
                    topLeft = Offset(w * 0.16f, h * 0.18f),
                    size = Size(w * 0.68f, h * 0.64f),
                    cornerRadius = CornerRadius(w * 0.1f),
                    style = stroke,
                )
                listOf(0.38f, 0.52f, 0.66f).forEachIndexed { i, y ->
                    val end = if (i == 2) 0.54f else 0.68f
                    drawLine(
                        color,
                        Offset(w * 0.32f, h * y),
                        Offset(w * end, h * y),
                        stroke.width,
                        StrokeCap.Round
                    )
                }
            }

            NavIcon.Profile -> {
                drawCircle(
                    color,
                    radius = w * 0.15f,
                    center = Offset(w * 0.5f, h * 0.36f),
                    style = stroke
                )
                val path = Path().apply {
                    moveTo(w * 0.2f, h * 0.84f)
                    cubicTo(w * 0.24f, h * 0.6f, w * 0.76f, h * 0.6f, w * 0.8f, h * 0.84f)
                }
                drawPath(path, color, style = stroke)
            }
        }
    }
}
