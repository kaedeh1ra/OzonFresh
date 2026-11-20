package ru.kaed.ozonfresh.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.kaed.ozonfresh.domain.model.Product
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.AddToCartUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ChangeCartQuantityUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.GetCatalogUseCase
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.screen
import ru.kaed.ozonfresh.presentation.designsystem.components.MessageState
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIconView
import ru.kaed.ozonfresh.presentation.designsystem.components.ProductImage
import ru.kaed.ozonfresh.presentation.designsystem.components.QuantityStepper
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.SheetScreen
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

/** Карточка товара: минимальная, открывается из любого раздела. */
internal class ProductPlugin : FeaturePlugin {
    override val routes = listOf(Destinations.PRODUCT)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.PRODUCT) { entry ->
            ProductRoute(entry.savedStateHandle.get<String>("id").orEmpty(), host.navigator)
        }
    }
}

object ProductContract {
    data class State(val product: Product? = null, val quantity: Int = 0)

    sealed interface Intent {
        data object Add : Intent
        data class ChangeQuantity(val quantity: Int) : Intent
    }
}

class ProductViewModel(
    private val productId: String,
    catalog: GetCatalogUseCase,
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val changeQuantity: ChangeCartQuantityUseCase,
) : MviViewModel<ProductContract.State, ProductContract.Intent, Nothing>(
    ProductContract.State(product = catalog.product(productId)),
) {
    init {
        observeCart().onEach { cart -> setState { copy(quantity = cart.quantityOf(productId)) } }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: ProductContract.Intent) {
        when (intent) {
            ProductContract.Intent.Add -> addToCart(productId)
            is ProductContract.Intent.ChangeQuantity -> changeQuantity(productId, intent.quantity)
        }
    }
}

@Composable
private fun ProductRoute(productId: String, navigator: AppNavigator) {
    val viewModel = koinViewModel<ProductViewModel>(key = "product-$productId") { parametersOf(productId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProductScreen(state, viewModel::onIntent, onBack = navigator::up)
}

/** Карточка товара как шторка в приложении: картинка во всю ширину, крестик поверх, цена и кнопка внизу. */
@Composable
fun ProductScreen(state: ProductContract.State, onIntent: (ProductContract.Intent) -> Unit, onBack: () -> Unit) {
    val product = state.product
    SheetScreen {
        if (product == null) {
            ScreenHeader("Товар", onBack)
            MessageState("Товар не найден", description = "Возможно, его убрали из ассортимента")
            return@SheetScreen
        }
        val padding = windowSize.contentPadding
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box {
                ProductImage(
                    product.emoji,
                    Modifier.fillMaxWidth().aspectRatio(1.15f),
                    emojiSize = 150.sp,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                )
                Row(
                    Modifier.align(Alignment.TopEnd).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavIconView(NavIcon.Heart, FreshColors.NavInactive, size = 24.dp)
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(FreshColors.NavInactive)
                            .clickable(onClickLabel = "Закрыть", onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        NavIconView(NavIcon.Close, FreshColors.White, size = 13.dp)
                    }
                }
            }
            Text(
                "${product.name}, ${product.unit}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = padding + 6.dp, end = padding, top = 16.dp),
            )
            Column(
                Modifier.padding(horizontal = padding).padding(top = 14.dp).fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)).background(FreshColors.Card).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("О товаре", style = MaterialTheme.typography.titleMedium)
                Text(
                    product.description.ifBlank { "Описание появится позже." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = FreshColors.Muted,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = padding + 6.dp, end = padding, top = 10.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(product.price.rub(), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            if (state.quantity == 0) {
                Button(
                    onClick = { onIntent(ProductContract.Intent.Add) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1.2f).height(50.dp),
                ) { Text("В корзину", style = MaterialTheme.typography.titleMedium) }
            } else {
                QuantityStepper(
                    state.quantity,
                    { onIntent(ProductContract.Intent.ChangeQuantity(it)) },
                    Modifier.weight(1.2f),
                    height = 50.dp,
                )
            }
        }
    }
}

/** Приложение подключает модуль само: реестр фич ищет `productFeatureModule` в этом пакете. */
val productFeatureModule = module {
    // Параметр — id товара из маршрута, остальное Koin подставит сам.
    factory { params -> ProductViewModel(params.get(), get(), get(), get(), get()) }
    singleOf(::ProductPlugin) bind FeaturePlugin::class
}
