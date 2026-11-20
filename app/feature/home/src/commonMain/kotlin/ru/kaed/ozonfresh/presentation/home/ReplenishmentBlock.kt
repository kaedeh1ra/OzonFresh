package ru.kaed.ozonfresh.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kaed.ozonfresh.domain.model.Day
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentItem
import ru.kaed.ozonfresh.presentation.designsystem.components.Pill
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductImage
import ru.kaed.ozonfresh.presentation.designsystem.components.QuantityStepper
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

private val CARD_WIDTH = 172.dp

/**
 * Блок «Пора пополнить»: выше обычных подборок, на бирюзовом фоне, чтобы отличаться от рекламы.
 * Только подсказки: «Добавить» кладёт товар в обычную корзину, цена обычная, без подписок и скидок.
 */
@Composable
internal fun ReplenishmentBlock(
    items: List<ReplenishmentItem>,
    today: Day,
    inCart: Map<String, Int>,
    horizontalPadding: Dp,
    onOpen: (String) -> Unit,
    onAdd: (ReplenishmentItem) -> Unit,
    onQuantityChange: (String, Int) -> Unit,
    onStillHave: (ReplenishmentItem) -> Unit,
    onLater: (ReplenishmentItem) -> Unit,
) {
    Column(
        Modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FreshColors.TealSoft)
            .padding(vertical = 16.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("Пора пополнить", style = MaterialTheme.typography.titleLarge, color = FreshColors.Ink)
            Text(
                "Подобрали по вашим прошлым заказам",
                style = MaterialTheme.typography.bodyMedium,
                color = FreshColors.TealDark,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            items(items, key = { it.product.id }) { item ->
                ReplenishmentCard(
                    item = item,
                    today = today,
                    quantity = inCart[item.product.id] ?: 0,
                    onOpen = { onOpen(item.product.id) },
                    onAdd = { onAdd(item) },
                    onQuantityChange = { onQuantityChange(item.product.id, it) },
                    onStillHave = { onStillHave(item) },
                    onLater = { onLater(item) },
                )
            }
        }
    }
}

@Composable
private fun ReplenishmentCard(
    item: ReplenishmentItem,
    today: Day,
    quantity: Int,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onStillHave: () -> Unit,
    onLater: () -> Unit,
) {
    val product = item.product
    val overdue = item.daysUntilDue(today) <= 0
    Column(
        Modifier
            .width(CARD_WIDTH)
            .clip(RoundedCornerShape(20.dp))
            .background(FreshColors.White)
            .padding(10.dp),
    ) {
        Column(Modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onOpen)) {
            ProductImage(
                product.emoji,
                product.tint,
                Modifier.fillMaxWidth().height(96.dp),
                emojiSize = 40.sp,
                shape = RoundedCornerShape(14.dp),
            ) {
                Pill(
                    text = if (overdue) "Пора пополнить" else "Скоро закончится",
                    background = if (overdue) FreshColors.Pink else FreshColors.Teal,
                    content = FreshColors.White,
                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
                )
            }
            Text(
                product.price.rub(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                product.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${product.unit} · берёте раз в ${item.forecast.intervalDays} дн.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (quantity == 0) {
            Button(
                onClick = onAdd,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.padding(top = 10.dp).fillMaxWidth().height(36.dp),
            ) {
                Text("Добавить")
            }
        } else {
            QuantityStepper(quantity, onQuantityChange, Modifier.padding(top = 10.dp).fillMaxWidth())
        }

        Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            SmallAction("Ещё есть", onStillHave)
            SmallAction("Позже", onLater)
        }
    }
}

@Composable
private fun SmallAction(text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        modifier = Modifier.height(36.dp),
    ) {
        Text(text, color = FreshColors.TealDark, style = MaterialTheme.typography.labelLarge)
    }
}
