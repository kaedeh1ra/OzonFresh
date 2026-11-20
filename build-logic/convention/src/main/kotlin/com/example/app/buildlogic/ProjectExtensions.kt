package com.example.app.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

/** `:app:core:domain` → `com.example.app.core.domain`: у каждого модуля свой Android namespace, иначе R-классы конфликтуют. */
internal val Project.androidNamespace: String
    get() = "${Constants.BASE_PACKAGE}." + path.split(":").filter { it.isNotEmpty() }.drop(1)
        .joinToString(".") { it.replace("-", "") }
