package ru.kaed.ozonfresh

import androidx.compose.ui.window.ComposeUIViewController
import ru.kaed.ozonfresh.di.initKoin
import ru.kaed.ozonfresh.presentation.app.App
import platform.UIKit.UIViewController

/** Точка входа iOS: Swift-код в iosApp вызывает её как `MainViewControllerKt.MainViewController()`. */
@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}
