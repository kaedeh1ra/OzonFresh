import com.example.app.buildlogic.Constants
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(Constants.Modules.SHARED))
            implementation(project(Constants.Modules.NAVIGATION))

            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.navigation.compose)
        }
    }
}
// Dev- и production-сборки делят папку build/wasm/packages/<модуль>/kotlin, и при переключении .wasm и .mjs
// оказываются из разных сборок (LinkError). Метка в папке заставляет Gradle синхронизировать её целиком.
val wasmPackagesDir =
    rootProject.layout.buildDirectory.dir("wasm/packages/${rootProject.name}-app-webApp/kotlin")
val forceFullWasmSync = tasks.register("forceFullWasmSync") {
    val dir = wasmPackagesDir
    outputs.upToDateWhen { false }
    doLast {
        val folder = dir.get().asFile.apply { mkdirs() }
        folder.resolve(".full-sync").writeText(System.nanoTime().toString())
    }
}
tasks.matching { it.name.endsWith("ExecutableCompileSync") && !it.name.contains("Test") }
    .configureEach {
        dependsOn(forceFullWasmSync)
    }

// Без preload wasm приложения качается только после skiko.wasm; preload в index.html запускает обе загрузки сразу.
// Имена файлов — хэши webpack, поэтому ссылки дописываются в index.html уже собранного дистрибутива.
val distributionDir = layout.buildDirectory.dir("dist/wasmJs/productionExecutable")
tasks.named("wasmJsBrowserDistribution") {
    val dir = distributionDir
    doLast {
        val folder = dir.get().asFile
        val index = folder.resolve("index.html")
        val wasm =
            folder.listFiles { file -> file.extension == "wasm" }.orEmpty().map { it.name }.sorted()
        check(index.isFile && wasm.isNotEmpty()) { "В $folder нет index.html или .wasm: некуда добавить preload" }
        // crossorigin обязателен: wasm качается через fetch(), и без него браузер не отдаст предзагруженный ответ.
        val links = wasm.joinToString("") {
            "    <link rel=\"preload\" href=\"$it\" as=\"fetch\" type=\"application/wasm\" crossorigin>\n"
        }
        val html = index.readText()
            .replace(Regex(" *<link rel=\"preload\"[^>]*\\.wasm\"[^>]*>\\r?\\n"), "")
        index.writeText(html.replaceFirst("</head>", "$links</head>"))
    }
}
