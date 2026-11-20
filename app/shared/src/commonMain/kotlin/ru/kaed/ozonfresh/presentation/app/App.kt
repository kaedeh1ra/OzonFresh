package ru.kaed.ozonfresh.presentation.app

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureEntry
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.presentation.designsystem.components.LocalWindowSize
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIconView
import ru.kaed.ozonfresh.presentation.designsystem.components.WindowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.AppTheme
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveCartUseCase

private const val NAV_FADE_MS = 150

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
        val cartCount by remember(observeCart) { observeCart.count() }.collectAsState(0)
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route
        // Экран, открытый поверх раздела, подсвечивает вкладку своего раздела.
        val currentPlugin = plugins.firstOrNull { route in it.routes }
        val section = currentPlugin?.section ?: route

        BoxWithConstraints(Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalWindowSize provides WindowSize.forWidth(maxWidth)) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
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
                    bottomBar = {
                        val onTabScreen = currentPlugin?.entry is FeatureEntry.Tab &&
                                route !in currentPlugin.fullScreenRoutes
                        // Одна вкладка — навигация не нужна; появится, когда фича объявит вторую.
                        if (tabs.size > 1 && onTabScreen) {
                            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                                tabs.forEach { (plugin, tab) ->
                                    val selected = section == plugin.section
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigator.open(plugin.section) },
                                        icon = {
                                            val badge = if (plugin.section == Destinations.CART) cartCount else 0
                                            BadgedBox(badge = {
                                                if (badge > 0) Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("$badge") }
                                            }) {
                                                NavIconView(tab.icon, LocalContentColor.current)
                                            }
                                        },
                                        label = { Text(tab.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                ) { padding ->
                    NavHost(
                        navController,
                        startDestination = Destinations.HOME,
                        modifier = Modifier.padding(padding).consumeWindowInsets(padding),
                        // Стандартный кроссфейд длится 700 мс: при частых кликах анимации накладываются «волной».
                        enterTransition = { fadeIn(tween(NAV_FADE_MS)) },
                        exitTransition = { fadeOut(tween(NAV_FADE_MS)) },
                        popEnterTransition = { fadeIn(tween(NAV_FADE_MS)) },
                        popExitTransition = { fadeOut(tween(NAV_FADE_MS)) },
                    ) {
                        plugins.forEach { plugin -> with(plugin) { register(host) } }
                    }
                }
            }
        }
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
