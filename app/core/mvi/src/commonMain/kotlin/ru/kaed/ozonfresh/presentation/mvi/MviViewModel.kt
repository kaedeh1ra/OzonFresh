package ru.kaed.ozonfresh.presentation.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Основа экранов в стиле MVI:
 * - [State] — всё, что рисует экран; меняется только через [setState];
 * - [Intent] — намерения пользователя, единственный вход — [onIntent];
 * - [Effect] — одноразовые события (навигация, тосты), которые не должны пережить пересоздание экрана.
 */
abstract class MviViewModel<State, Intent, Effect>(initialState: State) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    protected val currentState: State get() = _state.value

    abstract fun onIntent(intent: Intent)

    protected fun setState(reduce: State.() -> State) = _state.update(reduce)

    protected fun sendEffect(effect: Effect) {
        _effects.trySend(effect)
    }
}
