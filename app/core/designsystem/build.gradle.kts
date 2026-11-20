plugins {
    id("kmp.compose")
}

// Тема и компоненты интерфейса, общие для всех фич. Фото товаров — ресурсы Compose (composeResources/drawable).
kotlin {
    // Модуль Android-библиотеки сам ресурсы не пакует: без этого фото не попадут в APK.
    android {
        androidResources.enable = true
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "ru.kaed.ozonfresh.presentation.designsystem.resources"
}
