import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.example.app.buildlogic.Constants
import com.example.app.buildlogic.androidNamespace
import com.example.app.buildlogic.library
import com.example.app.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Любой модуль приложения: Kotlin Multiplatform под Android, iOS, desktop (JVM) и браузер (Wasm).
 * Тесты на JVM: `testAndroidHostTest` и `jvmTest`. iOS-таргеты собираются только на macOS, на других ОС Gradle их пропускает.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")
        val catalog = libs

        extensions.configure<KotlinMultiplatformExtension> {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                namespace = androidNamespace
                compileSdk = Constants.COMPILE_SDK
                minSdk = Constants.MIN_SDK
                compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Constants.JVM_TARGET)) }
                withHostTest {}
            }

            // Desktop: Windows, Linux и macOS через Compose for Desktop.
            jvm {
                compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Constants.JVM_TARGET)) }
            }

            // Устройство (arm64) и симулятор на Apple Silicon. Симулятор на Intel-маках (iosX64) не включён.
            iosArm64()
            iosSimulatorArm64()

            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                browser()
            }

            sourceSets.getByName("commonMain").dependencies {
                implementation(catalog.library("kotlinx-coroutinesCore"))
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(catalog.library("kotlin-test"))
                implementation(catalog.library("kotlinx-coroutinesTest"))
            }
        }
    }
}
