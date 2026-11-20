import com.example.app.buildlogic.Constants
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Constants.JVM_TARGET)) }
}

java {
    sourceCompatibility = Constants.JAVA_VERSION
    targetCompatibility = Constants.JAVA_VERSION
}

dependencies {
    implementation(project(Constants.Modules.SHARED))
    // Skia и AWT-окно под ОС, на которой идёт сборка; инсталлятор под другую ОС собирается только на ней.
    implementation(compose.desktop.currentOs)
    // Dispatchers.Main для viewModelScope.
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.navigation.compose)
}

// Адрес API для desktop. Локальный бэкенд: ./gradlew :app:desktopApp:run -PapiBaseUrl=http://localhost:8080/
val apiBaseUrl = providers.gradleProperty("apiBaseUrl").orElse("https://example.com/").get()

compose.desktop {
    application {
        mainClass = "com.example.app.desktop.MainKt"
        jvmArgs += "-Dapp.apiBaseUrl=$apiBaseUrl"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Dmg)
            packageName = rootProject.name
            // MSI и DMG требуют версию вида MAJOR.MINOR.PATCH с MAJOR > 0.
            packageVersion = "1.0.0"
            // java.util.prefs: без модуля jlink выкинет его из встроенной JRE, и настройки не сохранятся.
            modules("java.prefs")

            windows {
                menuGroup = rootProject.name
                // upgradeUuid не задан: задайте свой постоянный UUID, если нужны обновления поверх установленной версии.
            }
            macOS {
                // В bundle ID нельзя подчёркивание, а в пакете Kotlin можно.
                bundleID = Constants.APPLICATION_ID.replace('_', '-')
            }
        }
    }
}
