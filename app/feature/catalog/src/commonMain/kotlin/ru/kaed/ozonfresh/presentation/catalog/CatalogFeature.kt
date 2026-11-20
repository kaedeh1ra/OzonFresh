package ru.kaed.ozonfresh.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureEntry
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.screen
import ru.kaed.ozonfresh.presentation.designsystem.components.MessageState
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.gridColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
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
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<CatalogContract.State, CatalogContract.Intent, Nothing>(
    CatalogContract.State(categories = catalog.categories, products = catalog.products),
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
    Column(Modifier.fillMaxSize()) {
        if (category != null) {
            ScreenHeader("${category.emoji} ${category.title}", onBack)
        } else {
            Text(
                "Каталог",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = padding, end = padding, top = 16.dp, bottom = 8.dp),
            )
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(CatalogContract.Intent.Search(it)) },
                placeholder = { Text("Искать продукты") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = padding),
            )
        }

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) {
            when {
                category != null -> productGrid(state.productsIn(category.id), columns, state, onIntent, onOpenProduct, padding)
                state.query.isNotBlank() && state.searchResults.isEmpty() -> item {
                    MessageState("Ничего не нашли", description = "Попробуйте другое слово")
                }

                state.query.isNotBlank() -> productGrid(state.searchResults, columns, state, onIntent, onOpenProduct, padding)
                else -> items(state.categories.chunked(2), key = { it.first().id }) { row ->
                    Row(
                        Modifier.padding(horizontal = padding).padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        row.forEach { item ->
                            CategoryTile(item, Modifier.weight(1f)) { onOpenCategory(item.id) }
                        }
                        repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: Category, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .height(96.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(category.title, style = MaterialTheme.typography.titleSmall)
        Text(category.emoji, fontSize = 30.sp, modifier = Modifier.align(Alignment.End))
    }
}

private fun LazyListScope.productGrid(
    products: List<Product>,
    columns: Int,
    state: CatalogContract.State,
    onIntent: (CatalogContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    padding: Dp,
) {
    items(products.chunked(columns), key = { "grid-" + it.first().id }) { row ->
        Row(
            Modifier.padding(horizontal = padding).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { product ->
                ProductCard(
                    name = product.name,
                    unit = product.unit,
                    price = product.price.rub(),
                    emoji = product.emoji,
                    tint = product.tint,
                    quantity = state.inCart[product.id] ?: 0,
                    onClick = { onOpenProduct(product.id) },
                    onAdd = { onIntent(CatalogContract.Intent.Add(product.id)) },
                    onQuantityChange = { onIntent(CatalogContract.Intent.ChangeQuantity(product.id, it)) },
                    modifier = Modifier.weight(1f),
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
