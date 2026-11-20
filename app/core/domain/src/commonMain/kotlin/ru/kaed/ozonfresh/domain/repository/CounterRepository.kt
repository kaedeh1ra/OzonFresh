package ru.kaed.ozonfresh.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Пример репозитория: счётчик, который переживает перезапуск приложения. Реализация — в :app:core:data. */
interface CounterRepository {
    val count: StateFlow<Int>

    fun set(value: Int)
}
