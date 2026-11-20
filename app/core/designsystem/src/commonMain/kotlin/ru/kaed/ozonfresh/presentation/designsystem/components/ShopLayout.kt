package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

// Каркас экранов магазина: тёмная шапка с адресом, под ней белые скруглённые секции на сером фоне.

private val SECTION_RADIUS = 28.dp

/** Обещание доставки в шапке и на экранах. */
const val DELIVERY_ETA = "15–30 минут"

/**
 * Тёмная шапка: иконка приложения, «fresh • время доставки», адрес и крестик справа.
 * Скрытые входы в демо-панель для защиты: долгое нажатие на шапку и крестик.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopHeader(address: String, onOpenDemo: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(FreshColors.Dark)
            .then(
                if (onOpenDemo != null) {
                    Modifier.combinedClickable(onClick = {}, onLongClick = onOpenDemo, onLongClickLabel = "Демо-панель")
                } else {
                    Modifier
                },
            )
            .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppMark()
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(
                "fresh  •  $DELIVERY_ETA",
                color = FreshColors.White,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    address,
                    color = FreshColors.NavInactive,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                NavIconView(NavIcon.ChevronDown, FreshColors.NavInactive, Modifier.padding(start = 3.dp), size = 14.dp)
            }
        }
        if (onOpenDemo != null) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(FreshColors.DarkButton)
                    .clickable(onClickLabel = "Демо-панель", onClick = onOpenDemo),
                contentAlignment = Alignment.Center,
            ) {
                NavIconView(NavIcon.Close, FreshColors.White, size = 18.dp)
            }
        }
    }
}

/** Своя иконка прототипа: градиентный квадрат с яблоком. Логотип бренда не используем. */
@Composable
fun AppMark(modifier: Modifier = Modifier) {
    Box(
        modifier.size(40.dp).clip(RoundedCornerShape(11.dp))
            .background(Brush.linearGradient(listOf(FreshColors.Teal, FreshColors.Pink))),
        contentAlignment = Alignment.Center,
    ) {
        NavIconView(NavIcon.Products, FreshColors.White, size = 20.dp)
    }
}

/**
 * Часть белой секции в LazyColumn. Секция может занимать несколько элементов списка (сетка товаров):
 * скругляются только верх первой части и низ последней. [onDark] — секция сразу под тёмной шапкой.
 */
fun Modifier.sectionPart(top: Boolean, bottom: Boolean, onDark: Boolean = false): Modifier {
    val shape = RoundedCornerShape(
        topStart = if (top) SECTION_RADIUS else 0.dp,
        topEnd = if (top) SECTION_RADIUS else 0.dp,
        bottomStart = if (bottom) SECTION_RADIUS else 0.dp,
        bottomEnd = if (bottom) SECTION_RADIUS else 0.dp,
    )
    // Под скруглёнными углами первой секции видна шапка, под остальными — серый фон.
    val behind = if (onDark && top) {
        Brush.verticalGradient(0f to FreshColors.Dark, 0.5f to FreshColors.Dark, 0.5f to FreshColors.Canvas, 1f to FreshColors.Canvas)
    } else {
        SolidColor(FreshColors.Canvas)
    }
    return this
        .fillMaxWidth()
        .background(behind)
        .padding(bottom = if (bottom) 6.dp else 0.dp)
        .clip(shape)
        .background(FreshColors.White)
}

/** Целая белая секция, когда она помещается в один элемент списка. */
@Composable
fun ShopSection(
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.sectionPart(top = true, bottom = true, onDark = onDark).padding(vertical = contentPadding), content = content)
}

/**
 * Вложенный экран (карточка товара, оформление): тёмная полоса сверху и белый лист со скруглением,
 * как у модальных экранов референса.
 */
@Composable
fun SheetScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(FreshColors.Dark).padding(top = 8.dp)) {
        Column(
            modifier.fillMaxSize().clip(RoundedCornerShape(topStart = SECTION_RADIUS, topEnd = SECTION_RADIUS))
                .background(FreshColors.White),
            content = content,
        )
    }
}

/** Строка поиска. Без [onValueChange] — кнопка, ведущая на экран поиска. */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    placeholder: String = "Найти с быстрой доставкой",
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FreshColors.Card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (onValueChange == null) Arrangement.Center else Arrangement.Start,
    ) {
        NavIconView(NavIcon.Search, FreshColors.Muted, size = 22.dp)
        val textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, color = FreshColors.Ink)
        if (onValueChange == null) {
            Text(placeholder, style = textStyle.copy(color = FreshColors.Muted), modifier = Modifier.padding(start = 8.dp))
        } else {
            Box(Modifier.padding(start = 10.dp).weight(1f)) {
                if (value.isEmpty()) Text(placeholder, style = textStyle.copy(color = FreshColors.Muted))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(FreshColors.Teal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** «История» в ряду под поиском: крупная картинка без подложки и подпись в две строки. */
@Composable
fun PromoStory(emoji: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.width(80.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 44.sp, modifier = Modifier.height(58.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
        )
    }
}

/** Плитка раздела: название сверху слева, картинка снизу справа. В ряду под поиском и в каталоге. */
@Composable
fun CategoryTile(title: String, emoji: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.height(158.dp).clip(RoundedCornerShape(20.dp)).background(FreshColors.Card).clickable(onClick = onClick),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, lineHeight = 17.sp),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp),
        )
        Text(emoji, fontSize = 70.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 6.dp, bottom = 4.dp))
    }
}

/** Градиентный баннер акции: заголовок, подпись и белая плашка с цифрой. */
@Composable
fun PromoBanner(title: String, subtitle: String, badge: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFE9A5E), Color(0xFFFF6FA8), Color(0xFFFD6BBC))))
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = FreshColors.White, style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp))
            Text(subtitle, color = FreshColors.White, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            badge,
            color = FreshColors.Ink,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.rotate(-8f).clip(RoundedCornerShape(10.dp)).background(FreshColors.White)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/**
 * Купон с прогрессом: слева иконка и условие, справа сколько осталось.
 * [progress] от 0 до 1 — полоска под суммой.
 */
@Composable
fun CouponCard(
    emoji: String,
    title: String,
    description: String,
    chip: String,
    remaining: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(FreshColors.Card),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 38.sp, modifier = Modifier.padding(start = 12.dp))
        Column(Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = FreshColors.Muted)
            Text(
                chip,
                color = Color(0xFF5B4FE0),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp).clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE9E7FC)).padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        // Пунктир между частями купона.
        Canvas(Modifier.width(2.dp).height(90.dp)) {
            var y = 0f
            while (y < size.height) {
                drawCircle(FreshColors.White, radius = size.width, center = Offset(size.width / 2, y))
                y += size.width * 5
            }
        }
        Column(
            Modifier.width(118.dp).padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                remaining,
                color = FreshColors.TealText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Box(
                Modifier.padding(top = 8.dp).fillMaxWidth().height(4.dp).clip(CircleShape).background(FreshColors.Line),
            ) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(4.dp).clip(CircleShape).background(FreshColors.Teal))
            }
        }
    }
}
