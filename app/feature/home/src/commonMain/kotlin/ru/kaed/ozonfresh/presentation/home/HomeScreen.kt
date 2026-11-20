package ru.kaed.ozonfresh.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.discountPercent
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.FREE_DELIVERY_FROM
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.presentation.designsystem.components.CAROUSEL_CARD_WIDTH
import ru.kaed.ozonfresh.presentation.designsystem.components.CategoryTile
import ru.kaed.ozonfresh.presentation.designsystem.components.CouponCard
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_EDGE
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_GAP
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIconView
import ru.kaed.ozonfresh.presentation.designsystem.components.NewGreen
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.PromoBanner
import ru.kaed.ozonfresh.presentation.designsystem.components.PromoStory
import ru.kaed.ozonfresh.presentation.designsystem.components.STORY_ROW_START
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.SearchBar
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.TILE_GAP
import ru.kaed.ozonfresh.presentation.designsystem.components.TILE_HEIGHT
import ru.kaed.ozonfresh.presentation.designsystem.components.TILE_WIDTH
import ru.kaed.ozonfresh.presentation.designsystem.components.productColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.sectionPart
import ru.kaed.ozonfresh.presentation.designsystem.components.storyImage
import ru.kaed.ozonfresh.presentation.designsystem.components.tilePhoto
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.home.HomeContract.Effect
import ru.kaed.ozonfresh.presentation.home.HomeContract.Intent
import ru.kaed.ozonfresh.presentation.home.HomeContract.State
import ru.kaed.ozonfresh.presentation.mvi.CollectEffects

@Composable
internal fun HomeRoute(navigator: AppNavigator) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is Effect.ShowMessage -> scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(effect.text)
            }
        }
    }
    // Показ ленты фиксируется, пока главная на экране; новый эпизод решает use case.
    val feedKey = state.feed?.let { feed -> feed.group to feed.eligible.map { it.windowKey } }
    LaunchedEffect(feedKey) { if (feedKey != null) viewModel.onIntent(Intent.FeedShown) }

    Box(Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenProduct = { navigator.push(Destinations.product(it)) },
            onOpenCategory = { navigator.push(Destinations.catalogCategory(it)) },
            onOpenSearch = { navigator.open(Destinations.CATALOG) },
            onOpenDemo = { navigator.push(Destinations.DEMO) },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(8.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    state: State,
    onIntent: (Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDemo: () -> Unit,
) {
    val padding = ScreenPadding
    val columns = productColumns
    LazyColumn(Modifier.fillMaxSize().background(FreshColors.Canvas)) {
        // Долгое нажатие на адрес — вход в демо-панель для защиты.
        // Как в приложении: шапка с адресом и поиск остаются наверху, лента прокручивается под ними.
        stickyHeader(key = "header") {
            Column(Modifier.background(FreshColors.Dark)) {
                ShopHeader(state.address, onOpenDemo = onOpenDemo)
                Column(Modifier.sectionPart(top = true, bottom = false, onDark = true).padding(top = 8.dp, bottom = 8.dp)) {
                    SearchBar(Modifier.padding(horizontal = 8.dp), onClick = onOpenSearch)
                }
            }
        }

        item(key = "top") {
            Column(Modifier.sectionPart(top = false, bottom = true).padding(bottom = 8.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = STORY_ROW_START),
                    modifier = Modifier.padding(top = 11.dp),
                ) {
                    items(PROMO_STORIES, key = { it.second }) { (emoji, title) ->
                        PromoStory(
                            emoji,
                            title,
                            onClick = { onIntent(Intent.NotInPrototype(title.replace("\n", " "))) },
                            image = storyImage(title),
                        )
                    }
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = padding),
                    horizontalArrangement = Arrangement.spacedBy(TILE_GAP),
                    modifier = Modifier.padding(top = 17.dp),
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryTile(
                            category.title,
                            category.emoji,
                            { onOpenCategory(category.id) },
                            Modifier.size(TILE_WIDTH, TILE_HEIGHT),
                            photo = tilePhoto(category.id),
                        )
                    }
                }
                PromoBanner(
                    title = "Активируйте скидку",
                    subtitle = "На три покупки в приложении",
                    badge = "−30%",
                    onClick = { onIntent(Intent.NotInPrototype("Активируйте скидку")) },
                    modifier = Modifier.padding(start = padding, end = padding, top = 16.dp),
                )
            }
        }

        item(key = "coupon") {
            ShopSection {
                val left = FREE_DELIVERY_FROM - state.cartTotal
                // Купон не растягивается на широком экране: справа у него сумма и полоска фиксированной ширины.
                CouponCard(
                    emoji = "🚚",
                    title = "Доставка 0 ₽",
                    description = "Закажите от ${FREE_DELIVERY_FROM.rub()} — привезём бесплатно",
                    chip = "КАЖДЫЙ ДЕНЬ",
                    remaining = if (left > 0) "Ещё ${left.rub()}" else "Готово!",
                    progress = state.cartTotal.toFloat() / FREE_DELIVERY_FROM,
                    modifier = Modifier.padding(horizontal = padding).widthIn(max = 420.dp),
                )
            }
        }

        val today = state.today
        if (state.visibleFeed.isNotEmpty() && today != null) {
            item(key = "replenishment") {
                ReplenishmentBlock(
                    items = state.visibleFeed,
                    today = today,
                    inCart = state.inCart,
                    horizontalPadding = padding,
                    onOpen = onOpenProduct,
                    onAdd = { onIntent(Intent.AddFromFeed(it)) },
                    onQuantityChange = { id, qty -> onIntent(Intent.ChangeQuantity(id, qty)) },
                    onStillHave = { onIntent(Intent.StillHave(it)) },
                    onLater = { onIntent(Intent.Later(it)) },
                )
            }
        }

        // Лента как на главной приложения: две строки сетки, подборка, ещё две строки, подборка, остальное.
        val chunk = columns * 2
        val products = state.products
        productGrid("grid-a", products.take(chunk), columns, state, onIntent, onOpenProduct)
        if (state.weeklySale.isNotEmpty()) {
            item(key = "sale") { ProductCarousel("Скидки недели", state.weeklySale, state, onIntent, onOpenProduct) }
        }
        productGrid("grid-b", products.drop(chunk).take(chunk), columns, state, onIntent, onOpenProduct)
        if (state.novelties.isNotEmpty()) {
            item(key = "new") { ProductCarousel("Новинки", state.novelties, state, onIntent, onOpenProduct) }
        }
        productGrid("grid-c", products.drop(chunk * 2), columns, state, onIntent, onOpenProduct)
    }
}

/** Кусок ленты сеткой — отдельная белая секция, как между подборками в приложении. */
private fun LazyListScope.productGrid(
    key: String,
    products: List<Product>,
    columns: Int,
    state: State,
    onIntent: (Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
) {
    val rows = products.chunked(columns)
    itemsIndexed(rows, key = { _, row -> key + "-" + row.first().id }) { index, row ->
        Row(
            Modifier.sectionPart(top = index == 0, bottom = index == rows.lastIndex)
                .padding(top = if (index == 0) 4.dp else 0.dp)
                .padding(start = GRID_EDGE, end = GRID_EDGE, bottom = if (index == rows.lastIndex) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(GRID_GAP),
        ) {
            row.forEach { product ->
                ShopProductCard(product, state, onIntent, onOpenProduct, Modifier.weight(1f))
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/** Подборка «Скидки недели» / «Новинки»: заголовок с кнопкой «›» и горизонтальный ряд карточек. */
@Composable
private fun ProductCarousel(
    title: String,
    products: List<Product>,
    state: State,
    onIntent: (Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
) {
    ShopSection(contentPadding = 20.dp, bottomPadding = 12.dp) {
        Row(Modifier.fillMaxWidth().padding(start = ScreenPadding, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(title, Modifier.weight(1f))
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(FreshColors.Card)
                    .clickable(onClickLabel = "Все товары подборки") { onIntent(Intent.NotInPrototype(title)) },
                contentAlignment = Alignment.Center,
            ) {
                NavIconView(NavIcon.Back, FreshColors.Muted, Modifier.rotate(180f), size = 18.dp)
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(top = 14.dp),
        ) {
            items(products, key = { it.id }) { product ->
                ShopProductCard(product, state, onIntent, onOpenProduct, Modifier.width(CAROUSEL_CARD_WIDTH), carousel = true)
            }
        }
    }
}

/** Ряд «историй» как в приложении: в прототипе это витрина, разделы за ними не сделаны. */
private val PROMO_STORIES = listOf(
    "🏷️" to "Все скидки\nи акции",
    "⭐" to "Вкусные\nновинки",
    "🛍️" to "Прошлые\nпокупки",
    "⏰" to "Съешьте\nскорее",
    "💖" to "Любимые\nтовары",
    "❓" to "Вопросы\nи ответы",
)

@Composable
private fun ShopProductCard(
    product: Product,
    state: State,
    onIntent: (Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier,
    carousel: Boolean = false,
) {
    ProductCard(
        name = product.name,
        unit = product.unit,
        price = product.price.rub(),
        emoji = product.emoji,
        quantity = state.inCart[product.id] ?: 0,
        onClick = { onOpenProduct(product.id) },
        onAdd = { onIntent(Intent.Add(product.id)) },
        onQuantityChange = { onIntent(Intent.ChangeQuantity(product.id, it)) },
        modifier = modifier,
        productId = product.id,
        carousel = carousel,
        oldPrice = product.oldPrice?.rub(),
        discountPercent = product.discountPercent,
        tag = if (product.isNew) "Новинка" else null,
        tagColor = NewGreen,
        tagIcon = if (product.isNew) NavIcon.Star else null,
        rating = product.rating,
        reviews = product.reviews,
    )
}
