package ru.kaed.ozonfresh.presentation.home

import androidx.lifecycle.viewModelScope
import ru.kaed.ozonfresh.domain.usecase.counter.IncrementCounterUseCase
import ru.kaed.ozonfresh.domain.usecase.counter.ObserveCounterUseCase
import ru.kaed.ozonfresh.domain.usecase.counter.ResetCounterUseCase
import ru.kaed.ozonfresh.presentation.home.HomeContract.Effect
import ru.kaed.ozonfresh.presentation.home.HomeContract.Intent
import ru.kaed.ozonfresh.presentation.home.HomeContract.State
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel
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
