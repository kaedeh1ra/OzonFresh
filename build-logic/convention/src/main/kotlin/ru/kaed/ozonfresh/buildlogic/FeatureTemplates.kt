package ru.kaed.ozonfresh.buildlogic

/** Шаблоны файлов новой фичи. Код в них компилируется как есть: сгенерированная фича сразу собирается и проходит тест. */
object FeatureTemplates {

    sealed interface Entry {
        data object None : Entry
        data class Tab(val label: String) : Entry
        data class Footer(val label: String) : Entry
    }

    fun files(name: FeatureName, entry: Entry): Map<String, String> {
        val src = "src/commonMain/kotlin/${name.packageName.replace('.', '/')}"
        val test = "src/commonTest/kotlin/${name.packageName.replace('.', '/')}"
        return mapOf(
            "build.gradle.kts" to buildGradle(),
            "$src/${name.pascal}Feature.kt" to feature(name, entry),
            "$src/${name.pascal}Contract.kt" to contract(name),
            "$src/${name.pascal}ViewModel.kt" to viewModel(name),
            "$src/${name.pascal}Screen.kt" to screen(name),
            "$test/${name.pascal}ViewModelTest.kt" to viewModelTest(name),
        )
    }

    private fun buildGradle() = """
        |plugins {
        |    id("kmp.feature")
        |}
        |
        |// Зависимости на core-модули, Koin и навигацию добавляет плагин kmp.feature.
        |// Свои библиотеки фичи — здесь, в kotlin { sourceSets { commonMain.dependencies { ... } } }.
        |""".trimMargin()

    private fun feature(name: FeatureName, entry: Entry): String {
        val entryLine = when (entry) {
            Entry.None -> "    // Ссылки в оболочке нет: экран открывают из других разделов через host.navigator.push(${name.pascal}Routes.MAIN)."
            is Entry.Tab -> "    override val entry = FeatureEntry.Tab(\"${entry.label}\", NavIcon.Home, order = 100)"
            is Entry.Footer -> "    override val entry = FeatureEntry.Footer(\"${entry.label}\", order = 100)"
        }
        // С отступом и «|»: строка проходит через trimMargin вместе с остальным шаблоном.
        val navIconImport =
            if (entry is Entry.Tab) "|import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon\n" else ""
        val entryImport =
            if (entry == Entry.None) "" else "import ru.kaed.ozonfresh.navigation.FeatureEntry\n"
        return """
            |package ${name.packageName}
            |
            |import androidx.navigation.NavGraphBuilder
            |import org.koin.core.module.dsl.singleOf
            |import org.koin.core.module.dsl.viewModelOf
            |import org.koin.dsl.bind
            |import org.koin.dsl.module
            |${entryImport}import ru.kaed.ozonfresh.navigation.FeatureHost
            |import ru.kaed.ozonfresh.navigation.FeaturePlugin
            |import ru.kaed.ozonfresh.navigation.screen
            ${navIconImport}|
            |/** Маршруты раздела. Если на экран понадобится ссылаться из другой фичи, маршрут переезжает в Destinations. */
            |internal object ${name.pascal}Routes {
            |    const val MAIN = "${name.raw}"
            |}
            |
            |internal class ${name.pascal}Plugin : FeaturePlugin {
            |    override val routes = listOf(${name.pascal}Routes.MAIN)
            |$entryLine
            |
            |    override fun NavGraphBuilder.register(host: FeatureHost) {
            |        screen(${name.pascal}Routes.MAIN) { ${name.pascal}Route() }
            |    }
            |}
            |
            |/** Приложение подключает модуль само: реестр фич ищет `${name.camel}FeatureModule` в этом пакете. */
            |val ${name.camel}FeatureModule = module {
            |    viewModelOf(::${name.pascal}ViewModel)
            |    singleOf(::${name.pascal}Plugin) bind FeaturePlugin::class
            |}
            |""".trimMargin()
    }

    private fun contract(name: FeatureName) = """
        |package ${name.packageName}
        |
        |object ${name.pascal}Contract {
        |    data class State(
        |        val title: String = "${name.pascal}",
        |    )
        |
        |    sealed interface Intent {
        |        data object Refresh : Intent
        |    }
        |
        |    sealed interface Effect
        |}
        |""".trimMargin()

    private fun viewModel(name: FeatureName) = """
        |package ${name.packageName}
        |
        |import ${name.packageName}.${name.pascal}Contract.Effect
        |import ${name.packageName}.${name.pascal}Contract.Intent
        |import ${name.packageName}.${name.pascal}Contract.State
        |import ru.kaed.ozonfresh.presentation.mvi.MviViewModel
        |
        |/** Зависимости (use case'ы из :app:core:domain) приходят в конструктор: Koin подставит их сам. */
        |class ${name.pascal}ViewModel : MviViewModel<State, Intent, Effect>(State()) {
        |
        |    override fun onIntent(intent: Intent) {
        |        when (intent) {
        |            Intent.Refresh -> Unit
        |        }
        |    }
        |}
        |""".trimMargin()

    private fun screen(name: FeatureName) = """
        |package ${name.packageName}
        |
        |import androidx.compose.foundation.layout.fillMaxSize
        |import androidx.compose.foundation.layout.padding
        |import androidx.compose.runtime.Composable
        |import androidx.compose.runtime.getValue
        |import androidx.compose.ui.Modifier
        |import androidx.lifecycle.compose.collectAsStateWithLifecycle
        |import org.koin.compose.viewmodel.koinViewModel
        |import ${name.packageName}.${name.pascal}Contract.Intent
        |import ${name.packageName}.${name.pascal}Contract.State
        |import ru.kaed.ozonfresh.presentation.designsystem.components.AppCard
        |import ru.kaed.ozonfresh.presentation.designsystem.components.MessageState
        |import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
        |import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
        |
        |@Composable
        |internal fun ${name.pascal}Route() {
        |    val viewModel = koinViewModel<${name.pascal}ViewModel>()
        |    val state by viewModel.state.collectAsStateWithLifecycle()
        |    ${name.pascal}Screen(state, viewModel::onIntent)
        |}
        |
        |@Composable
        |fun ${name.pascal}Screen(state: State, onIntent: (Intent) -> Unit) {
        |    AppCard(Modifier.padding(windowSize.contentPadding)) {
        |        MessageState(
        |            title = state.title,
        |            description = "Экран создан генератором: опишите состояние в ${name.pascal}Contract и нарисуйте его здесь.",
        |            actionText = "Обновить",
        |            onAction = { onIntent(Intent.Refresh) },
        |            modifier = Modifier.fillMaxSize(),
        |        )
        |    }
        |}
        |""".trimMargin()

    private fun viewModelTest(name: FeatureName) = """
        |package ${name.packageName}
        |
        |import kotlin.test.Test
        |import kotlin.test.assertEquals
        |
        |class ${name.pascal}ViewModelTest {
        |
        |    @Test
        |    fun startsWithDefaultState() {
        |        assertEquals(${name.pascal}Contract.State(), ${name.pascal}ViewModel().state.value)
        |    }
        |}
        |""".trimMargin()
}
