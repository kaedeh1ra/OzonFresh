package ru.kaed.ozonfresh.data.repository

import ru.kaed.ozonfresh.data.platform.KeyValueStorage
import ru.kaed.ozonfresh.domain.repository.CounterRepository
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
