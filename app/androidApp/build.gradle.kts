import ru.kaed.ozonfresh.buildlogic.Constants
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// Ключ релизной подписи: файл keystore.properties в корне (в git не попадает)
// или переменные окружения KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD (для CI).
val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingValue(key: String, env: String): String? =
    keystoreProps.getProperty(key) ?: providers.environmentVariable(env).orNull

val releaseStoreFile = signingValue("storeFile", "KEYSTORE_FILE")

// Адрес API для Android-приложения. Локальный бэкенд из эмулятора: -PapiBaseUrl=http://10.0.2.2:8080/
val apiBaseUrl = providers.gradleProperty("apiBaseUrl").orElse("https://example.com/").get()

android {
    namespace = "${Constants.BASE_PACKAGE}.android"
    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        applicationId = Constants.APPLICATION_ID
        minSdk = Constants.MIN_SDK
        targetSdk = Constants.TARGET_SDK
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    // Встроенный в AGP 9 Kotlin берёт jvmTarget отсюда же.
    compileOptions {
        sourceCompatibility = Constants.JAVA_VERSION
        targetCompatibility = Constants.JAVA_VERSION
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Без ключа релиз подписывается debug-ключом: APK ставится, но в магазин такой не загрузить.
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1,INDEX.LIST,io.netty.versions.properties}"
    }
}

dependencies {
    implementation(project(Constants.Modules.SHARED))
    // AndroidPlatform.init в MainActivity.
    implementation(project(Constants.Modules.DATA))
    implementation(libs.androidx.activityCompose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.navigation.compose)
}
