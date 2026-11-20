package ru.kaed.ozonfresh.presentation.cart

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.CartSource
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.AddToCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.CartItem
import ru.kaed.ozonfresh.domain.usecase.shop.CartSummary
import ru.kaed.ozonfresh.domain.usecase.shop.ChangeCartQuantityUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.FREE_DELIVERY_FROM
import ru.kaed.ozonfresh.domain.usecase.shop.GetCatalogUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveTodayUseCase
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.presentation.designsystem.components.Pill
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductImage
import ru.kaed.ozonfresh.presentation.designsystem.components.QuantityStepper
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.gridColumns
import ru.kaed.ozonfresh.presentation.designsystem.components.sectionPart
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

object CartContract {
    data class State(
        val address: String = "",
        val summary: CartSummary = CartSummary(emptyList()),
        /** «Рекомендуем» под пустой корзиной. */
        val recommended: List<Product> = emptyList(),
    )

    sealed interface Intent {
        data class Add(val productId: String) : Intent
        data class ChangeQuantity(val productId: String, val quantity: Int) : Intent
    }
}

class CartViewModel(
    today: ObserveTodayUseCase,
    catalog: GetCatalogUseCase,
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<CartContract.State, CartContract.Intent, Nothing>(
    CartContract.State(address = today.address, recommended = catalog.weeklyHits),
) {
    init {
        observeCart().onEach { setState { copy(summary = it) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: CartContract.Intent) {
        when (intent) {
            is CartContract.Intent.Add -> addToCart(intent.productId)
            is CartContract.Intent.ChangeQuantity -> changeQuantity(intent.productId, intent.quantity)
        }
    }
}

@Composable
internal fun CartRoute(navigator: AppNavigator) {
    val viewModel = koinViewModel<CartViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CartScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onOpenProduct = { navigator.push(Destinations.product(it)) },
        onCheckout = { navigator.push(CartRoutes.CHECKOUT) },
        onShop = { navigator.open(Destinations.HOME) },
    )
}

@Composable
fun CartScreen(
    state: CartContract.State,
    onIntent: (CartContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    onCheckout: () -> Unit,
    onShop: () -> Unit,
) {
    val padding = windowSize.contentPadding
    val summary = state.summary
    Column(Modifier.fillMaxSize().background(FreshColors.Canvas)) {
        LazyColumn(Modifier.weight(1f)) {
            item(key = "header") { ShopHeader(state.address) }
            if (summary.items.isEmpty()) {
                item(key = "empty") { EmptyCart(onShop) }
                recommended(state, onIntent, onOpenProduct, padding)
            } else {
                item(key = "items") {
                    ShopSection(onDark = true) {
                        SectionTitle("Корзина", Modifier.padding(horizontal = padding))
                        summary.items.forEachIndexed { index, item ->
                            if (index > 0) HorizontalDivider(Modifier.padding(horizontal = padding), color = FreshColors.Line)
                            CartRow(item, onIntent, onOpenProduct, Modifier.padding(horizontal = padding))
                        }
                    }
                }
                item(key = "summary") {
                    ShopSection { SummaryCard(summary, Modifier.padding(horizontal = padding)) }
                }
            }
        }
        if (summary.items.isNotEmpty()) {
            Button(
                onClick = onCheckout,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().background(FreshColors.White)
                    .padding(horizontal = padding, vertical = 10.dp).height(54.dp),
            ) {
                Text("Перейти к оформлению  ·  ${summary.total.rub()}")
            }
        }
    }
}

@Composable
private fun EmptyCart(onShop: () -> Unit) {
    ShopSection(onDark = true) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🧺", fontSize = 110.sp)
            Text(
                "Здесь пока пусто",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                "Добавьте товары, чтобы оформить заказ",
                style = MaterialTheme.typography.bodyLarge,
                color = FreshColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Button(
                onClick = onShop,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 28.dp),
                modifier = Modifier.padding(top = 22.dp).height(52.dp),
            ) {
                Text("Начать покупки")
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.recommended(
    state: CartContract.State,
    onIntent: (CartContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    padding: androidx.compose.ui.unit.Dp,
) {
    item(key = "recommended-title") {
        SectionTitle(
            "Рекомендуем",
            Modifier.sectionPart(top = true, bottom = false).padding(start = padding, end = padding, top = 20.dp, bottom = 14.dp),
        )
    }
    // Колонки — 2, как на телефоне: LazyListScope не видит размер окна.
    val rows = state.recommended.chunked(2)
    itemsIndexed(rows, key = { _, row -> "rec-" + row.first().id }) { index, row ->
        val last = index == rows.lastIndex
        Row(
            Modifier.sectionPart(top = false, bottom = last)
                .padding(start = padding, end = padding, bottom = if (last) 16.dp else 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { product ->
                ProductCard(
                    name = product.name,
                    unit = product.unit,
                    price = product.price.rub(),
                    emoji = product.emoji,
                    quantity = state.summary.quantityOf(product.id),
                    onClick = { onOpenProduct(product.id) },
                    onAdd = { onIntent(CartContract.Intent.Add(product.id)) },
                    onQuantityChange = { onIntent(CartContract.Intent.ChangeQuantity(product.id, it)) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun CartRow(
    item: CartItem,
    onIntent: (CartContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
    modifier: Modifier,
) {
    val product = item.product
    Row(modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        ProductImage(
            product.emoji,
            Modifier.size(76.dp).clickable { onOpenProduct(product.id) },
            emojiSize = 40.sp,
            shape = RoundedCornerShape(18.dp),
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${product.unit} · ${product.price.rub()}",
                style = MaterialTheme.typography.bodyMedium,
                color = FreshColors.Muted,
            )
            if (item.source == CartSource.REPLENISHMENT) {
                Pill("Из «Пора пополнить»", FreshColors.TealSoft, FreshColors.TealText, Modifier.padding(top = 4.dp))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.total.rub(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            QuantityStepper(
                item.quantity,
                { onIntent(CartContract.Intent.ChangeQuantity(product.id, it)) },
                Modifier.padding(top = 6.dp).width(108.dp),
                height = 36.dp,
            )
        }
    }
}

@Composable
internal fun SummaryCard(summary: CartSummary, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().widthIn(max = 640.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryLine("Товары, ${summary.count} шт.", summary.itemsTotal.rub())
        SummaryLine("Доставка", if (summary.deliveryFee == 0) "Бесплатно" else summary.deliveryFee.rub())
        if (summary.deliveryFee > 0) {
            Text(
                "Бесплатная доставка от ${FREE_DELIVERY_FROM.rub()}",
                style = MaterialTheme.typography.bodyMedium,
                color = FreshColors.TealText,
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Итого", style = MaterialTheme.typography.titleLarge)
            Text(summary.total.rub(), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SummaryLine(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = FreshColors.Muted)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
