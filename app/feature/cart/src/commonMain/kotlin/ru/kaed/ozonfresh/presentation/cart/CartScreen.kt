package ru.kaed.ozonfresh.presentation.cart

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
import androidx.compose.ui.text.TextStyle
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
import ru.kaed.ozonfresh.domain.model.discountPercent
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
import ru.kaed.ozonfresh.presentation.designsystem.components.CarouselPriceStyle
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_EDGE
import ru.kaed.ozonfresh.presentation.designsystem.components.GRID_GAP
import ru.kaed.ozonfresh.presentation.designsystem.components.Pill
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductCard
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductImage
import ru.kaed.ozonfresh.presentation.designsystem.components.QuantityStepper
import ru.kaed.ozonfresh.presentation.designsystem.components.READABLE_WIDTH
import ru.kaed.ozonfresh.presentation.designsystem.components.SECTION_GAP
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.SectionTitle
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.WindowSize
import ru.kaed.ozonfresh.presentation.designsystem.components.productColumns
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
    val padding = ScreenPadding
    val summary = state.summary
    val columns = productColumns
    // На широком экране итог стоит справа от списка, а не под ним.
    val twoPane = windowSize != WindowSize.Compact
    Column(Modifier.fillMaxSize().background(FreshColors.Canvas)) {
        LazyColumn(Modifier.weight(1f)) {
            item(key = "header") {
                // У корзины белый лист начинается на 8 dp ниже, чем на главной.
                Column(Modifier.background(FreshColors.Dark)) {
                    ShopHeader(state.address)
                    Spacer(Modifier.height(8.dp))
                }
            }
            if (summary.items.isEmpty()) {
                item(key = "empty") { EmptyCart(onShop) }
                recommended(state, columns, onIntent, onOpenProduct)
            } else if (twoPane) {
                item(key = "items") {
                    Row(Modifier.background(FreshColors.Canvas)) {
                        ShopSection(Modifier.weight(1f), onDark = true) { CartItems(summary, onIntent, onOpenProduct) }
                        ShopSection(Modifier.width(360.dp).padding(start = SECTION_GAP), onDark = true) {
                            SummaryCard(summary, Modifier.padding(horizontal = padding))
                        }
                    }
                }
            } else {
                item(key = "items") {
                    ShopSection(onDark = true) { CartItems(summary, onIntent, onOpenProduct) }
                }
                item(key = "summary") {
                    ShopSection { SummaryCard(summary, Modifier.padding(horizontal = padding)) }
                }
            }
        }
        if (summary.items.isNotEmpty()) {
            Box(Modifier.fillMaxWidth().background(FreshColors.Surface), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onCheckout,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.widthIn(max = READABLE_WIDTH).fillMaxWidth()
                        .padding(horizontal = padding, vertical = 8.dp).height(48.dp),
                ) {
                    Text("Перейти к оформлению  ·  ${summary.total.rub()}", style = ButtonTextStyle)
                }
            }
        }
    }
}

@Composable
private fun CartItems(summary: CartSummary, onIntent: (CartContract.Intent) -> Unit, onOpenProduct: (String) -> Unit) {
    SectionTitle("Корзина", Modifier.padding(horizontal = ScreenPadding))
    summary.items.forEachIndexed { index, item ->
        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = ScreenPadding), color = FreshColors.Line)
        CartRow(item, onIntent, onOpenProduct, Modifier.padding(horizontal = ScreenPadding))
    }
}

/** Текст основных кнопок: 16 sp, полужирный, как «Начать покупки» в приложении. */
internal val ButtonTextStyle = TextStyle(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)

/** Пустая корзина по скриншоту: картинка ~120 dp, заголовок 25 sp, подпись 16 sp, кнопка 155 × 44 dp. */
@Composable
private fun EmptyCart(onShop: () -> Unit) {
    ShopSection(onDark = true, contentPadding = 24.dp, bottomPadding = 14.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.height(122.dp), contentAlignment = Alignment.Center) { Text("🧺", fontSize = 100.sp) }
            Text(
                "Здесь пока пусто",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 21.dp),
            )
            Text(
                "Добавьте товары, чтобы оформить заказ",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
                color = FreshColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onShop,
                shape = RoundedCornerShape(15.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(top = 14.dp).height(44.dp).widthIn(min = 155.dp),
            ) {
                Text("Начать покупки", style = ButtonTextStyle)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.recommended(
    state: CartContract.State,
    columns: Int,
    onIntent: (CartContract.Intent) -> Unit,
    onOpenProduct: (String) -> Unit,
) {
    item(key = "recommended-title") {
        SectionTitle(
            "Рекомендуем",
            Modifier.sectionPart(top = true, bottom = false)
                .padding(start = ScreenPadding, end = ScreenPadding, top = 24.dp, bottom = 9.dp),
        )
    }
    val rows = state.recommended.chunked(columns)
    itemsIndexed(rows, key = { _, row -> "rec-" + row.first().id }) { index, row ->
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
                    quantity = state.summary.quantityOf(product.id),
                    onClick = { onOpenProduct(product.id) },
                    onAdd = { onIntent(CartContract.Intent.Add(product.id)) },
                    onQuantityChange = { onIntent(CartContract.Intent.ChangeQuantity(product.id, it)) },
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
            productId = product.id,
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                product.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 18.5.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
            Text(item.total.rub(), style = CarouselPriceStyle)
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
