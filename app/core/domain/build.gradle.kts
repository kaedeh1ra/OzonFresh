plugins {
    id("kmp.library")
}

// Чистый Kotlin: модели, интерфейсы репозиториев и use case'ы. Ни Compose, ни сети.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
        }
    }
}
