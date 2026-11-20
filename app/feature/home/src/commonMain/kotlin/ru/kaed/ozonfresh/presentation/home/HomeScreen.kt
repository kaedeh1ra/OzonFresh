package ru.kaed.ozonfresh.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.formatLong
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.model.weekdayShort
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIconView
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.gridColumns
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
    val padding = windowSize.contentPadding
    val columns = windowSize.gridColumns
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item(key = "header") {
            Column(
                Modifier
                    .fillMaxWidth()
                    // Долгое нажатие на адрес — вход в демо-панель для защиты.
                    .combinedClickable(onClick = {}, onLongClick = onOpenDemo, onLongClickLabel = "Демо-панель")
                    .padding(horizontal = padding, vertical = 12.dp),
            ) {
                Text(
                    "Доставим за 15–30 минут",
                    style = MaterialTheme.typography.labelLarge,
                    color = FreshColors.Teal,
                )
                Text(
                    "${state.address} ▾",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                state.today?.let {
                    Text(
                        "Сегодня ${it.weekdayShort()}, ${it.formatLong()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item(key = "search") {
            Row(
                Modifier
                    .padding(horizontal = padding)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavIconView(NavIcon.Search, MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
                Text(
                    "Искать продукты",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }

        item(key = "chips") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = padding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 14.dp),
            ) {
                items(state.categories, key = { it.id }) { category ->
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenCategory(category.id) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(category.emoji)
                        Text(
                            category.title,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 6.dp),
                        )
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
            item(key = "purchased-title") {
                SectionTitle("Вы покупали", Modifier.padding(start = padding, end = padding, top = 24.dp, bottom = 10.dp))
            }
            item(key = "purchased") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = padding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.purchased, key = { it.id }) { product ->
                        ShopProductCard(product, state, onIntent, onOpenProduct, Modifier.width(132.dp))
                    }
                }
            }
        }

        item(key = "hits-title") {
            SectionTitle("Хиты недели", Modifier.padding(start = padding, end = padding, top = 24.dp, bottom = 10.dp))
        }
        items(state.hits.chunked(columns), key = { row -> "hits-" + row.first().id }) { row ->
            Row(
                Modifier.padding(horizontal = padding).padding(bottom = 12.dp),
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
        tint = product.tint,
        quantity = state.inCart[product.id] ?: 0,
        onClick = { onOpenProduct(product.id) },
        onAdd = { onIntent(Intent.Add(product.id)) },
        onQuantityChange = { onIntent(Intent.ChangeQuantity(product.id, it)) },
        modifier = modifier,
    )
}
