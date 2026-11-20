import com.example.app.buildlogic.Constants

plugins {
    id("kmp.library")
    // @Serializable для DTO сетевого API.
    alias(libs.plugins.kotlinSerialization)
}

// Реализации репозиториев: HTTP-клиент, хранилище настроек платформы.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(Constants.Modules.DOMAIN))
            implementation(libs.koin.core)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationJson)
            implementation(libs.kotlinx.serializationJson)
        }
        commonTest.dependencies {
            implementation(project(Constants.Modules.TESTING))
            implementation(libs.ktor.clientMock)
        }
        androidMain.dependencies {
            implementation(libs.ktor.clientOkhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientOkhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }
    }
}
