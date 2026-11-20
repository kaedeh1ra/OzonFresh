package ru.kaed.ozonfresh.presentation.home

import ru.kaed.ozonfresh.presentation.home.HomeContract.Effect
import ru.kaed.ozonfresh.presentation.home.HomeContract.Intent
import ru.kaed.ozonfresh.presentation.home.HomeContract.State
import ru.kaed.ozonfresh.presentation.mvi.MviViewModel

class HomeViewModel : MviViewModel<State, Intent, Effect>(State()) {
    override fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Increment, Intent.Reset -> Unit
        }
    }
}
