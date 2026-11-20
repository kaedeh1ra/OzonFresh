package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

/**
 * Картинка товара: фото из ресурсов, если оно есть для [productId] (см. ProductPhotos), иначе эмодзи
 * на светлом фоне.
 */
@Composable
fun ProductImage(
    emoji: String,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 56.sp,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    productId: String? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val photo = productId?.let(::productPhoto)
    Box(modifier.clip(shape).background(FreshColors.Card), contentAlignment = Alignment.Center) {
        if (photo != null) {
            Image(
                painterResource(photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(emoji, fontSize = emojiSize, textAlign = TextAlign.Center)
        }
        overlay()
    }
}

/** «− 2 +» в бирюзовой плашке. */
@Composable
fun QuantityStepper(
    quantity: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
) {
    Row(
        modifier.height(height).clip(CircleShape).background(FreshColors.Teal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StepperButton("−", "Убрать одну штуку", height) { onChange(quantity - 1) }
        Text("$quantity", color = Color.White, style = MaterialTheme.typography.labelLarge)
        StepperButton("+", "Добавить ещё одну", height) { onChange(quantity + 1) }
    }
}

@Composable
private fun StepperButton(symbol: String, label: String, size: Dp, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(CircleShape).clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    }
}

/** Белая круглая кнопка с корзиной поверх картинки товара. */
@Composable
fun BasketButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 38.dp) {
    Box(
        modifier.size(size).shadow(6.dp, CircleShape).clip(CircleShape).background(FreshColors.White)
            .clickable(role = Role.Button, onClickLabel = "В корзину", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavIconView(NavIcon.Cart, FreshColors.Ink, size = size * 0.5f)
    }
}

/** Розовый бейдж в углу картинки: «Пора пополнить», «−12%». */
@Composable
fun Badge(text: String, background: Color, modifier: Modifier = Modifier, content: Color = FreshColors.White) {
    Text(
        text,
        color = content,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier.clip(CircleShape).background(background).padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

/** Название и вес одной строкой: вес серым, как в карточках референса. */
@Composable
fun NameWithUnit(name: String, unit: String, modifier: Modifier = Modifier, lines: Int = 2) {
    Text(
        buildAnnotatedString {
            append(name)
            withStyle(SpanStyle(color = FreshColors.Muted)) { append("  $unit") }
        },
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 18.5.sp),
        maxLines = lines,
        minLines = lines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/**
 * Карточка товара для сеток и подборок, как в приложении: квадратная картинка, сердечко, круглая кнопка
 * корзины 43 dp, цена 21 sp, название 15 sp с весом. Когда товар уже в корзине, вместо кнопки — степпер.
 */
@Composable
fun ProductCard(
    name: String,
    unit: String,
    price: String,
    emoji: String,
    quantity: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tag: String? = null,
    tagColor: Color = FreshColors.TealText,
    productId: String? = null,
    carousel: Boolean = false,
) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)) {
        ProductImage(emoji, Modifier.fillMaxWidth().aspectRatio(1f), productId = productId) {
            // Сердечко в белом вырезе угла, как в приложении.
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-6).dp).size(32.dp).clip(CircleShape).background(FreshColors.White),
                contentAlignment = Alignment.Center,
            ) {
                NavIconView(NavIcon.Heart, HeartGray, Modifier.offset(x = (-2).dp, y = 2.dp), size = 20.dp)
            }
            if (quantity == 0) {
                BasketButton(
                    onAdd,
                    Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 4.dp),
                    size = if (carousel) 36.dp else 43.dp,
                )
            } else {
                QuantityStepper(
                    quantity,
                    onQuantityChange,
                    Modifier.align(Alignment.BottomCenter).padding(start = 6.dp, end = 6.dp, bottom = 4.dp).fillMaxWidth(),
                    height = 40.dp,
                )
            }
        }
        Text(
            price,
            style = if (carousel) CarouselPriceStyle else PriceStyle,
            modifier = Modifier.padding(start = if (carousel) 4.dp else 8.dp, top = if (carousel) 6.dp else 7.dp),
        )
        if (tag != null) {
            Text(
                tag,
                color = tagColor,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        val inset = if (carousel) 4.dp else 8.dp
        NameWithUnit(name, unit, Modifier.padding(start = inset, end = inset, bottom = 8.dp))
    }
}

private val HeartGray = Color(0xFFC6CCD4)

/** Цена в карточке товара: в сетке крупная, в горизонтальной подборке мельче, как в приложении. */
val PriceStyle = TextStyle(fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold, color = FreshColors.Ink)
val CarouselPriceStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, color = FreshColors.Ink)

/** Заголовок секции: «Пора пополнить», «Вы покупали». */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier) {
        Text(text, style = MaterialTheme.typography.headlineSmall)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** Маленькая плашка на светлом фоне: «Из «Пора пополнить»», статусы в демо-панели. */
@Composable
fun Pill(text: String, background: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        color = content,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier.clip(CircleShape).background(background).padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Строка заголовка вложенного экрана: «‹» и название. */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            Box(
                Modifier.size(44.dp).clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Назад", onClick = onBack),
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
