package com.example.app.data.platform

import java.util.prefs.Preferences

private const val DEFAULT_API_BASE_URL = "https://example.com/"

/** Задаётся при запуске: `-Dapp.apiBaseUrl=...` (см. desktopApp/build.gradle.kts). */
internal actual val apiBaseUrl: String =
    System.getProperty("app.apiBaseUrl")?.takeIf { it.isNotBlank() } ?: DEFAULT_API_BASE_URL

// Узел по пути пакета: у каждого приложения из шаблона свои настройки (реестр Windows, ~/.java на Linux, plist на macOS).
private val preferences: Preferences = Preferences.userRoot().node("com/example/app")

internal actual fun platformStorageGet(key: String): String? = preferences.get(key, null)

internal actual fun platformStorageSet(key: String, value: String) {
    preferences.put(key, value)
    preferences.flush()
}
