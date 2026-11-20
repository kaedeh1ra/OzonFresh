package ru.kaed.ozonfresh.presentation.ready

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.discountPercent
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
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_EDGE
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_GAP
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.SearchBar
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.TileCell
import ru.kaed.ozonfresh.presentation.designsystem.components.TileGrid
import ru.kaed.ozonfresh.presentation.designsystem.components.WindowSize
import ru.kaed.ozonfresh.presentation.designsystem.components.productColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.sectionPart
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

/**
 * «Готовое» — вкладка как в приложении: витрина готовой еды плитками и «Подобрали для вас».
 * Готовой еды в демо-данных нет, поэтому плитки — витрина («в прототипе не работает»), а подборка — хиты недели.
 */
internal class ReadyPlugin : FeaturePlugin {
    override val routes = listOf(Destinations.READY)
    override val entry = FeatureEntry.Tab("Готовое", NavIcon.Ready, order = 2)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.READY) { ReadyRoute(host.navigator) }
    }
}

object ReadyContract {
    data class State(
        val address: String = "",
        val picks: List<Product> = emptyList(),
        val inCart: Map<String, Int> = emptyMap(),
    )

    sealed interface Intent {
        data class Add(val productId: String) : Intent
        data class ChangeQuantity(val productId: String, val quantity: Int) : Intent
    }
}

class ReadyViewModel(
    today: ObserveTodayUseCase,
    catalog: GetCatalogUseCase,
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<ReadyContract.State, ReadyContract.Intent, Nothing>(
    ReadyContract.State(address = today.address, picks = catalog.weeklyHits),
) {
    init {
        observeCart().onEach { cart ->
            setState { copy(inCart = cart.items.associate { it.product.id to it.quantity }) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: ReadyContract.Intent) {
        when (intent) {
            is ReadyContract.Intent.Add -> addToCart(intent.productId)
            is ReadyContract.Intent.ChangeQuantity -> changeQuantity(intent.productId, intent.quantity)
        }
    }
}

@Composable
private fun ReadyRoute(navigator: AppNavigator) {
    val viewModel = koinViewModel<ReadyViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        ReadyScreen(
            state = state,
            onIntent = viewModel::onIntent,
            onOpenProduct = { navigator.push(Destinations.product(it)) },
            onOpenSearch = { navigator.open(Destinations.CATALOG) },
            onOpenDemo = { navigator.push(Destinations.DEMO) },
            onShowcase = { title ->
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar("«$title» — в прототипе не работает")
                }
            },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(8.dp))
    }
}

/** Плитка витрины: название, картинка и размер в клетках сетки. */
private class Showcase(val title: String, val emoji: String, val cols: Int = 1)

private class ShowcaseSection(val title: String?, val tiles: List<Showcase>)

/** Секции по скриншоту приложения: без заголовка сверху, «Горячее от Hot&fresh», «Вся готовая еда», «Основное меню». */
private val SHOWCASE = listOf(
    ShowcaseSection(null, listOf(Showcase("Новинки", "✨"), Showcase("Скидки", "🏷️"), Showcase("Съешьте скорее", "⏰"))),
    ShowcaseSection("Горячее от Hot&fresh", listOf(Showcase("Блюда", "🌭", cols = 2), Showcase("Напитки", "🥤"))),
    ShowcaseSection("Вся готовая еда", listOf(Showcase("Найдите свой вкус", "🍜", cols = 2), Showcase("Комбо", "🍱"))),
    ShowcaseSection(
        "Основное меню",
        listOf(
            Showcase("Завтраки", "🥞"),
            Showcase("Вторые блюда", "🍝"),
            Showcase("Стритфуд", "🍔"),
            Showcase("Супы", "🍲"),
            Showcase("Закуски и салаты", "🥗"),
            Showcase("Десерты", "🍰"),
        ),
    ),
)

/** Соседние секции не шире трёх клеток — пары; остальные идут по одной. Заголовок есть у обеих или ни у одной. */
private fun List<ShowcaseSection>.pairUp(): List<List<ShowcaseSection>> {
    val result = mutableListOf<List<ShowcaseSection>>()
    var i = 0
    while (i < size) {
        val a = this[i]
        val b = getOrNull(i + 1)
        val fits = { s: ShowcaseSection -> s.tiles.sumOf { it.cols } <= 3 }
        if (b != null && fits(a) && fits(b) && (a.title == null) == (b.title == null)) {
            result += listOf(a, b); i += 2
        } else {
            result += listOf(a); i += 1
        }
    }
    return result
}

/** Экран одним белым листом, как в приложении: секции идут подряд без серых промежутков. */
@Composable
fun ReadyScreen(
    state: ReadyContract.State,
    onIntent: (ReadyContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDemo: () -> Unit,
    onShowcase: (String) -> Unit,
) {
    val columns = productColumns
    val wide = windowSize != WindowSize.Compact
    LazyColumn(Modifier.fillMaxSize().background(FreshColors.Canvas)) {
        item(key = "header") { ShopHeader(state.address, onOpenDemo = onOpenDemo) }
        item(key = "search") {
            Column(Modifier.sectionPart(top = true, bottom = false, onDark = true).padding(top = 10.dp, bottom = 13.dp)) {
                SearchBar(Modifier.padding(horizontal = ScreenPadding), onClick = onOpenSearch, leftAligned = true)
            }
        }
        // На широком экране секции по три клетки встают парами, чтобы не оставлять пустую половину строки.
        val rowsOfSections = if (wide) SHOWCASE.pairUp() else SHOWCASE.map { listOf(it) }
        rowsOfSections.forEach { pair ->
            item(key = "showcase-" + pair.joinToString { it.title ?: "top" }) {
                Row(Modifier.sectionPart(top = false, bottom = false).padding(bottom = 16.dp)) {
                    pair.forEach { section ->
                        Column(Modifier.weight(1f)) {
                            if (pair.any { it.title != null }) {
                                SectionTitle(
                                    section.title.orEmpty(),
                                    Modifier.padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp, bottom = 14.dp),
                                )
                            }
                            TileGrid(
                                section.tiles.map { TileCell(it.title, it.emoji, it.cols, 1) { onShowcase(it.title) } },
                                columns = if (pair.size > 1) 3 else null,
                            )
                        }
                    }
                }
            }
        }
        item(key = "picks-title") {
            SectionTitle(
                "Подобрали для вас",
                Modifier.sectionPart(top = false, bottom = false).padding(start = ScreenPadding, end = ScreenPadding, top = 16.dp, bottom = 12.dp),
            )
        }
        val rows = state.picks.chunked(columns)
        itemsIndexed(rows, key = { _, row -> "picks-" + row.first().id }) { index, row ->
            val last = index == rows.lastIndex
            Row(
                Modifier.sectionPart(top = false, bottom = last)
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
                        onAdd = { onIntent(ReadyContract.Intent.Add(product.id)) },
                        onQuantityChange = { onIntent(ReadyContract.Intent.ChangeQuantity(product.id, it)) },
                        modifier = Modifier.weight(1f),
                        productId = product.id,
                        oldPrice = product.oldPrice?.rub(),
                        discountPercent = product.discountPercent,
                        rating = product.rating,
                        reviews = product.reviews,
                    )
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `readyFeatureModule` в этом пакете. */
val readyFeatureModule = module {
    viewModelOf(::ReadyViewModel)
    singleOf(::ReadyPlugin) bind FeaturePlugin::class
}
