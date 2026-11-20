package com.example.app.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.app.di.initKoin
import com.example.app.presentation.app.App

/** Точка входа desktop: Windows, Linux и macOS. */
fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Template",
            state = rememberWindowState(width = 960.dp, height = 720.dp),
        ) {
            App()
        }
    }
}
