package com.example.app.data.repository

import com.example.app.data.platform.KeyValueStorage
import com.example.app.domain.repository.CounterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CounterRepositoryImpl(private val storage: KeyValueStorage) : CounterRepository {
    private val _count = MutableStateFlow(storage.get(KEY)?.toIntOrNull() ?: 0)
    override val count: StateFlow<Int> = _count.asStateFlow()

    override fun set(value: Int) {
        _count.value = value
        storage.set(KEY, value.toString())
    }

    private companion object {
        const val KEY = "counter"
    }
}
