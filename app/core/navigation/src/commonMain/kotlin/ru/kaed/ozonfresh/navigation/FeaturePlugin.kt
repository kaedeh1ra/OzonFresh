package ru.kaed.ozonfresh.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon

/**
 * Раздел приложения. Фича кладёт его в свой Koin-модуль (`singleOf(::MyPlugin) bind FeaturePlugin::class`),
 * оболочка собирает все через `getAll()` в граф навигации, вкладки и пункты меню.
 */
interface FeaturePlugin {
    /** Шаблоны маршрутов, которые плагин регистрирует в [register]: по ним оболочка понимает, чей экран открыт. */
    val routes: List<String>

    /** Раздел, который подсвечен в навигации, пока открыт экран плагина. */
    val section: String get() = routes.first()

    /** Ссылка на раздел в оболочке; null — экран открывают только из других разделов. */
    val entry: FeatureEntry? get() = null

    /** Маршруты из [routes], на которых оболочка прячет нижнюю навигацию (оформление заказа и т. п.). */
    val fullScreenRoutes: List<String> get() = emptyList()

    fun NavGraphBuilder.register(host: FeatureHost)
}

/** Где оболочка показывает ссылку на раздел. Ссылка открывает [FeaturePlugin.section]; [order] — место среди соседей. */
sealed interface FeatureEntry {
    val label: String
    val order: Int

    /** Вкладка нижней навигации. */
    data class Tab(override val label: String, val icon: NavIcon, override val order: Int) :
        FeatureEntry

    /** Служебная ссылка: пункт меню в верхней панели. */
    data class Footer(override val label: String, override val order: Int) : FeatureEntry
}

/** То, что оболочка даёт экранам фич. */
interface FeatureHost {
    val navigator: AppNavigator
}

interface AppNavigator {
    /** Открывает раздел. Стек плоский: стартовый экран и не больше одного раздела, состояние разделов сохраняется. */
    fun open(section: String)

    /** Открывает экран поверх текущего. */
    fun push(route: String)

    /** «Назад» с экрана поверх [parent]: если под ним [parent], возвращается, иначе (пришли по прямой ссылке) открывает [parent]. */
    fun back(parent: String)

    /** «Назад» на предыдущий экран; если его нет (открыли по ссылке), — на главную. */
    fun up()

    /** Открывает раздел и забывает текущий стек: после оформления заказа корзина не вернётся на экран «Заказ оформлен». */
    fun openClean(section: String)
}

/**
 * Рамка экрана от оболочки: получает маршрут и сам экран. Оболочка добавляет через неё нижнюю навигацию
 * к вкладкам — навигация становится частью экрана, и шторка поверх раздела закрывает её целиком.
 */
fun interface ScreenFrame {
    @Composable
    fun Frame(route: String, content: @Composable () -> Unit)
}

val LocalScreenFrame = staticCompositionLocalOf { ScreenFrame { _, content -> content() } }

/** Экран графа навигации. Обёртка — единое место для общих настроек экранов (рамка оболочки, анимации). */
fun NavGraphBuilder.screen(
    route: String,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route) { entry ->
        LocalScreenFrame.current.Frame(route) { content(entry) }
    }
}
