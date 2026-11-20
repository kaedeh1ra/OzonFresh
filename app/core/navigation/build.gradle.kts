import com.example.app.buildlogic.Constants

plugins {
    id("kmp.compose")
}

// Контракты между фичами: плагин раздела, навигатор, общие маршруты.
// Фичи зависят от этого модуля, а не друг от друга; реализации связывает Koin.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(Constants.Modules.DOMAIN))
            api(libs.navigation.compose)
            implementation(project(Constants.Modules.DESIGN_SYSTEM))
        }
    }
}
