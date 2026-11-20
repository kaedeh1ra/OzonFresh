package ru.kaed.ozonfresh.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.kaed.ozonfresh.di.initKoin
import ru.kaed.ozonfresh.presentation.app.App

/** Точка входа desktop: Windows, Linux и macOS. */
fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Ozon Fresh",
            state = rememberWindowState(width = 960.dp, height = 720.dp),
        ) {
            App()
        }
    }
}
