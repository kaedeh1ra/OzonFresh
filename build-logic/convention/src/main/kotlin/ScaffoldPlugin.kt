import com.example.app.buildlogic.FeatureLayout
import com.example.app.buildlogic.FeatureName
import com.example.app.buildlogic.FeatureTemplates
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.register

class ScaffoldPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register<NewFeatureTask>("newFeature") {
            rootDirectory.set(target.layout.projectDirectory)
        }
    }
}

/**
 * Создаёт модуль фичи в app/feature/<name> по шаблону. Файлы вне новой папки не трогает:
 * settings и приложение подхватят фичу сами при следующей сборке.
 *
 *   ./gradlew newFeature --name=settings
 *   ./gradlew newFeature --name=settings --tab="Настройки"      вкладка в навигации
 *   ./gradlew newFeature --name=settings --footer="Настройки"   ссылка в подвале и в меню
 */
abstract class NewFeatureTask : DefaultTask() {
    @get:Input
    @get:Option(option = "name", description = "Имя фичи в kebab-case, например files-check")
    abstract val featureName: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "tab", description = "Подпись вкладки в навигации")
    abstract val tabLabel: Property<String>

    @get:Input
    @get:Optional
    @get:Option(option = "footer", description = "Подпись ссылки в подвале")
    abstract val footerLabel: Property<String>

    @get:Internal
    abstract val rootDirectory: DirectoryProperty

    init {
        group = "scaffolding"
        description =
            "Создаёт модуль фичи в ${FeatureLayout.DIR}: MVI-экран, Koin-модуль и плагин навигации"
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun create() {
        val raw = featureName.orNull?.trim().orEmpty()
        if (raw.isEmpty()) throw GradleException("Укажите имя: ./gradlew newFeature --name=settings")
        val name =
            runCatching { FeatureName(raw) }.getOrElse { throw GradleException(it.message.orEmpty()) }
        if (tabLabel.isPresent && footerLabel.isPresent) {
            throw GradleException("Выберите что-то одно: --tab или --footer")
        }

        val moduleDir = rootDirectory.get().asFile.resolve("${FeatureLayout.DIR}/${name.raw}")
        if (moduleDir.exists()) throw GradleException("Папка ${moduleDir.relativeTo(rootDirectory.get().asFile)} уже есть")

        val entry = when {
            tabLabel.isPresent -> FeatureTemplates.Entry.Tab(tabLabel.get())
            footerLabel.isPresent -> FeatureTemplates.Entry.Footer(footerLabel.get())
            else -> FeatureTemplates.Entry.None
        }
        FeatureTemplates.files(name, entry).forEach { (path, content) ->
            val file = moduleDir.resolve(path)
            file.parentFile.mkdirs()
            file.writeText(content)
        }
        logger.lifecycle(
            "Фича ${name.projectPath} создана в ${FeatureLayout.DIR}/${name.raw}. " +
                    "Синхронизируйте Gradle: модуль подключится сам, экран откроется по маршруту \"${name.raw}\".",
        )
    }
}
