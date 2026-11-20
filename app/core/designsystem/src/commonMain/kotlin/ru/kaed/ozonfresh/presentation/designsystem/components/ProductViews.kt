package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

/** Картинка-заглушка товара: цветной блок с эмодзи. Своих фото нет, чужие не используем. */
@Composable
fun ProductImage(
    emoji: String,
    tint: Long,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 44.sp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier.clip(shape).background(Color(tint)), contentAlignment = Alignment.Center) {
        Text(emoji, fontSize = emojiSize, textAlign = TextAlign.Center)
        overlay()
    }
}

/** «− 2 +» в розовой плашке. */
@Composable
fun QuantityStepper(
    quantity: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
) {
    Row(
        modifier.height(height).clip(CircleShape).background(FreshColors.Pink),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StepperButton("−", height) { onChange(quantity - 1) }
        Text(
            "$quantity",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
        )
        StepperButton("+", height) { onChange(quantity + 1) }
    }
}

@Composable
private fun StepperButton(symbol: String, size: Dp, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

/** Круглая кнопка «+» поверх картинки товара. */
@Composable
fun AddButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    Box(
        modifier.size(size).clip(CircleShape).background(FreshColors.Pink)
            .clickable(role = Role.Button, onClickLabel = "Добавить в корзину", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("+", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Карточка товара для сеток и подборок: картинка, цена, название, вес или объём, кнопка «+».
 * Когда товар уже в корзине, вместо «+» показывается степпер.
 */
@Composable
fun ProductCard(
    name: String,
    unit: String,
    price: String,
    emoji: String,
    tint: Long,
    quantity: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)) {
        ProductImage(emoji, tint, Modifier.fillMaxWidth().aspectRatio(1f)) {
            if (quantity == 0) {
                AddButton(onAdd, Modifier.align(Alignment.BottomEnd).padding(8.dp))
            } else {
                QuantityStepper(
                    quantity,
                    onQuantityChange,
                    Modifier.align(Alignment.BottomCenter).padding(8.dp).fillMaxWidth(),
                )
            }
        }
        Text(
            price,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp),
        )
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
        Text(
            unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 4.dp),
        )
    }
}

/** Заголовок раздела на экране: «Вы покупали», «Хиты недели». */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier) {
        Text(text, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Маленькая цветная плашка: «Скоро закончится», «Из ленты». */
@Composable
fun Pill(text: String, background: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        color = content,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        modifier = modifier.clip(CircleShape).background(background).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Строка заголовка вложенного экрана: «‹ Назад» и название. */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = "Назад", onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                NavIconView(NavIcon.Back, MaterialTheme.colorScheme.onSurface)
            }
        } else {
            Box(Modifier.width(8.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

/** Сетка карточек в LazyColumn: колонок 2 на телефоне, больше на широком экране. */
val WindowSize.gridColumns: Int
    get() = when (this) {
        WindowSize.Compact -> 2
        WindowSize.Medium -> 3
        WindowSize.Wide -> 5
    }
