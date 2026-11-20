import com.example.app.buildlogic.Constants
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("kmp.compose")
    // Зависимости на все app/feature/* и сгенерированный список их Koin-модулей.
    id("kmp.feature.registry")
}

// Оболочка приложения: каркас страницы, граф навигации из плагинов фич, запуск Koin.
// Для iOS модуль собирается во фреймворк Shared, который подключает Xcode-проект iosApp.
kotlin {
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(Constants.Modules.DOMAIN))
            implementation(project(Constants.Modules.DATA))
            implementation(project(Constants.Modules.MVI))
            implementation(project(Constants.Modules.DESIGN_SYSTEM))
            implementation(project(Constants.Modules.NAVIGATION))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
        }
        commonTest.dependencies {
            implementation(project(Constants.Modules.TESTING))
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.koin.test)
            implementation(libs.ktor.clientCore)
        }
    }
}
