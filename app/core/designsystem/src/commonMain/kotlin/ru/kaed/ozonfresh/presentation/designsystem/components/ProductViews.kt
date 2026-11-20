package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    Box(modifier.clip(shape).background(FreshColors.PhotoBg), contentAlignment = Alignment.Center) {
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

/**
 * «− 2 +»: в бирюзовой плашке (корзина, лента, карточка товара) или, с [onPhoto], в белой с тенью поверх фото
 * товара, как в сетке приложения.
 */
@Composable
fun QuantityStepper(
    quantity: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp,
    onPhoto: Boolean = false,
) {
    val content = if (onPhoto) FreshColors.OnWhite else Color.White
    Row(
        modifier.height(height)
            .then(if (onPhoto) Modifier.shadow(6.dp, CircleShape) else Modifier)
            .clip(CircleShape).background(if (onPhoto) FreshColors.White else FreshColors.Teal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StepperButton("−", "Убрать одну штуку", height, content, onPhoto) { onChange(quantity - 1) }
        Text(
            "$quantity",
            color = content,
            style = if (onPhoto) MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp) else MaterialTheme.typography.labelLarge,
        )
        StepperButton("+", "Добавить ещё одну", height, content, onPhoto) { onChange(quantity + 1) }
    }
}

@Composable
private fun StepperButton(symbol: String, label: String, size: Dp, color: Color, large: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(size).clip(CircleShape).clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = color, fontSize = if (large) 24.sp else 19.sp, fontWeight = if (large) FontWeight.Normal else FontWeight.Bold)
    }
}

/**
 * Кнопка корзины на фото товара. После добавления белый кружок выезжает влево на всю ширину и превращается
 * в «− 1 +», как в приложении; при нуле штук сворачивается обратно.
 */
@Composable
fun CartControl(quantity: Int, onAdd: () -> Unit, onChange: (Int) -> Unit, size: Dp, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
        val expanded = quantity > 0
        val width by animateDpAsState(if (expanded) maxWidth else size, tween(CART_EXPAND_MS, easing = FastOutSlowInEasing))
        // Содержимое меняется, когда плашка почти раскрылась (или только начала сворачиваться).
        val stepperAlpha by animateFloatAsState(if (expanded) 1f else 0f, tween(CART_EXPAND_MS / 2, delayMillis = if (expanded) CART_EXPAND_MS / 2 else 0))
        // Последнее ненулевое количество: пока плашка сворачивается, в ней не мелькает «0».
        var shown by remember { mutableIntStateOf(quantity.coerceAtLeast(1)) }
        if (quantity > 0) shown = quantity
        Box(
            Modifier.width(width).height(size).shadow(6.dp, CircleShape).clip(CircleShape).background(FreshColors.White),
            contentAlignment = Alignment.Center,
        ) {
            if (stepperAlpha < 1f) {
                Box(
                    Modifier.align(Alignment.CenterEnd).size(size).graphicsLayer { alpha = 1f - stepperAlpha }
                        .clickable(enabled = !expanded, role = Role.Button, onClickLabel = "В корзину", onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    NavIconView(NavIcon.Cart, FreshColors.OnWhite, size = size * 0.5f, cutout = FreshColors.White)
                }
            }
            if (stepperAlpha > 0f) {
                Row(
                    Modifier.fillMaxWidth().graphicsLayer { alpha = stepperAlpha },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StepperButton("−", "Убрать одну штуку", size, FreshColors.OnWhite, true) { onChange(shown - 1) }
                    Text("$shown", color = FreshColors.OnWhite, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp), maxLines = 1)
                    StepperButton("+", "Добавить ещё одну", size, FreshColors.OnWhite, true) { onChange(shown + 1) }
                }
            }
        }
    }
}

private const val CART_EXPAND_MS = 260

/** Белая круглая кнопка с корзиной поверх картинки товара. */
@Composable
fun BasketButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 38.dp) {
    Box(
        modifier.size(size).shadow(6.dp, CircleShape).clip(CircleShape).background(FreshColors.White)
            .clickable(role = Role.Button, onClickLabel = "В корзину", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavIconView(NavIcon.Cart, FreshColors.OnWhite, size = size * 0.5f, cutout = FreshColors.White)
    }
}

/** Розовый бейдж в углу картинки: «Пора пополнить», «−12%». */
@Composable
fun Badge(text: String, background: Color, modifier: Modifier = Modifier, content: Color = FreshColors.White) {
    Text(
        text,
        color = content,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = modifier.clip(CircleShape).background(background).padding(horizontal = 7.dp, vertical = 1.5.dp),
    )
}

/** Название и вес одной строкой: вес серым, как в карточках референса. */
@Composable
fun NameWithUnit(name: String, unit: String, modifier: Modifier = Modifier, lines: Int = 2, minLines: Int = lines) {
    Text(
        buildAnnotatedString {
            append(name)
            withStyle(SpanStyle(color = FreshColors.Muted)) { append("  $unit") }
        },
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 18.5.sp),
        maxLines = lines,
        minLines = minLines,
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
    tagIcon: NavIcon? = null,
    productId: String? = null,
    carousel: Boolean = false,
    oldPrice: String? = null,
    discountPercent: Int? = null,
    rating: Double? = null,
    reviews: Int? = null,
) {
    Column(modifier.clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)) {
        ProductImage(emoji, Modifier.fillMaxWidth().aspectRatio(1f), productId = productId) {
            if (discountPercent != null) {
                Badge(
                    "-$discountPercent%",
                    FreshColors.Pink,
                    Modifier.align(Alignment.TopStart).padding(start = if (carousel) 10.dp else 12.dp, top = if (carousel) 12.dp else 14.dp),
                )
            }
            // Сердечко в вырезе угла, как в приложении; нажатие добавляет товар в избранное.
            val favorite = productId != null && productId in Favorites.ids
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-6).dp).size(32.dp).clip(CircleShape)
                    .background(FreshColors.Surface)
                    .then(
                        if (productId != null) {
                            Modifier.clickable(role = Role.Button, onClickLabel = if (favorite) "Убрать из избранного" else "В избранное") {
                                Favorites.toggle(productId)
                            }
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                NavIconView(NavIcon.Heart, if (favorite) HeartRed else HeartGray, Modifier.offset(x = (-2).dp, y = 2.dp), size = 20.dp)
            }
            CartControl(
                quantity = quantity,
                onAdd = onAdd,
                onChange = onQuantityChange,
                size = if (carousel) 36.dp else 43.dp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(start = 6.dp, end = 6.dp, bottom = 4.dp),
            )
        }
        Row(
            Modifier.padding(start = if (carousel) 4.dp else 8.dp, top = if (carousel) 6.dp else 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(price, style = if (carousel) CarouselPriceStyle else PriceStyle)
            if (oldPrice != null) {
                // Зелёная плашка карты и зачёркнутая цена, как у скидок в приложении.
                Box(
                    Modifier.padding(start = 5.dp).size(17.dp, 12.dp).clip(RoundedCornerShape(3.dp)).background(FreshColors.Bank),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(Modifier.padding(top = 3.dp).fillMaxWidth().height(2.dp).background(Color.White.copy(alpha = 0.55f)))
                }
                Text(
                    oldPrice,
                    color = FreshColors.Muted,
                    style = TextStyle(fontSize = if (carousel) 13.sp else 15.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.LineThrough),
                    modifier = Modifier.padding(start = 5.dp),
                )
            }
        }
        if (tag != null) {
            Row(Modifier.padding(start = if (carousel) 4.dp else 8.dp, top = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                if (tagIcon != null) NavIconView(tagIcon, tagColor, Modifier.padding(end = 5.dp), size = 15.dp)
                Text(
                    tag,
                    color = tagColor,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        val inset = if (carousel) 4.dp else 8.dp
        val withRating = rating != null && reviews != null
        // С рейтингом, как в приложении, он идёт сразу за названием, даже если оно в одну строку.
        NameWithUnit(
            name,
            unit,
            Modifier.padding(start = inset, end = inset, bottom = if (withRating) 0.dp else 8.dp),
            minLines = if (withRating) 1 else 2,
        )
        if (withRating) RatingRow(rating!!, reviews!!, Modifier.padding(start = inset, top = 4.dp, bottom = 8.dp))
    }
}

/** «★ 4.9  💬 15 801» под названием. */
@Composable
fun RatingRow(rating: Double, reviews: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        NavIconView(NavIcon.Star, StarOrange, size = 16.dp)
        Text(
            (kotlin.math.round(rating * 10) / 10).toString(),
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 5.dp),
        )
        NavIconView(NavIcon.Comment, FreshColors.NavInactive, Modifier.padding(start = 10.dp), size = 14.dp)
        Text(
            groupThousands(reviews),
            color = FreshColors.Muted,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}

/** 15801 → «15 801», как в приложении (узкий неразрывный пробел). */
private fun groupThousands(n: Int): String = n.toString().reversed().chunked(3).joinToString("\u202F").reversed()

/** Избранное прототипа: живёт, пока открыто приложение, на ленту и корзину не влияет. */
object Favorites {
    val ids = mutableStateSetOf<String>()
    fun toggle(id: String) {
        if (!ids.remove(id)) ids += id
    }
}

/** Зелёная метка «Новинка». */
val NewGreen = Color(0xFF2DBE60)

private val HeartGray = Color(0xFFC6CCD4)
private val HeartRed = Color(0xFFFF4D4D)
private val StarOrange = Color(0xFFFFA800)

/** Цена в карточке товара: в сетке крупная, в горизонтальной подборке мельче, как в приложении. */
val PriceStyle = TextStyle(fontSize = 21.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
val CarouselPriceStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)

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
