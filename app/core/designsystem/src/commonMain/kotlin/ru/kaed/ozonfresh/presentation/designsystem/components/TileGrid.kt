package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/** Плитка сетки разделов: квадрат 123 dp на телефоне, промежуток 4 dp, края 8 dp — как в каталоге приложения. */
val TILE_GRID_CELL = 123.dp
val TILE_GRID_GAP = 4.dp
val TILE_GRID_EDGE = 8.dp

/** Плитка в сетке: [cols] × [rows] клеток — обычная, широкая или высокая. */
class TileCell(val title: String, val emoji: String, val cols: Int, val rows: Int, val onClick: () -> Unit)

/**
 * Сетка плиток с широкими и высокими: плитки укладываются плотно, слева направо и сверху вниз.
 * Колонок на телефоне 3, на раскрытом складном 6 — размер клетки остаётся около 123 dp, высокие плитки там в одну строку.
 * [columns] задают явно, когда сетка занимает часть ширины (две секции рядом).
 */
@Composable
fun TileGrid(allCells: List<TileCell>, modifier: Modifier = Modifier, columns: Int? = null) {
    val width = LocalWindowWidth.current
    val columns = columns
        ?: ((width - TILE_GRID_EDGE * 2 + TILE_GRID_GAP) / (TILE_GRID_CELL + TILE_GRID_GAP)).toInt().coerceAtLeast(3)
    // На широком экране секция помещается в одну строку: высокая плитка оставила бы под соседями пустоту.
    val cells = if (columns > 3) allCells.map { TileCell(it.title, it.emoji, it.cols, 1, it.onClick) } else allCells
    val placement = remember(cells, columns) { pack(cells, columns) }
    Layout(
        content = {
            cells.forEach { c ->
                CategoryTile(c.title, c.emoji, c.onClick, emojiSize = if (c.rows > 1) 80 else 64)
            }
        },
        modifier = modifier.fillMaxWidth().padding(horizontal = TILE_GRID_EDGE),
    ) { measurables, constraints ->
        val gap = TILE_GRID_GAP.roundToPx()
        val cell = (constraints.maxWidth - gap * (columns - 1)) / columns
        val placeables = measurables.mapIndexed { i, m ->
            val c = cells[i]
            val w = cell * c.cols + gap * (c.cols - 1)
            val h = cell * c.rows + gap * (c.rows - 1)
            m.measure(Constraints.fixed(w, h))
        }
        val rows = placement.indices.maxOfOrNull { placement[it].second + cells[it].rows } ?: 0
        val height = (cell * rows + gap * (rows - 1)).coerceAtLeast(0)
        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { i, p ->
                val (col, row) = placement[i]
                p.place(col * (cell + gap), row * (cell + gap))
            }
        }
    }
}

/** Плотная укладка: каждая плитка встаёт в первое свободное место, где помещается целиком. */
private fun pack(cells: List<TileCell>, columns: Int): List<Pair<Int, Int>> {
    val taken = mutableSetOf<Pair<Int, Int>>()
    return cells.map { c ->
        val cols = c.cols.coerceAtMost(columns)
        fun fits(x: Int, y: Int) = (x until x + cols).all { cx -> (y until y + c.rows).all { cy -> (cx to cy) !in taken } }
        val spot = generateSequence(0) { it + 1 }.firstNotNullOf { y -> (0..columns - cols).firstOrNull { x -> fits(x, y) }?.let { it to y } }
        for (cx in spot.first until spot.first + cols) for (cy in spot.second until spot.second + c.rows) taken += cx to cy
        spot
    }
}
