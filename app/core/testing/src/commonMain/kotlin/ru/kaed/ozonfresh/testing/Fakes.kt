package ru.kaed.ozonfresh.testing

import ru.kaed.ozonfresh.data.platform.KeyValueStorage

class FakeKeyValueStorage : KeyValueStorage {
    private val values = mutableMapOf<String, String>()
    override fun get(key: String): String? = values[key]
    override fun set(key: String, value: String) {
        values[key] = value
    }
}
