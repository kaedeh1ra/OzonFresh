import com.example.app.buildlogic.library
import com.example.app.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Модуль с Compose UI: [KmpLibraryConventionPlugin] плюс Compose Multiplatform и lifecycle для ViewModel. */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        val catalog = libs

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                listOf(
                    "compose-runtime",
                    "compose-foundation",
                    "compose-material3",
                    "compose-ui",
                    "compose-uiToolingPreview",
                    "androidx-lifecycle-viewmodelCompose",
                    "androidx-lifecycle-runtimeCompose",
                ).forEach { implementation(catalog.library(it)) }
            }
        }
    }
}
