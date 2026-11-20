package com.example.app.presentation.home

object HomeContract {
    data class State(
        val count: Int = 0,
    )

    sealed interface Intent {
        data object Increment : Intent
        data object Reset : Intent
    }

    sealed interface Effect {
        data class ShowMessage(val text: String) : Effect
    }
}
