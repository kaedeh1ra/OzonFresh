package ru.kaed.ozonfresh.data.platform

import android.content.Context
import android.content.SharedPreferences

internal actual val apiBaseUrl: String
    get() = AndroidPlatform.requireInit().apiBaseUrl

/**
 * Платформенные настройки Android: адрес API из настроек сборки и SharedPreferences вместо localStorage.
 * [init] вызывается из Activity до запуска Koin; без него приложение падает с понятной ошибкой.
 */
object AndroidPlatform {
    private var state: State? = null

    internal class State(val prefs: SharedPreferences, val apiBaseUrl: String)

    fun init(context: Context, apiBaseUrl: String) {
        state = State(
            context.applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE),
            apiBaseUrl,
        )
    }

    internal fun requireInit(): State =
        checkNotNull(state) { "AndroidPlatform.init() не вызван: вызовите его в Activity.onCreate" }
}

internal actual fun platformStorageGet(key: String): String? =
    AndroidPlatform.requireInit().prefs.getString(key, null)

internal actual fun platformStorageSet(key: String, value: String) {
    AndroidPlatform.requireInit().prefs.edit().putString(key, value).apply()
}
