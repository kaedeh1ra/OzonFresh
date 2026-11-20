package ru.kaed.ozonfresh.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ru.kaed.ozonfresh.data.platform.AndroidPlatform
import ru.kaed.ozonfresh.di.initKoin
import ru.kaed.ozonfresh.presentation.app.App

private const val DARK_SURFACE = 0xFF1C2025.toInt()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Под статус-баром всегда тёмная шапка: светлые иконки при любой теме системы.
        // Внизу — нижняя панель приложения: белая в светлой теме, тёмная (#1C2025) в тёмной.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.WHITE, DARK_SURFACE),
        )
        super.onCreate(savedInstanceState)
        AndroidPlatform.init(this, BuildConfig.API_BASE_URL)
        initKoin()
        setContent { App(rememberNavController()) }
    }
}
