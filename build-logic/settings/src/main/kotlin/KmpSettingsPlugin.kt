import ru.kaed.ozonfresh.buildlogic.FeatureLayout
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

/** Подключает все фичи из app/feature: новая папка с build.gradle.kts становится модулем без правки settings. */
class KmpSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        FeatureLayout.discover(settings.rootDir).forEach { settings.include(it.projectPath) }
    }
}
