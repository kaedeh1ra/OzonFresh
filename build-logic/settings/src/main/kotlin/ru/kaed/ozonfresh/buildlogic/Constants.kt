package ru.kaed.ozonfresh.buildlogic

import org.gradle.api.JavaVersion

/**
 * Настройки сборки для всех модулей, видны в любом build.gradle.kts: `import ru.kaed.ozonfresh.buildlogic.Constants`.
 * Версии библиотек — в gradle/libs.versions.toml.
 */
object Constants {
    /** Корневой пакет, Android namespace и applicationId. Меняется скриптом rename (см. README). */
    const val BASE_PACKAGE = "ru.kaed.ozonfresh"
    const val APPLICATION_ID = BASE_PACKAGE

    /**
     * Байткод всех модулей. JDK самой сборки задаётся отдельно, меняйте вместе:
     * java-version в .github/workflows/ci.yml и gradle/gradle-daemon-jvm.properties.
     */
    const val JVM_VERSION = 21
    const val JVM_TARGET = "$JVM_VERSION"
    val JAVA_VERSION: JavaVersion = JavaVersion.toVersion(JVM_VERSION)

    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 31

    /** Пути модулей: фичи подключаются сами (см. FeatureLayout), здесь — постоянные модули. */
    object Modules {
        const val SHARED = ":app:shared"
        const val DOMAIN = ":app:core:domain"
        const val DATA = ":app:core:data"
        const val MVI = ":app:core:mvi"
        const val DESIGN_SYSTEM = ":app:core:designsystem"
        const val NAVIGATION = ":app:core:navigation"
        const val TESTING = ":app:core:testing"
    }
}
