package com.example.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.app.data.platform.AndroidPlatform
import com.example.app.di.initKoin
import com.example.app.presentation.app.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidPlatform.init(this, BuildConfig.API_BASE_URL)
        initKoin()
        setContent { App(rememberNavController()) }
    }
}
