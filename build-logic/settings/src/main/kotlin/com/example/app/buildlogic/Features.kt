package com.example.app.buildlogic

import java.io.File
import java.util.Locale

/**
 * Где лежат фичи и как их зовут. Одно место для settings (include), реестра Koin-модулей и генератора:
 * все три должны одинаково понимать, что такое фича.
 */
object FeatureLayout {
    const val DIR = "app/feature"
    const val PATH_PREFIX = ":app:feature:"
    const val BASE_PACKAGE = "${Constants.BASE_PACKAGE}.presentation"

    /** Фича — папка в [DIR] со своим build.gradle.kts. Порядок стабильный, чтобы реестр не менялся от ОС. */
    fun discover(rootDir: File): List<FeatureName> =
        File(rootDir, DIR).listFiles().orEmpty()
            .filter { it.isDirectory && File(it, "build.gradle.kts").isFile }
            .map { FeatureName(it.name) }
            .sortedBy { it.raw }
}

/** Имя фичи в kebab-case («files-check») и производные от него: пакет, классы, путь проекта. */
class FeatureName(val raw: String) {
    init {
        require(raw.matches(NAME)) {
            "Имя фичи «$raw» не подходит: строчные латинские буквы и цифры, слова через дефис, например files-check"
        }
    }

    val pascal: String = raw.split("-")
        .joinToString("") { part -> part.replaceFirstChar { it.titlecase(Locale.ROOT) } }
    val camel: String = pascal.replaceFirstChar { it.lowercase(Locale.ROOT) }
    val packageName: String = "${FeatureLayout.BASE_PACKAGE}.${raw.replace("-", "")}"
    val projectPath: String = FeatureLayout.PATH_PREFIX + raw

    /** Koin-модуль, который фича обязана объявить: `val <camel>FeatureModule` в корне своего пакета. */
    val koinModule: String = "$packageName.${camel}FeatureModule"

    private companion object {
        val NAME = Regex("[a-z][a-z0-9]*(-[a-z0-9]+)*")
    }
}
