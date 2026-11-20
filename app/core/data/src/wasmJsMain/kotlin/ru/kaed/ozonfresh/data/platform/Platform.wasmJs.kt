@file:OptIn(ExperimentalWasmJsInterop::class)

package ru.kaed.ozonfresh.data.platform

internal actual val apiBaseUrl: String = js("window.location.origin + '/'")

// localStorage может быть недоступен (приватный режим, запрет cookies), поэтому все обращения в try/catch.

internal actual fun platformStorageGet(key: String): String? =
    js("(function () { try { return window.localStorage.getItem(key); } catch (e) { return null; } })()")

internal actual fun platformStorageSet(key: String, value: String): Unit =
    js("(function () { try { window.localStorage.setItem(key, value); } catch (e) {} })()")
