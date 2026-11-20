package com.example.app.data.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

private const val DEFAULT_API_BASE_URL = "https://example.com/"

/** Ключ API_BASE_URL в iosApp/iosApp/Info.plist, значение задаётся в iosApp/Configuration/Config.xcconfig. */
internal actual val apiBaseUrl: String =
    (NSBundle.mainBundle.objectForInfoDictionaryKey("API_BASE_URL") as? String)
        ?.takeIf { it.isNotBlank() } ?: DEFAULT_API_BASE_URL

internal actual fun platformStorageGet(key: String): String? =
    NSUserDefaults.standardUserDefaults.stringForKey(key)

internal actual fun platformStorageSet(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
}
