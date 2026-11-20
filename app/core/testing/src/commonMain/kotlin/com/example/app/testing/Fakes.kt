package com.example.app.testing

import com.example.app.data.platform.KeyValueStorage
import com.example.app.domain.repository.CounterRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeKeyValueStorage : KeyValueStorage {
    private val values = mutableMapOf<String, String>()
    override fun get(key: String): String? = values[key]
    override fun set(key: String, value: String) {
        values[key] = value
    }
}

class FakeCounterRepository(initial: Int = 0) : CounterRepository {
    override val count = MutableStateFlow(initial)
    override fun set(value: Int) {
        count.value = value
    }
}
