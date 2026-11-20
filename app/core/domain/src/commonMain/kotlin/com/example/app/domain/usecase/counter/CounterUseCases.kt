package com.example.app.domain.usecase.counter

import com.example.app.domain.repository.CounterRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveCounterUseCase(private val repository: CounterRepository) {
    operator fun invoke(): StateFlow<Int> = repository.count
}

class IncrementCounterUseCase(private val repository: CounterRepository) {
    operator fun invoke() = repository.set(repository.count.value + 1)
}

class ResetCounterUseCase(private val repository: CounterRepository) {
    operator fun invoke() = repository.set(0)
}
