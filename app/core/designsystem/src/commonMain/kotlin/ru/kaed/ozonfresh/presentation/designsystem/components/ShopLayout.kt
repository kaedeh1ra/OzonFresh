package ru.kaed.ozonfresh.presentation.designsystem.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

// Каркас экранов магазина: тёмная шапка с адресом, под ней белые скруглённые секции на сером фоне.

private val SECTION_RADIUS = 32.dp

/** Обещание доставки в шапке и на экранах. */
const val DELIVERY_ETA = "15–30 минут"

/**
 * Тёмная шапка: иконка приложения, «fresh • время доставки» и адрес.
 * Долгое нажатие — скрытый вход в демо-панель для защиты.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopHeader(address: String, onLongPress: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(FreshColors.Dark)
            .then(
                if (onLongPress != null) {
                    Modifier.combinedClickable(onClick = {}, onLongClick = onLongPress, onLongClickLabel = "Демо-панель")
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppMark()
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                "fresh  •  $DELIVERY_ETA",
                color = FreshColors.White,
                style = MaterialTheme.typography.titleMedium,
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
                NavIconView(NavIcon.ChevronDown, FreshColors.NavInactive, Modifier.padding(start = 4.dp), size = 18.dp)
            }
        }
    }
}

/** Своя иконка прототипа: бирюзовый квадрат с корзиной. Логотип бренда не используем. */
@Composable
fun AppMark(modifier: Modifier = Modifier) {
    Box(
        modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(FreshColors.Teal, FreshColors.Pink))),
        contentAlignment = Alignment.Center,
    ) {
        NavIconView(NavIcon.Products, FreshColors.White, size = 24.dp)
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
        .padding(bottom = if (bottom) 8.dp else 0.dp)
        .clip(shape)
        .background(FreshColors.White)
}

/** Целая белая секция, когда она помещается в один элемент списка. */
@Composable
fun ShopSection(
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.sectionPart(top = true, bottom = true, onDark = onDark).padding(vertical = 20.dp), content = content)
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
            .height(54.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(FreshColors.Card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (onValueChange == null) Arrangement.Center else Arrangement.Start,
    ) {
        NavIconView(NavIcon.Search, FreshColors.Muted, size = 26.dp)
        val textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = FreshColors.Ink)
        if (onValueChange == null) {
            Text(placeholder, style = textStyle.copy(color = FreshColors.Muted), modifier = Modifier.padding(start = 10.dp))
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

/** Иконка раздела в горизонтальном ряду: «Молочное», «Хлеб»… */
@Composable
fun CategoryIcon(emoji: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.width(84.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)).background(FreshColors.Card),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 38.sp)
        }
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

/** Плитка каталога: название сверху слева, картинка снизу справа. */
@Composable
fun CategoryTile(title: String, emoji: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.height(150.dp).clip(RoundedCornerShape(24.dp)).background(FreshColors.Card).clickable(onClick = onClick),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(14.dp),
        )
        Text(emoji, fontSize = 56.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp))
    }
}
