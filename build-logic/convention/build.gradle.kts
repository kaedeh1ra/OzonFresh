plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: сами плагины подключает корневой build.gradle.kts (apply false), здесь нужен только их API.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    // FeatureLayout и Constants общие с settings-плагином.
    implementation(project(":settings"))
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("feature") {
            id = "kmp.feature"
            implementationClass = "FeatureConventionPlugin"
        }
        register("featureRegistry") {
            id = "kmp.feature.registry"
            implementationClass = "FeatureRegistryPlugin"
        }
        register("scaffold") {
            id = "kmp.scaffold"
            implementationClass = "ScaffoldPlugin"
        }
    }
}
