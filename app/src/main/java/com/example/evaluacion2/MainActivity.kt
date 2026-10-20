package com.example.evaluacion2

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.evaluacion2.ui.navigation.AppNavigation
import com.example.evaluacion2.ui.screens.splash.SplashScreen
import com.example.evaluacion2.ui.theme.Evaluacion2Theme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)
        Configuration.getInstance().apply {
            load(this@MainActivity, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = "LouArayaMacrame/1.0 (+https://github.com/andresjp23/EA2-lou-araya)"
        }

        setContent {

            Evaluacion2Theme {

                Surface(
                    modifier =
                        Modifier.fillMaxSize(),

                    color =
                        MaterialTheme
                            .colorScheme
                            .background
                ) {
                    var showSplash by rememberSaveable { mutableStateOf(true) }

                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(durationMillis = 450),
                        label = "startupTransition"
                    ) { splashVisible ->
                        if (splashVisible) {
                            SplashScreen(onFinished = { showSplash = false })
                        } else {
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }
}
