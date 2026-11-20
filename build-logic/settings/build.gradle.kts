plugins {
    `kotlin-dsl`
}

// Settings-плагин лежит в отдельном jar: Gradle грузит jar settings-плагина в classloader настроек,
// где нет Kotlin и Android Gradle Plugin, и convention-плагины из того же jar там бы не нашли свои типы.
gradlePlugin {
    plugins {
        register("settings") {
            id = "kmp.settings"
            implementationClass = "KmpSettingsPlugin"
        }
    }
}
