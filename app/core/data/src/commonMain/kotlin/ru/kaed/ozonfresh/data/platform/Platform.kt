package ru.kaed.ozonfresh.data.platform

/**
 * Базовый URL API: в браузере — origin страницы, на Android — из настроек сборки (см. androidApp/build.gradle.kts),
 * на desktop — системное свойство app.apiBaseUrl, на iOS — ключ API_BASE_URL в Info.plist (из Config.xcconfig).
 */
internal expect val apiBaseUrl: String

internal expect fun platformStorageGet(key: String): String?
internal expect fun platformStorageSet(key: String, value: String)
