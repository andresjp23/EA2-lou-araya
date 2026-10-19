package com.example.evaluacion2

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.evaluacion2.ui.navigation.AppNavigation
import com.example.evaluacion2.ui.theme.Evaluacion2Theme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)
        Configuration.getInstance().apply {
            load(this@MainActivity, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = "LouArayaEventos/1.0 (Android; contact: admin@louaraya.cl)"
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

                    AppNavigation()
                }
            }
        }
    }
}