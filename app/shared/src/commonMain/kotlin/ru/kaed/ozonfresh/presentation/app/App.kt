package ru.kaed.ozonfresh.presentation.app

import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.domain.usecase.shop.CartSummary
import ru.kaed.ozonfresh.domain.usecase.shop.DELIVERY_FEE
import ru.kaed.ozonfresh.domain.usecase.shop.FREE_DELIVERY_FROM
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureEntry
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.LocalScreenFrame
import ru.kaed.ozonfresh.navigation.ScreenFrame
import ru.kaed.ozonfresh.presentation.designsystem.components.LocalWindowSize
import ru.kaed.ozonfresh.presentation.designsystem.components.LocalWindowWidth
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIconView
import ru.kaed.ozonfresh.presentation.designsystem.components.READABLE_WIDTH
import ru.kaed.ozonfresh.presentation.designsystem.components.WindowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.AppTheme
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

private const val NAV_FADE_MS = 150
private const val SHEET_MS = 320

/** Раздел под открытой шторкой тускнеет на тёмном фоне оболочки. */
private const val BEHIND_ALPHA = 0.6f

/** Шторка — любой экран, кроме самих вкладок. */
private fun NavBackStackEntry.isSheet(tabSections: Set<String>): Boolean = destination.route !in tabSections

/**
 * Оболочка: верхняя панель, нижняя навигация и граф навигации. Разделы она не знает по именам —
 * берёт все [FeaturePlugin] из Koin, а из их [FeatureEntry] строит вкладки и пункты меню.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(navController: NavHostController = rememberNavController()) {
    AppTheme {
        val koin = getKoin()
        val plugins = remember(koin) { koin.getAll<FeaturePlugin>() }
        val tabs = remember(plugins) { plugins.entries<FeatureEntry.Tab>() }
        val links = remember(plugins) { plugins.entries<FeatureEntry.Footer>() }
        val navigator = remember(navController) { ShellNavigator(navController) }
        val host = remember(navigator) {
            object : FeatureHost {
                override val navigator: AppNavigator = navigator
            }
        }
        val observeCart = koinInject<ObserveCartUseCase>()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route
        val currentPlugin = plugins.firstOrNull { route in it.routes }
        val tabSections = remember(tabs) { tabs.map { it.first.section }.toSet() }

        // Нижняя навигация — часть экранов-вкладок, а не Scaffold: шторка поверх раздела закрывает её целиком,
        // а при жесте «назад» раздел под шторкой уже стоит со своей навигацией и после жеста не перестраивается.
        val frame = remember(tabs, plugins, navigator) {
            ScreenFrame { screenRoute, content ->
                val plugin = plugins.firstOrNull { screenRoute in it.routes }
                val withBars = plugin?.entry is FeatureEntry.Tab && screenRoute == plugin.section &&
                    screenRoute !in plugin.fullScreenRoutes && tabs.size > 1
                if (!withBars) {
                    content()
                } else {
                    val cartNow by remember(observeCart) { observeCart() }.collectAsState(CartSummary(emptyList()))
                    // Панель лежит поверх списка, как в приложении: в её скруглённых углах видно прокручиваемое содержимое.
                    val withDeliveryBar = screenRoute != Destinations.CART
                    BarsOverContent(
                        overlap = withDeliveryBar,
                        content = content,
                        bars = {
                            Column {
                                // Как в приложении: плашки доставки нет на экране самой корзины.
                                if (withDeliveryBar) {
                                    DeliveryBar(cartNow, onClick = { navigator.open(Destinations.CART) })
                                }
                                ShopNavigationBar(
                                    tabs = tabs,
                                    section = plugin.section,
                                    cartCount = cartNow.count,
                                    onOpen = navigator::open,
                                )
                            }
                        },
                    )
                }
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            CompositionLocalProvider(
                LocalWindowSize provides WindowSize.forWidth(maxWidth),
                LocalWindowWidth provides maxWidth,
                LocalScreenFrame provides frame,
            ) {
                Scaffold(
                    // Тёмная полоса под статус-баром, как у референса; экраны рисуют свой фон сами.
                    containerColor = FreshColors.Dark,
                    // Нижний отступ под системную панель дают навигация и шторки.
                    contentWindowInsets = WindowInsets.statusBars,
                    topBar = {
                        // Экраны магазина рисуют заголовки сами; панель нужна только для служебных ссылок.
                        if (links.isNotEmpty()) {
                            TopAppBar(
                                title = { Text(currentPlugin?.entry?.label.orEmpty()) },
                                actions = {
                                    OverflowMenu(links.map { (plugin, link) ->
                                        link.label to { navigator.open(plugin.section) }
                                    })
                                },
                            )
                        }
                    },
                ) { padding ->
                    NavHost(
                        navController,
                        startDestination = Destinations.HOME,
                        modifier = Modifier.padding(padding).consumeWindowInsets(padding),
                        // Вкладки сменяются коротким кроссфейдом. Экраны поверх раздела (товар, оформление, демо-панель)
                        // выезжают шторкой снизу. Раздел под шторкой не двигается и не масштабируется, только темнеет:
                        // иначе при жесте «назад» он сначала ползёт вверх, а потом встаёт на место.
                        enterTransition = {
                            if (targetState.isSheet(tabSections)) {
                                slideInVertically(tween(SHEET_MS)) { it } + fadeIn(tween(SHEET_MS / 2))
                            } else {
                                fadeIn(tween(NAV_FADE_MS))
                            }
                        },
                        exitTransition = {
                            if (targetState.isSheet(tabSections)) {
                                fadeOut(tween(SHEET_MS), targetAlpha = BEHIND_ALPHA)
                            } else {
                                fadeOut(tween(NAV_FADE_MS))
                            }
                        },
                        popEnterTransition = {
                            if (initialState.isSheet(tabSections)) {
                                fadeIn(tween(SHEET_MS), initialAlpha = BEHIND_ALPHA)
                            } else {
                                fadeIn(tween(NAV_FADE_MS))
                            }
                        },
                        popExitTransition = {
                            if (initialState.isSheet(tabSections)) {
                                slideOutVertically(tween(SHEET_MS)) { it }
                            } else {
                                fadeOut(tween(NAV_FADE_MS))
                            }
                        },
                        // Жест «назад» двигает переход вслед за пальцем: шторка уезжает вниз и чуть уменьшается,
                        // раздел под ней проявляется на месте.
                        predictivePopEnterTransition = {
                            if (initialState.isSheet(tabSections)) {
                                fadeIn(initialAlpha = BEHIND_ALPHA)
                            } else {
                                fadeIn()
                            }
                        },
                        predictivePopExitTransition = {
                            if (initialState.isSheet(tabSections)) {
                                // Кубическое ускорение: пока палец тянет, шторка сдвигается немного (как в системном
                                // «назад»), основная часть пути — уже после отпускания.
                                slideOutVertically(tween(SHEET_MS, easing = EaseInCubic)) { it } +
                                    scaleOut(tween(SHEET_MS, easing = EaseOutCubic), targetScale = 0.9f)
                            } else {
                                fadeOut()
                            }
                        },
                    ) {
                        plugins.forEach { plugin -> with(plugin) { register(host) } }
                    }
                }
            }
        }
    }
}

/**
 * Нижняя навигация как в приложении: 56 dp, ниже стандартной Material (80 dp), без подсветки, активная вкладка
 * чёрная. На широком экране вкладки собраны по центру, а не растянуты на всю ширину.
 */
@Composable
private fun ShopNavigationBar(
    tabs: List<Pair<FeaturePlugin, FeatureEntry.Tab>>,
    section: String?,
    cartCount: Int,
    onOpen: (String) -> Unit,
) {
    Box(Modifier.fillMaxWidth().background(FreshColors.White).navigationBarsPadding(), contentAlignment = Alignment.Center) {
        Row(Modifier.widthIn(max = READABLE_WIDTH).fillMaxWidth().height(56.dp)) {
            tabs.forEach { (plugin, tab) ->
                val selected = section == plugin.section
                val color = if (selected) FreshColors.Ink else FreshColors.NavInactive
                Column(
                    Modifier.weight(1f).fillMaxHeight().selectable(selected, role = Role.Tab) { onOpen(plugin.section) }
                        .padding(top = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val badge = if (plugin.section == Destinations.CART) cartCount else 0
                    BadgedBox(badge = { if (badge > 0) Badge(containerColor = FreshColors.Pink) { Text("$badge") } }) {
                        NavIconView(tab.icon, color, size = 23.dp)
                    }
                    Text(
                        tab.label,
                        color = color,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.75.sp, lineHeight = 15.sp),
                        modifier = Modifier.paddingFromBaseline(top = 14.5.dp),
                    )
                }
            }
        }
    }
}

/** Скругление верха плашки доставки: под ним видно содержимое экрана. */
private val BAR_RADIUS = 20.dp

/**
 * Экран и нижняя панель: панель внизу поверх экрана. С плашкой доставки ([overlap]) экран заканчивается под её
 * скруглёнными углами, без неё — ровно над навигацией.
 */
@Composable
private fun BarsOverContent(overlap: Boolean, content: @Composable () -> Unit, bars: @Composable () -> Unit) {
    Layout(
        content = {
            Box(Modifier.background(FreshColors.Canvas)) { content() }
            bars()
        },
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0)
        val barsPlaceable = measurables[1].measure(loose)
        val under = if (overlap) BAR_RADIUS.roundToPx() else 0
        val contentHeight = (constraints.maxHeight - barsPlaceable.height + under).coerceAtLeast(0)
        val contentPlaceable = measurables[0].measure(constraints.copy(minHeight = contentHeight, maxHeight = contentHeight))
        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.place(0, 0)
            barsPlaceable.place(0, constraints.maxHeight - barsPlaceable.height)
        }
    }
}

/** Плашка над навигацией, как «Заказ от 300 ₽ • доставка от +0 ₽ ⌄» в приложении: сумма и условие доставки. */
@Composable
private fun DeliveryBar(cart: CartSummary, onClick: () -> Unit) {
    val (left, right) = when {
        cart.items.isEmpty() -> "Доставка ${DELIVERY_FEE.rub()}" to "бесплатно от ${FREE_DELIVERY_FROM.rub()}"
        cart.deliveryFee > 0 -> "Корзина ${cart.itemsTotal.rub()}" to "ещё ${(FREE_DELIVERY_FROM - cart.itemsTotal).rub()} до бесплатной"
        else -> "Корзина ${cart.itemsTotal.rub()}" to "доставка 0 ₽"
    }
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = BAR_RADIUS, topEnd = BAR_RADIUS))
            .background(FreshColors.White)
            .clickable(onClickLabel = "Открыть корзину", onClick = onClick),
    ) {
        Row(
            Modifier.fillMaxWidth().height(37.dp).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp)
            Text(left, color = FreshColors.Muted, style = style, maxLines = 1)
            Text("  •  ", color = FreshColors.Line, style = style)
            Text(right, color = FreshColors.Muted, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            NavIconView(NavIcon.ChevronDown, FreshColors.Muted, Modifier.padding(start = 4.dp), size = 14.dp)
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(1.5.dp).clip(CircleShape).background(FreshColors.Line))
    }
}

@Composable
private fun OverflowMenu(items: List<Pair<String, () -> Unit>>) {
    var expanded by remember { mutableStateOf(false) }
    IconButton({ expanded = true }) { Text("⋮") }
    DropdownMenu(expanded, { expanded = false }) {
        items.forEach { (label, onClick) ->
            DropdownMenuItem(text = { Text(label) }, onClick = { expanded = false; onClick() })
        }
    }
}

/** Ссылки нужного вида вместе с их плагинами, в порядке [FeatureEntry.order]. */
private inline fun <reified E : FeatureEntry> List<FeaturePlugin>.entries(): List<Pair<FeaturePlugin, E>> =
    mapNotNull { plugin -> (plugin.entry as? E)?.let { plugin to it } }.sortedBy { it.second.order }

/** Навигация по правилам оболочки; фичи видят её как [AppNavigator]. */
private class ShellNavigator(private val navController: NavHostController) : AppNavigator {
    // Стек всегда плоский: стартовый экран + не больше одного раздела. Состояние разделов сохраняется,
    // поэтому повторный переход не пересоздаёт экран и не перезагружает данные.
    override fun open(section: String) {
        if (navController.currentDestination?.route == section) return
        navController.navigate(section) {
            popUpTo(Destinations.HOME) { saveState = true }
            launchSingleTop = true
            // Для стартового экрана восстанавливать нечего: иначе вернётся только что сохранённый раздел.
            restoreState = section != Destinations.HOME
        }
    }

    override fun push(route: String) {
        navController.navigate(route)
    }

    override fun back(parent: String) {
        if (navController.previousBackStackEntry?.destination?.route == parent) {
            navController.popBackStack()
        } else {
            open(parent)
        }
    }

    override fun up() {
        if (navController.previousBackStackEntry != null) navController.popBackStack() else open(Destinations.HOME)
    }

    override fun openClean(section: String) {
        navController.navigate(section) {
            popUpTo(Destinations.HOME) { saveState = false }
            launchSingleTop = true
            restoreState = false
        }
    }
}
