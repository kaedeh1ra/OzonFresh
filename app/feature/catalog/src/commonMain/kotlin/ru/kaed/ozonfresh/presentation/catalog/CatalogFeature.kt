package ru.kaed.ozonfresh.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import ru.kaed.ozonfresh.presentation.designsystem.components.MessageState
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.SearchBar
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.SheetScreen
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.gridColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.sectionPart
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

/** Каталог: простой — поиск, плитки разделов и сетка товаров раздела. */
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
    CatalogScreen(
        state = state,
        categoryId = categoryId,
        onIntent = viewModel::onIntent,
        onOpenCategory = { navigator.push(Destinations.catalogCategory(it)) },
        onOpenProduct = { navigator.push(Destinations.product(it)) },
        onBack = navigator::up,
    )
}

@Composable
fun CatalogScreen(
    state: CatalogContract.State,
    categoryId: String?,
    onIntent: (CatalogContract.Intent) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenProduct: (String) -> Unit,
    onBack: () -> Unit,
) {
    val padding = windowSize.contentPadding
    val columns = windowSize.gridColumns
    val category = state.categories.firstOrNull { it.id == categoryId }
    if (category != null) {
        // Раздел открыт поверх каталога: белый лист с заголовком и сеткой товаров.
        SheetScreen {
            ScreenHeader(category.title, onBack)
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)) {
                productGrid(state.productsIn(category.id), columns, state, onIntent, onOpenProduct, padding)
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().background(FreshColors.Canvas)) {
        item(key = "header") { ShopHeader(state.address) }
        item(key = "search") {
            ShopSection(onDark = true, contentPadding = 12.dp) {
                SearchBar(
                    Modifier.padding(horizontal = padding),
                    value = state.query,
                    onValueChange = { onIntent(CatalogContract.Intent.Search(it)) },
                )
                if (state.query.isBlank()) {
                    CategoryGrid(state, padding, onOpenCategory)
                }
            }
        }
        when {
            state.query.isNotBlank() && state.searchResults.isEmpty() -> item(key = "empty") {
                ShopSection { MessageState("Ничего не нашли", description = "Попробуйте другое слово") }
            }

            state.query.isNotBlank() -> {
                item(key = "results-title") {
                    SectionTitle(
                        "Нашли ${state.searchResults.size}",
                        Modifier.sectionPart(top = true, bottom = false).padding(start = padding, end = padding, top = 20.dp, bottom = 14.dp),
                    )
                }
                productGrid(state.searchResults, columns, state, onIntent, onOpenProduct, padding, inSection = true)
            }

            else -> Unit
        }
    }
}

/** Плитки разделов как в каталоге приложения: первая строка — широкая плитка и узкая, дальше по три. */
@Composable
private fun CategoryGrid(state: CatalogContract.State, padding: Dp, onOpenCategory: (String) -> Unit) {
    val first = state.categories.take(2)
    val rest = state.categories.drop(2).chunked(3)
    Column(Modifier.padding(horizontal = padding).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            first.forEachIndexed { index, item ->
                CategoryTile(item.title, item.emoji, { onOpenCategory(item.id) }, Modifier.weight(if (index == 0) 2f else 1f))
            }
        }
        rest.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    CategoryTile(item.title, item.emoji, { onOpenCategory(item.id) }, Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun LazyListScope.productGrid(
    products: List<Product>,
    columns: Int,
    state: CatalogContract.State,
    onIntent: (CatalogContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    padding: Dp,
    inSection: Boolean = false,
) {
    val rows = products.chunked(columns)
    itemsIndexed(rows, key = { _, row -> "grid-" + row.first().id }) { index, row ->
        val last = index == rows.lastIndex
        Row(
            (if (inSection) Modifier.sectionPart(top = false, bottom = last) else Modifier)
                .padding(start = padding, end = padding, bottom = if (last) 16.dp else 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
