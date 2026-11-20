package com.example.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.presentation.designsystem.components.AppCard
import com.example.app.presentation.designsystem.components.contentPadding
import com.example.app.presentation.designsystem.components.windowSize
import com.example.app.presentation.home.HomeContract.Effect
import com.example.app.presentation.home.HomeContract.Intent
import com.example.app.presentation.home.HomeContract.State
import com.example.app.presentation.mvi.CollectEffects
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun HomeRoute() {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is Effect.ShowMessage -> scope.launch { snackbar.showSnackbar(effect.text) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        HomeScreen(state, viewModel::onIntent)
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun HomeScreen(state: State, onIntent: (Intent) -> Unit) {
    Box(
        Modifier.fillMaxSize().padding(windowSize.contentPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        AppCard(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
            Text("Шаблон Kotlin Multiplatform", style = MaterialTheme.typography.titleLarge)
            Text(
                "Счётчик хранится между запусками: localStorage в браузере, SharedPreferences на Android.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "${state.count}",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally),
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                OutlinedButton({ onIntent(Intent.Reset) }) { Text("Сбросить") }
                Button({ onIntent(Intent.Increment) }) { Text("+1") }
            }
        }
    }
}
