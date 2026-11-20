@file:OptIn(ExperimentalWasmJsInterop::class)

package com.example.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.compose.rememberNavController
import com.example.app.di.initKoin
import com.example.app.navigation.Destinations
import com.example.app.presentation.app.App

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    // bindToBrowserNavigation открывает экран из адреса поверх стартового: для `#home` стек стал бы [home, home].
    // Адрес стартового экрана убираем до привязки к истории.
    dropStartRouteFromUrl("#${Destinations.HOME}")
    initKoin()
    ComposeViewport {
        val navController = rememberNavController()
        App(navController)
        LaunchedEffect(navController) { navController.bindToBrowserNavigation() }
    }
}

private fun dropStartRouteFromUrl(hash: String): Unit =
    js("window.location.hash === hash ? window.history.replaceState(null, '', window.location.pathname + window.location.search) : undefined")
