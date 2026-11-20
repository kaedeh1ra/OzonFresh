package ru.kaed.ozonfresh.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.CartSource
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.CartItem
import ru.kaed.ozonfresh.domain.usecase.shop.CartSummary
import ru.kaed.ozonfresh.domain.usecase.shop.ChangeCartQuantityUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.FREE_DELIVERY_FROM
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.presentation.designsystem.components.MessageState
import ru.kaed.ozonfresh.presentation.designsystem.components.Pill
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductImage
import ru.kaed.ozonfresh.presentation.designsystem.components.QuantityStepper
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

object CartContract {
    data class State(val summary: CartSummary = CartSummary(emptyList()))

    sealed interface Intent {
        data class ChangeQuantity(val productId: String, val quantity: Int) : Intent
    }
}

class CartViewModel(
    observeCart: ObserveCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<CartContract.State, CartContract.Intent, Nothing>(CartContract.State()) {
    init {
        observeCart().onEach { setState { copy(summary = it) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: CartContract.Intent) {
        when (intent) {
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
    Column(Modifier.fillMaxSize()) {
        Text(
            if (summary.count > 0) "Корзина · ${summary.count} шт." else "Корзина",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = padding, end = padding, top = 16.dp, bottom = 8.dp),
        )
        if (summary.items.isEmpty()) {
            MessageState(
                title = "В корзине пусто",
                description = "Загляните в «Пора пополнить» на главной: там то, что обычно заканчивается",
                actionText = "На главную",
                onAction = onShop,
            )
            return
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = padding, vertical = 8.dp)) {
            items(summary.items, key = { it.product.id }) { item ->
                CartRow(item, onIntent, onOpenProduct)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                SummaryCard(summary, Modifier.padding(top = 16.dp))
            }
        }
        Button(
            onClick = onCheckout,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().padding(horizontal = padding, vertical = 12.dp).height(52.dp),
        ) {
            Text("Перейти к оформлению · ${summary.total.rub()}")
        }
    }
}

@Composable
private fun CartRow(item: CartItem, onIntent: (CartContract.Intent) -> Unit, onOpenProduct: (String) -> Unit) {
    val product = item.product
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductImage(
            product.emoji,
            product.tint,
            Modifier.size(64.dp).clickable { onOpenProduct(product.id) },
            emojiSize = 30.sp,
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${product.unit} · ${product.price.rub()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (item.source == CartSource.REPLENISHMENT) {
                Pill(
                    "Из «Пора пополнить»",
                    FreshColors.TealSoft,
                    FreshColors.TealDark,
                    Modifier.padding(top = 4.dp),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(item.total.rub(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            QuantityStepper(
                item.quantity,
                { onIntent(CartContract.Intent.ChangeQuantity(product.id, it)) },
                Modifier.padding(top = 6.dp).width(104.dp),
                height = 32.dp,
            )
        }
    }
}

@Composable
internal fun SummaryCard(summary: CartSummary, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SummaryLine("Товары, ${summary.count} шт.", summary.itemsTotal.rub())
        SummaryLine("Доставка", if (summary.deliveryFee == 0) "Бесплатно" else summary.deliveryFee.rub())
        if (summary.deliveryFee > 0) {
            Text(
                "Бесплатная доставка от ${FREE_DELIVERY_FROM.rub()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Итого", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(summary.total.rub(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryLine(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
