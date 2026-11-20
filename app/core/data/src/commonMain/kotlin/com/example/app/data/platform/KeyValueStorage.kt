package com.example.app.data.platform

/** Хранилище настроек: localStorage в браузере, SharedPreferences на Android, java.util.prefs на desktop, NSUserDefaults на iOS. */
interface KeyValueStorage {
    fun get(key: String): String?
    fun set(key: String, value: String)
}

internal object PlatformKeyValueStorage : KeyValueStorage {
    override fun get(key: String): String? = platformStorageGet(key)
    override fun set(key: String, value: String) = platformStorageSet(key, value)
}
