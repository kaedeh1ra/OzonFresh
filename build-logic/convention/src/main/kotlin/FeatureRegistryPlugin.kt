import com.example.app.buildlogic.Constants
import com.example.app.buildlogic.FeatureLayout
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Сборка приложения из найденных фич: зависимость на каждую и сгенерированный список их Koin-модулей.
 * Koin сам модули не находит, а рефлексии в Wasm нет, поэтому список генерируется при сборке.
 */
class FeatureRegistryPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        val features = FeatureLayout.discover(rootDir)
        val generate = tasks.register<GenerateFeatureRegistryTask>("generateFeatureRegistry") {
            modules.set(features.map { it.koinModule })
            outputDir.set(layout.buildDirectory.dir("generated/kmp/featureRegistry/kotlin"))
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain") {
                    kotlin.srcDir(generate.flatMap { it.outputDir })
                    dependencies {
                        features.forEach { implementation(project(it.projectPath)) }
                    }
                }
            }
        }
    }
}

/** Пакет, где оболочка (:app:shared) ждёт `featureModules`. */
private val REGISTRY_PACKAGE = "${Constants.BASE_PACKAGE}.di"

@CacheableTask
abstract class GenerateFeatureRegistryTask : DefaultTask() {
    @get:Input
    abstract val modules: ListProperty<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "build"
        description = "Генерирует список Koin-модулей всех фич из ${FeatureLayout.DIR}"
    }

    @TaskAction
    fun generate() {
        val root = outputDir.get().asFile
        root.deleteRecursively()
        val file = root.resolve("${REGISTRY_PACKAGE.replace('.', '/')}/FeatureRegistry.kt")
        file.parentFile.mkdirs()
        val entries = modules.get().joinToString("") { "    $it,\n" }
        file.writeText(
            """
            |// Сгенерировано задачей generateFeatureRegistry по папкам ${FeatureLayout.DIR}/*, руками не править.
            |// Фича попадает сюда сама, если объявляет `val <имя>FeatureModule` в пакете ${FeatureLayout.BASE_PACKAGE}.<имя>.
            |package $REGISTRY_PACKAGE
            |
            |import org.koin.core.module.Module
            |
            |/** Koin-модули всех фич, найденных при сборке. */
            |internal val featureModules: List<Module> = listOf(
            |$entries)
            |""".trimMargin(),
        )
    }
}
