package ru.kaed.ozonfresh.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import ru.kaed.ozonfresh.data.platform.AndroidPlatform
import ru.kaed.ozonfresh.di.initKoin
import ru.kaed.ozonfresh.presentation.app.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidPlatform.init(this, BuildConfig.API_BASE_URL)
        initKoin()
        setContent { App(rememberNavController()) }
    }
}
