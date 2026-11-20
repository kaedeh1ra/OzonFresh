package ru.kaed.ozonfresh.presentation.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.CartSummary
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveTodayUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.OrderResult
import ru.kaed.ozonfresh.domain.usecase.shop.PlaceOrderUseCase
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.presentation.designsystem.components.READABLE_WIDTH
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.SheetScreen
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.CollectEffects
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

object CheckoutContract {
    val slots = listOf("Через 15–30 минут", "Сегодня, 19:00–20:00", "Завтра, 9:00–10:00")
    val payments = listOf("Картой онлайн", "СБП")

    data class State(
        val address: String = "",
        val summary: CartSummary = CartSummary(emptyList()),
        val slot: Int = 0,
        val payment: Int = 0,
        /** Товары, которые закончились перед оплатой: показываем сообщение. */
        val unavailable: List<Product> = emptyList(),
    )

    sealed interface Intent {
        data class SelectSlot(val index: Int) : Intent
        data class SelectPayment(val index: Int) : Intent
        data object Pay : Intent
        data object DismissUnavailable : Intent
    }

    sealed interface Effect {
        data class OrderPlaced(val orderId: String, val total: Int) : Effect
        data object BackToCart : Effect
    }
}

class CheckoutViewModel(
    today: ObserveTodayUseCase,
    observeCart: ObserveCartUseCase,
    private val placeOrder: PlaceOrderUseCase,
) : MviViewModel<CheckoutContract.State, CheckoutContract.Intent, CheckoutContract.Effect>(
    CheckoutContract.State(address = today.address),
) {
    init {
        observeCart().onEach { setState { copy(summary = it) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: CheckoutContract.Intent) {
        when (intent) {
            is CheckoutContract.Intent.SelectSlot -> setState { copy(slot = intent.index) }
            is CheckoutContract.Intent.SelectPayment -> setState { copy(payment = intent.index) }
            CheckoutContract.Intent.Pay -> when (val result = placeOrder()) {
                is OrderResult.Placed -> sendEffect(CheckoutContract.Effect.OrderPlaced(result.orderId, result.total))
                is OrderResult.ItemsUnavailable -> setState { copy(unavailable = result.products) }
                OrderResult.EmptyCart -> sendEffect(CheckoutContract.Effect.BackToCart)
            }

            CheckoutContract.Intent.DismissUnavailable -> {
                setState { copy(unavailable = emptyList()) }
                if (currentState.summary.items.isEmpty()) sendEffect(CheckoutContract.Effect.BackToCart)
            }
        }
    }
}

@Composable
internal fun CheckoutRoute(navigator: AppNavigator) {
    val viewModel = koinViewModel<CheckoutViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            // Стек забывается: «назад» с экрана «Заказ оформлен» ведёт на главную, а не в пустое оформление.
            is CheckoutContract.Effect.OrderPlaced -> navigator.openClean(CartRoutes.orderDone(effect.orderId, effect.total))
            CheckoutContract.Effect.BackToCart -> navigator.up()
        }
    }
    CheckoutScreen(state, viewModel::onIntent, onBack = navigator::up)
}

@Composable
fun CheckoutScreen(
    state: CheckoutContract.State,
    onIntent: (CheckoutContract.Intent) -> Unit,
    onBack: () -> Unit,
) {
    val padding = ScreenPadding
    SheetScreen {
        ScreenHeader("Оформление заказа", onBack)
        // На широком экране форма не растягивается: колонка по центру.
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Column(
            Modifier.widthIn(max = READABLE_WIDTH).fillMaxWidth().padding(horizontal = padding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Section("Адрес доставки") {
                Text(state.address, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Подъезд 2, этаж 5. Позвонить за 5 минут",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Section("Когда привезти") {
                CheckoutContract.slots.forEachIndexed { index, slot ->
                    Choice(slot, selected = state.slot == index) { onIntent(CheckoutContract.Intent.SelectSlot(index)) }
                }
            }
            Section("Оплата") {
                CheckoutContract.payments.forEachIndexed { index, payment ->
                    Choice(payment, selected = state.payment == index) {
                        onIntent(CheckoutContract.Intent.SelectPayment(index))
                    }
                }
            }
            SummaryCard(state.summary, Modifier.clip(RoundedCornerShape(24.dp)).background(FreshColors.Card).padding(16.dp))
            Text(
                "Прототип: оплата не списывается, заказ сразу считается доставленным.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        }
        Button(
            onClick = { onIntent(CheckoutContract.Intent.Pay) },
            enabled = state.summary.items.isNotEmpty(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = READABLE_WIDTH).fillMaxWidth()
                .padding(horizontal = padding, vertical = 10.dp).height(48.dp),
        ) {
            Text("Оплатить ${state.summary.total.rub()}", style = ButtonTextStyle)
        }
    }

    if (state.unavailable.isNotEmpty()) {
        val names = state.unavailable.joinToString { "«${it.name}»" }
        val next = if (state.summary.items.isEmpty()) {
            "В заказе ничего не осталось, вернём вас в корзину."
        } else {
            "Мы убрали это из заказа и пересчитали сумму. Проверьте заказ и оплатите ещё раз."
        }
        AlertDialog(
            onDismissRequest = { onIntent(CheckoutContract.Intent.DismissUnavailable) },
            title = { Text("Товар закончился") },
            text = {
                Text("Пока вы оформляли заказ, закончилось: $names. $next")
            },
            confirmButton = {
                TextButton({ onIntent(CheckoutContract.Intent.DismissUnavailable) }) { Text("Понятно") }
            },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}

@Composable
private fun Choice(text: String, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, border), RoundedCornerShape(16.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
