package com.example.app.presentation.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.flow.Flow

/** Подписка экрана на [MviViewModel.effects] на время его жизни в композиции. */
@Composable
fun <E> CollectEffects(effects: Flow<E>, onEffect: (E) -> Unit) {
    val handler by rememberUpdatedState(onEffect)
    LaunchedEffect(effects) { effects.collect { handler(it) } }
}
