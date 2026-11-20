package ru.kaed.ozonfresh.di

import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Граф приложения собирается из модулей, найденных при сборке: проверяем, что фичи действительно в нём. */
class KoinGraphTest {
    private val koin = koinApplication { modules(appModules) }.koin
    private val plugins = koin.getAll<FeaturePlugin>()

    @Test
    fun everyScreenRegistersOnce() {
        val routes = plugins.flatMap { it.routes }
        assertEquals(routes.distinct(), routes, "маршрут объявлен в двух плагинах")
        assertTrue(Destinations.HOME in routes, "нет стартового экрана")
    }

    @Test
    fun everySectionHasScreen() {
        val routes = plugins.flatMap { it.routes }
        plugins.forEach { plugin ->
            assertTrue(plugin.section in routes, "раздел ${plugin.section} без экрана")
        }
    }
}
