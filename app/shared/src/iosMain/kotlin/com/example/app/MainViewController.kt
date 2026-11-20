package com.example.app

import androidx.compose.ui.window.ComposeUIViewController
import com.example.app.di.initKoin
import com.example.app.presentation.app.App
import platform.UIKit.UIViewController

/** Точка входа iOS: Swift-код в iosApp вызывает её как `MainViewControllerKt.MainViewController()`. */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
