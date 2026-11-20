package ru.kaed.ozonfresh.presentation.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.kaed.ozonfresh.domain.model.Category
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.AddToCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ChangeCartQuantityUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.GetCatalogUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveTodayUseCase
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureEntry
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.screen
import ru.kaed.ozonfresh.presentation.designsystem.components.CategoryTile
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_EDGE
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_GAP
import ru.kaed.ozonfresh.presentation.designsystem.components.LocalWindowWidth
import ru.kaed.ozonfresh.presentation.designsystem.components.MessageState
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.SearchBar
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.SheetScreen
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.TILE_GRID_EDGE
import ru.kaed.ozonfresh.presentation.designsystem.components.TileCell
import ru.kaed.ozonfresh.presentation.designsystem.components.TileGrid
import ru.kaed.ozonfresh.presentation.designsystem.components.productColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.sectionPart
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

/** Каталог как в приложении: шапка, прилипающий поиск, подборки и секции с плитками разделов; сетка товаров раздела. */
internal class CatalogPlugin : FeaturePlugin {
    override val routes = listOf(Destinations.CATALOG, Destinations.CATALOG_CATEGORY)
    override val entry = FeatureEntry.Tab("Каталог", NavIcon.Catalog, order = 1)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.CATALOG) { CatalogRoute(categoryId = null, host.navigator) }
        screen(Destinations.CATALOG_CATEGORY) { entry ->
            CatalogRoute(entry.savedStateHandle.get<String>("category"), host.navigator)
        }
    }
}

object CatalogContract {
    data class State(
        val categories: List<Category> = emptyList(),
        val products: List<Product> = emptyList(),
        val query: String = "",
        val inCart: Map<String, Int> = emptyMap(),
        val address: String = "",
    ) {
        fun productsIn(categoryId: String) = products.filter { it.categoryId == categoryId }
        val searchResults: List<Product>
            get() = if (query.isBlank()) emptyList() else products.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    sealed interface Intent {
        data class Search(val query: String) : Intent
        data class Add(val productId: String) : Intent
        data class ChangeQuantity(val productId: String, val quantity: Int) : Intent
    }
}

class CatalogViewModel(
    catalog: GetCatalogUseCase,
    today: ObserveTodayUseCase,
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<CatalogContract.State, CatalogContract.Intent, Nothing>(
    CatalogContract.State(categories = catalog.categories, products = catalog.products, address = today.address),
) {
    init {
        observeCart().onEach { cart ->
            setState { copy(inCart = cart.items.associate { it.product.id to it.quantity }) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: CatalogContract.Intent) {
        when (intent) {
            is CatalogContract.Intent.Search -> setState { copy(query = intent.query) }
            is CatalogContract.Intent.Add -> addToCart(intent.productId)
            is CatalogContract.Intent.ChangeQuantity -> changeQuantity(intent.productId, intent.quantity)
        }
    }
}

@Composable
private fun CatalogRoute(categoryId: String?, navigator: AppNavigator) {
    val viewModel = koinViewModel<CatalogViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        CatalogScreen(
            state = state,
            categoryId = categoryId,
            onIntent = viewModel::onIntent,
            onOpenCategory = { navigator.push(Destinations.catalogCategory(it)) },
            onOpenProduct = { navigator.push(Destinations.product(it)) },
            onBack = navigator::up,
            onPromo = { title ->
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar("«$title» — в прототипе не работает")
                }
            },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(8.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CatalogScreen(
    state: CatalogContract.State,
    categoryId: String?,
    onIntent: (CatalogContract.Intent) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenProduct: (String) -> Unit,
    onBack: () -> Unit,
    onPromo: (String) -> Unit = {},
) {
    val columns = productColumns
    val category = state.categories.firstOrNull { it.id == categoryId }
    if (category != null) {
        // Раздел открыт поверх каталога: белый лист с заголовком и сеткой товаров.
        SheetScreen {
            ScreenHeader(category.title, onBack)
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
                productGrid(state.productsIn(category.id), columns, state, onIntent, onOpenProduct)
            }
        }
        return
    }
    // Как в приложении: шапка с адресом уезжает при прокрутке, а поиск прилипает к верху.
    LazyColumn(Modifier.fillMaxSize().background(FreshColors.Canvas)) {
        item(key = "header") { ShopHeader(state.address) }
        stickyHeader(key = "search") {
            Box(Modifier.background(FreshColors.Dark)) {
                Column(Modifier.sectionPart(top = true, bottom = false, onDark = true).padding(top = 10.dp, bottom = 10.dp)) {
                    SearchBar(
                        Modifier.padding(horizontal = ScreenPadding),
                        value = state.query,
                        onValueChange = { onIntent(CatalogContract.Intent.Search(it)) },
                    )
                }
            }
        }
        if (state.query.isBlank()) {
            val byId = state.categories.associateBy { it.id }
            item(key = "promo") {
                Column(Modifier.sectionPart(top = false, bottom = true).padding(top = 3.dp, bottom = TILE_GRID_EDGE)) {
                    TileGrid(PROMO_TILES.map { tile -> tile.toCell { onPromo(tile.title) } })
                }
            }
            CATALOG_SECTIONS.forEach { section ->
                val cells = section.tiles.mapNotNull { tile ->
                    byId[tile.id]?.let { c -> TileCell(c.title, c.emoji, tile.cols, tile.rows) { onOpenCategory(c.id) } }
                }
                if (cells.isNotEmpty()) {
                    item(key = "section-" + section.title) {
                        ShopSection(contentPadding = 20.dp, bottomPadding = TILE_GRID_EDGE) {
                            SectionTitle(section.title, Modifier.padding(horizontal = ScreenPadding))
                            TileGrid(cells, Modifier.padding(top = 14.dp))
                        }
                    }
                }
            }
        } else if (state.searchResults.isEmpty()) {
            item(key = "empty") {
                Column(Modifier.sectionPart(top = false, bottom = true)) {
                    MessageState("Ничего не нашли", description = "Попробуйте другое слово")
                }
            }
        } else {
            item(key = "results-title") {
                SectionTitle(
                    "Нашли ${state.searchResults.size}",
                    Modifier.sectionPart(top = false, bottom = false).padding(start = ScreenPadding, end = ScreenPadding, top = 6.dp, bottom = 12.dp),
                )
            }
            productGrid(state.searchResults, columns, state, onIntent, onOpenProduct, inSection = true)
        }
    }
}

private class TileSpec(val id: String, val cols: Int = 1, val rows: Int = 1)

private class PromoSpec(val title: String, val emoji: String, val cols: Int = 1) {
    fun toCell(onClick: () -> Unit) = TileCell(title, emoji, cols, 1, onClick)
}

private class CatalogSection(val title: String, val tiles: List<TileSpec>)

/** Подборки над разделами: в прототипе не работают, как промо-блоки на главной. */
private val PROMO_TILES = listOf(
    PromoSpec("Хиты недели", "🔥", cols = 2),
    PromoSpec("Новинки", "✨"),
    PromoSpec("Здоровое питание", "🥗"),
    PromoSpec("Скидки", "🏷️"),
    PromoSpec("Съешьте скорее", "⏰"),
)

/** Разделы, сгруппированные как в каталоге приложения: высокая и широкая плитки задают ритм секции. */
private val CATALOG_SECTIONS = listOf(
    CatalogSection(
        "Продукты",
        listOf(TileSpec("dairy", rows = 2), TileSpec("fruits", cols = 2), TileSpec("bakery"), TileSpec("grocery")),
    ),
    CatalogSection("Напитки", listOf(TileSpec("drinks"), TileSpec("coffee", cols = 2))),
    CatalogSection("Для дома и питомцев", listOf(TileSpec("pets", cols = 2), TileSpec("household"))),
)

private fun LazyListScope.productGrid(
    products: List<Product>,
    columns: Int,
    state: CatalogContract.State,
    onIntent: (CatalogContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    inSection: Boolean = false,
) {
    val rows = products.chunked(columns)
    itemsIndexed(rows, key = { _, row -> "grid-" + row.first().id }) { index, row ->
        val last = index == rows.lastIndex
        Row(
            (if (inSection) Modifier.sectionPart(top = false, bottom = last) else Modifier)
                .padding(start = GRID_EDGE, end = GRID_EDGE, bottom = if (last) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(GRID_GAP),
        ) {
            row.forEach { product ->
                ProductCard(
                    name = product.name,
                    unit = product.unit,
                    price = product.price.rub(),
                    emoji = product.emoji,
                    quantity = state.inCart[product.id] ?: 0,
                    onClick = { onOpenProduct(product.id) },
                    onAdd = { onIntent(CatalogContract.Intent.Add(product.id)) },
                    onQuantityChange = { onIntent(CatalogContract.Intent.ChangeQuantity(product.id, it)) },
                    modifier = Modifier.weight(1f),
                    productId = product.id,
                )
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `catalogFeatureModule` в этом пакете. */
val catalogFeatureModule = module {
    viewModelOf(::CatalogViewModel)
    singleOf(::CatalogPlugin) bind FeaturePlugin::class
}
