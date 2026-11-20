import ru.kaed.ozonfresh.buildlogic.Constants
import ru.kaed.ozonfresh.buildlogic.library
import ru.kaed.ozonfresh.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Фича: экран(ы) со своей ViewModel и Koin-модулем. Видит только core-модули, но не другие фичи:
 * всё общее между фичами — контракты в :app:core:navigation, реализацию подставляет Koin.
 */
class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("kmp.compose")
        val catalog = libs

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.getByName("commonMain").dependencies {
                implementation(project(Constants.Modules.DOMAIN))
                implementation(project(Constants.Modules.MVI))
                implementation(project(Constants.Modules.DESIGN_SYSTEM))
                implementation(project(Constants.Modules.NAVIGATION))
                implementation(catalog.library("navigation-compose"))
                implementation(catalog.library("koin-core"))
                implementation(catalog.library("koin-compose"))
                implementation(catalog.library("koin-composeViewmodel"))
            }
            // Тестам ViewModel нужны фейки репозиториев и настоящие реализации хранилища сессии.
            sourceSets.getByName("commonTest").dependencies {
                implementation(project(Constants.Modules.TESTING))
                implementation(project(Constants.Modules.DATA))
            }
        }
    }
}
