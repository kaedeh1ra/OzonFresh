pluginManagement {
    // Convention-плагины модулей и подключение фич: см. build-logic/README.md.
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Подключает каждую папку app/feature/<имя> с build.gradle.kts как модуль :app:feature:<имя>.
    id("kmp.settings")
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "KmpTemplate"

include(":app:shared")
include(":app:androidApp")
include(":app:webApp")
include(":app:desktopApp")
include(":app:core:domain")
include(":app:core:data")
include(":app:core:mvi")
include(":app:core:designsystem")
include(":app:core:navigation")
include(":app:core:testing")
