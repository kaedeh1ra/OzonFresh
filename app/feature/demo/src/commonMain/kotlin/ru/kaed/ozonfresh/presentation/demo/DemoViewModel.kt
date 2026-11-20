package ru.kaed.ozonfresh.presentation.demo

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.kaed.ozonfresh.domain.usecase.demo.DemoControlsUseCase
import ru.kaed.ozonfresh.domain.usecase.replenishment.ObserveReplenishmentDiagnosticsUseCase
import ru.kaed.ozonfresh.presentation.demo.DemoContract.Effect
import ru.kaed.ozonfresh.presentation.demo.DemoContract.Intent
import ru.kaed.ozonfresh.presentation.demo.DemoContract.State
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

/** Остаток, который ставит переключатель «в наличии». */
private const val RESTOCK_QUANTITY = 20

class DemoViewModel(
    private val controls: DemoControlsUseCase,
    observeDiagnostics: ObserveReplenishmentDiagnosticsUseCase,
) : MviViewModel<State, Intent, Effect>(State(today = controls.today.value, group = controls.group.value)) {

    init {
        controls.today.onEach { setState { copy(today = it) } }.launchIn(viewModelScope)
        controls.group.onEach { setState { copy(group = it) } }.launchIn(viewModelScope)
        controls.stock.onEach { list -> setState { copy(stock = list.associate { it.productId to it.quantity }) } }
            .launchIn(viewModelScope)
        controls.events.onEach { setState { copy(events = it) } }.launchIn(viewModelScope)
        observeDiagnostics().onEach { setState { copy(diagnostics = it) } }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.ShiftDay -> controls.shiftDay(intent.days)
            is Intent.SetGroup -> controls.setGroup(intent.group)
            is Intent.SetInStock -> controls.setStock(intent.productId, if (intent.inStock) RESTOCK_QUANTITY else 0)
            Intent.Reset -> {
                controls.reset()
                sendEffect(Effect.ShowMessage("Данные сброшены к исходным"))
            }
        }
    }
}
