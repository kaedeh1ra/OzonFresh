package ru.kaed.ozonfresh.presentation.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ru.kaed.ozonfresh.presentation.designsystem.theme.DarkPalette
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

// Каркас экранов магазина: тёмная шапка с адресом, под ней белые скруглённые секции на сером фоне.
// Размеры в dp сняты со скриншотов приложения на телефоне 1280×2772 при 520 dpi (3,25 px на dp).

private val SECTION_RADIUS = 28.dp

/** Серый промежуток между секциями. */
val SECTION_GAP = 8.dp

/** Обещание доставки в шапке и на экранах. */
const val DELIVERY_ETA = "15–30 минут"

/**
 * Тёмная шапка высотой 61 dp под статус-баром: иконка 32 dp, «fresh • время доставки», адрес и крестик справа.
 * Скрытые входы в демо-панель для защиты: долгое нажатие на шапку и крестик.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopHeader(address: String, onOpenDemo: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(61.dp)
            .background(FreshColors.Dark)
            .then(
                if (onOpenDemo != null) {
                    Modifier.combinedClickable(onClick = {}, onLongClick = onOpenDemo, onLongClickLabel = "Демо-панель")
                } else {
                    Modifier
                },
            ),
    ) {
        AppMark(Modifier.offset(x = 16.dp, y = 12.dp))
        val textStart = Modifier.padding(start = 56.dp, end = if (onOpenDemo != null) 64.dp else 16.dp)
        Text(
            // Как в приложении: точка серая и с одинарными пробелами.
            buildAnnotatedString {
                append("fresh ")
                withStyle(SpanStyle(color = FreshColors.NavInactive)) { append("•") }
                append(" $DELIVERY_ETA")
            },
            color = FreshColors.White,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp, lineHeight = 16.sp),
            maxLines = 1,
            modifier = textStart.paddingFromBaseline(top = 25.dp),
        )
        Row(textStart.paddingFromBaseline(top = 41.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                address,
                color = FreshColors.NavInactive,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.5.sp, lineHeight = 16.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            NavIconView(NavIcon.ChevronDown, FreshColors.NavInactive, Modifier.padding(start = 3.dp), size = 14.dp)
        }
        if (onOpenDemo != null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 26.dp).size(32.dp).clip(CircleShape)
                    .background(FreshColors.DarkButton)
                    .clickable(onClickLabel = "Демо-панель", onClick = onOpenDemo),
                contentAlignment = Alignment.Center,
            ) {
                NavIconView(NavIcon.Close, FreshColors.White, size = 15.dp)
            }
        }
    }
}

/** Своя иконка прототипа: градиентный квадрат с яблоком. Логотип бренда не используем. */
@Composable
fun AppMark(modifier: Modifier = Modifier) {
    Box(
        modifier.size(32.dp).clip(RoundedCornerShape(9.dp))
            .background(Brush.linearGradient(listOf(FreshColors.Teal, FreshColors.Pink))),
        contentAlignment = Alignment.Center,
    ) {
        NavIconView(NavIcon.Products, FreshColors.White, size = 17.dp)
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
        .padding(bottom = if (bottom) SECTION_GAP else 0.dp)
        .clip(shape)
        .background(FreshColors.Surface)
}

/** Целая белая секция, когда она помещается в один элемент списка. */
@Composable
fun ShopSection(
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
    contentPadding: Dp = 16.dp,
    bottomPadding: Dp = contentPadding,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.sectionPart(top = true, bottom = true, onDark = onDark).padding(top = contentPadding, bottom = bottomPadding),
        content = content,
    )
}

/**
 * Вложенный экран (карточка товара, оформление): белый лист со скруглением сверху, как у модальных экранов
 * референса. Полоса над листом прозрачная: при жесте «назад» сквозь неё виден экран под шторкой.
 */
@Composable
fun SheetScreen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Column(
            modifier.fillMaxSize().clip(RoundedCornerShape(topStart = SECTION_RADIUS, topEnd = SECTION_RADIUS))
                .background(FreshColors.Surface)
                .navigationBarsPadding()
                .imePadding(),
            content = content,
        )
    }
}

/**
 * Строка поиска. Без [onValueChange] — кнопка по центру (главная: 48 dp, края 8 dp), с ним — поле
 * с текстом слева (каталог: 40 dp, края 16 dp). [leftAligned] — кнопка в виде поля, как на «Готовом».
 */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String = "",
    onValueChange: ((String) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    placeholder: String = "Найти с быстрой доставкой",
    leftAligned: Boolean = onValueChange != null,
) {
    val field = leftAligned
    Row(
        modifier
            .fillMaxWidth()
            .height(if (field) 40.dp else 48.dp)
            .clip(RoundedCornerShape(if (field) 13.dp else 16.dp))
            .background(FreshColors.Card)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = if (field) 8.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (field) Arrangement.Start else Arrangement.Center,
    ) {
        NavIconView(NavIcon.Search, FreshColors.Muted, size = 19.dp)
        val textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp, color = FreshColors.Ink)
        if (onValueChange == null) {
            Text(placeholder, style = textStyle.copy(color = FreshColors.Muted), modifier = Modifier.padding(start = 6.dp))
        } else {
            Box(Modifier.padding(start = 6.dp).weight(1f)) {
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

/** Шаг «историй» в ряду под поиском и отступ первой. */
val STORY_WIDTH = 69.5.dp
val STORY_ROW_START = 8.dp

/** «История» в ряду под поиском: картинка 47 dp на светлой подложке и подпись в две строки. */
@Composable
fun PromoStory(emoji: String, title: String, onClick: () -> Unit, modifier: Modifier = Modifier, image: DrawableResource? = null) {
    Column(
        modifier.width(STORY_WIDTH).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(47.dp).clip(RoundedCornerShape(12.dp)).background(FreshColors.Card), contentAlignment = Alignment.Center) {
            // Объёмная картинка, как иконки историй в приложении; без неё — эмодзи.
            if (image != null) {
                Image(painterResource(image), contentDescription = null, modifier = Modifier.size(42.dp))
            } else {
                Text(emoji, fontSize = 34.sp)
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 15.4.sp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            modifier = Modifier.paddingFromBaseline(top = 12.dp),
        )
    }
}

/** Плитки разделов в ряду под историями: 100 × 134 dp, промежуток 6,5 dp. */
val TILE_WIDTH = 100.dp
val TILE_HEIGHT = 134.dp
val TILE_GAP = 6.5.dp

/** Плитка раздела: название сверху слева, картинка снизу справа. В ряду под поиском и в каталоге. */
@Composable
fun CategoryTile(
    title: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emojiSize: Int = 56,
    photo: DrawableResource? = null,
) {
    Box(
        modifier.clip(RoundedCornerShape(18.dp)).background(FreshColors.Card).clickable(onClick = onClick),
    ) {
        if (photo != null) {
            // Предмет без фона крупно в правом нижнем углу и чуть заходит за край плитки, как в приложении.
            BoxWithConstraints(Modifier.matchParentSize()) {
                val side = minOf(maxWidth, maxHeight - 22.dp) * 0.95f
                Image(
                    painterResource(photo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = side * 0.06f, y = side * 0.05f).size(side),
                )
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp, lineHeight = 13.8.sp),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp, end = 6.dp).paddingFromBaseline(top = 18.dp),
        )
        if (photo == null) {
            Text(emoji, fontSize = emojiSize.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 2.dp))
        }
    }
}

/** Градиентный баннер акции 54,5 dp: заголовок, подпись и белая плашка с цифрой. */
@Composable
fun PromoBanner(title: String, subtitle: String, badge: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(54.5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFFE9A5E), Color(0xFFFF6FA8), Color(0xFFFD6BBC))))
            .clickable(onClick = onClick),
    ) {
        Text(
            title,
            color = FreshColors.White,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.25.sp, lineHeight = 17.sp),
            modifier = Modifier.padding(start = 12.dp).paddingFromBaseline(top = 19.5.dp),
        )
        Text(
            subtitle,
            color = FreshColors.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.6.sp, lineHeight = 15.sp),
            modifier = Modifier.padding(start = 12.dp).paddingFromBaseline(top = 40.5.dp),
        )
        Text(
            badge,
            color = FreshColors.OnWhite,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp).rotate(-8f)
                .clip(RoundedCornerShape(10.dp)).background(FreshColors.White)
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/**
 * Купон с прогрессом, 92 dp: слева иконка и условие, справа (101 dp) сколько осталось.
 * [progress] от 0 до 1 — полоска 48 dp под суммой.
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
    val dark = FreshColors.palette === DarkPalette
    Row(
        modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(20.dp)).background(FreshColors.Card),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            emoji,
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.Top).padding(start = 7.dp, top = 11.dp).width(45.dp),
        )
        Column(Modifier.weight(1f).padding(start = 7.dp, end = 8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp, lineHeight = 19.sp),
                maxLines = 1,
            )
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp, lineHeight = 15.sp),
                color = FreshColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                chip,
                color = if (dark) Color(0xFFB4ADFF) else Color(0xFF5B4FE0),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp, lineHeight = 12.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp).clip(RoundedCornerShape(6.dp))
                    .background(if (dark) Color(0xFF34336B) else Color(0xFFE8EAF9)).padding(horizontal = 6.dp, vertical = 1.5.dp),
            )
        }
        // Пунктир между частями купона.
        Canvas(Modifier.width(2.dp).height(76.dp)) {
            var y = 0f
            while (y < size.height) {
                drawCircle(FreshColors.Surface, radius = size.width, center = Offset(size.width / 2, y))
                y += size.width * 5
            }
        }
        Column(
            Modifier.width(101.dp).padding(horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                remaining,
                color = FreshColors.TealText,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Box(Modifier.padding(top = 9.dp).width(48.dp).height(4.dp).clip(CircleShape).background(FreshColors.Line)) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(4.dp).clip(CircleShape).background(FreshColors.Teal))
            }
        }
    }
}
