package ru.kaed.ozonfresh.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.FREE_DELIVERY_FROM
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.presentation.designsystem.components.CAROUSEL_CARD_WIDTH
import ru.kaed.ozonfresh.presentation.designsystem.components.CategoryTile
import ru.kaed.ozonfresh.presentation.designsystem.components.CouponCard
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_EDGE
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_GAP
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
        item(key = "header") { ShopHeader(state.address, onOpenDemo = onOpenDemo) }

        item(key = "top") {
            ShopSection(onDark = true, contentPadding = 8.dp, bottomPadding = 8.dp) {
                SearchBar(Modifier.padding(horizontal = 8.dp), onClick = onOpenSearch)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = STORY_ROW_START),
                    modifier = Modifier.padding(top = 19.dp),
                ) {
                    items(PROMO_STORIES, key = { it.second }) { (emoji, title) ->
                        PromoStory(emoji, title, onClick = { onIntent(Intent.NotInPrototype(title.replace("\n", " "))) })
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

        if (state.purchased.isNotEmpty()) {
            item(key = "purchased") {
                ShopSection(contentPadding = 20.dp) {
                    SectionTitle("Вы покупали", Modifier.padding(horizontal = padding))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = padding),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        items(state.purchased, key = { it.id }) { product ->
                            ShopProductCard(product, state, onIntent, onOpenProduct, Modifier.width(CAROUSEL_CARD_WIDTH), carousel = true)
                        }
                    }
                }
            }
        }

        item(key = "hits-title") {
            SectionTitle(
                "Хиты недели",
                Modifier.sectionPart(top = true, bottom = false).padding(start = padding, end = padding, top = 20.dp, bottom = 12.dp),
            )
        }
        val rows = state.hits.chunked(columns)
        itemsIndexed(rows, key = { _, row -> "hits-" + row.first().id }) { index, row ->
            Row(
                Modifier.sectionPart(top = false, bottom = index == rows.lastIndex)
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
    )
}
