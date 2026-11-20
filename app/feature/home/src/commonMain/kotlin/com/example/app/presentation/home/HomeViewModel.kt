package com.example.app.presentation.home

import androidx.lifecycle.viewModelScope
import com.example.app.domain.usecase.counter.IncrementCounterUseCase
import com.example.app.domain.usecase.counter.ObserveCounterUseCase
import com.example.app.domain.usecase.counter.ResetCounterUseCase
import com.example.app.presentation.home.HomeContract.Effect
import com.example.app.presentation.home.HomeContract.Intent
import com.example.app.presentation.home.HomeContract.State
import com.example.app.presentation.mvi.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** Пример экрана: состояние из репозитория через use case'ы, действия — через [Intent], разовые события — через [Effect]. */
class HomeViewModel(
    observeCounter: ObserveCounterUseCase,
    private val incrementCounter: IncrementCounterUseCase,
    private val resetCounter: ResetCounterUseCase,
) : MviViewModel<State, Intent, Effect>(State(count = observeCounter().value)) {

    init {
        observeCounter().onEach { setState { copy(count = it) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Increment -> incrementCounter()
            Intent.Reset -> {
                resetCounter()
                sendEffect(Effect.ShowMessage("Счётчик сброшен"))
            }
        }
    }
}
