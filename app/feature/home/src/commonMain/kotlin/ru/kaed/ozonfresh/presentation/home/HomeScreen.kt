package ru.kaed.ozonfresh.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.presentation.designsystem.components.CategoryIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.SearchBar
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.gridColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.sectionPart
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
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
    val padding = windowSize.contentPadding
    val columns = windowSize.gridColumns
    LazyColumn(Modifier.fillMaxSize().background(FreshColors.Canvas), contentPadding = PaddingValues(bottom = 8.dp)) {
        // Долгое нажатие на адрес — вход в демо-панель для защиты.
        item(key = "header") { ShopHeader(state.address, onLongPress = onOpenDemo) }

        item(key = "top") {
            ShopSection(onDark = true) {
                SearchBar(Modifier.padding(horizontal = padding), onClick = onOpenSearch)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = padding - 8.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryIcon(category.emoji, category.title, onClick = { onOpenCategory(category.id) })
                    }
                }
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
                ShopSection {
                    SectionTitle("Вы покупали", Modifier.padding(horizontal = padding))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = padding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 14.dp),
                    ) {
                        items(state.purchased, key = { it.id }) { product ->
                            ShopProductCard(product, state, onIntent, onOpenProduct, Modifier.width(170.dp))
                        }
                    }
                }
            }
        }

        item(key = "hits-title") {
            SectionTitle(
                "Хиты недели",
                Modifier.sectionPart(top = true, bottom = false).padding(start = padding, end = padding, top = 20.dp, bottom = 14.dp),
            )
        }
        val rows = state.hits.chunked(columns)
        itemsIndexed(rows, key = { _, row -> "hits-" + row.first().id }) { index, row ->
            Row(
                Modifier.sectionPart(top = false, bottom = index == rows.lastIndex)
                    .padding(start = padding, end = padding, bottom = if (index == rows.lastIndex) 16.dp else 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { product ->
                    ShopProductCard(product, state, onIntent, onOpenProduct, Modifier.weight(1f))
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ShopProductCard(
    product: Product,
    state: State,
    onIntent: (Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier,
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
    )
}
