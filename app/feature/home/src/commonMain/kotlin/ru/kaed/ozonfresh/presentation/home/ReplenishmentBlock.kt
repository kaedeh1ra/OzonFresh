package ru.kaed.ozonfresh.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import ru.kaed.ozonfresh.presentation.designsystem.components.Badge
import ru.kaed.ozonfresh.presentation.designsystem.components.CarouselPriceStyle
import ru.kaed.ozonfresh.presentation.designsystem.components.NameWithUnit
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductImage
import ru.kaed.ozonfresh.presentation.designsystem.components.QuantityStepper
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors


/** Шире обычной карусели (126 dp): на карточке бейдж «Скоро закончится», кнопка и «Ещё есть» / «Позже». */
private val CARD_WIDTH = 146.dp

/**
 * Блок «Пора пополнить»: белая секция выше обычных подборок. Только подсказки: «Добавить» кладёт
 * товар в обычную корзину, цена обычная, без подписок и скидок.
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
    ShopSection(contentPadding = 20.dp, bottomPadding = 12.dp) {
        SectionTitle(
            "Пора пополнить",
            Modifier.padding(horizontal = horizontalPadding),
            subtitle = "Подобрали по вашим прошлым заказам",
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    Column(Modifier.width(CARD_WIDTH)) {
        Column(Modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onOpen)) {
            ProductImage(product.emoji, Modifier.fillMaxWidth().aspectRatio(1f), productId = product.id) {
                Badge(
                    text = if (overdue) "Пора пополнить" else "Скоро закончится",
                    background = if (overdue) FreshColors.Pink else FreshColors.Teal,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                )
            }
            Text(
                product.price.rub(),
                style = CarouselPriceStyle,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
            )
            Text(
                "Берёте раз в ${item.forecast.intervalDays} дн.",
                color = FreshColors.TealText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp),
            )
            NameWithUnit(product.name, product.unit, Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp))
        }

        if (quantity == 0) {
            Button(
                onClick = onAdd,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(36.dp),
            ) {
                Text("Добавить", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
            }
        } else {
            QuantityStepper(quantity, onQuantityChange, Modifier.padding(top = 8.dp).fillMaxWidth(), height = 36.dp)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
        modifier = Modifier.height(32.dp),
    ) {
        Text(text, color = FreshColors.Muted, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), fontWeight = FontWeight.SemiBold)
    }
}
