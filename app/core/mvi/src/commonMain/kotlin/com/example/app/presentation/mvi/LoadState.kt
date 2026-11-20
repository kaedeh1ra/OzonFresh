package com.example.app.presentation.mvi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Error(val message: String) : LoadState<Nothing>
    data class Ready<T>(val data: T) : LoadState<T>
}

/** Поток «Loading → Ready | Error» для одной загрузки. */
fun <T> loadFlow(block: suspend () -> T): Flow<LoadState<T>> = flow {
    emit(LoadState.Loading)
    val result = try {
        LoadState.Ready(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        LoadState.Error(e.message ?: "Неизвестная ошибка")
    }
    emit(result)
}
