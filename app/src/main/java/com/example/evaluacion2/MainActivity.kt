
package com.example.evaluacion2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.ui.screens.login.LoginScreen
import com.example.evaluacion2.ui.screens.signin.SignInScreen
import com.example.evaluacion2.ui.screens.signup.SignUpScreen
import com.example.evaluacion2.ui.theme.Evaluacion2Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Evaluacion2Theme {

                var pantalla by remember {
                    mutableStateOf("login")
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (pantalla) {

                        "login" -> LoginScreen(
                            onLoginClick = {
                                pantalla = "signin"
                            },
                            onCreateAccountClick = {
                                pantalla = "signup"
                            }
                        )

                        "signin" -> SignInScreen(
                            onLoginSuccess = {
                                pantalla = "home"
                            },
                            onBackClick = {
                                pantalla = "login"
                            }
                        )

                        "signup" -> SignUpScreen(
                            onRegisterSuccess = {
                                pantalla = "home"
                            },
                            onBackClick = {
                                pantalla =  "login"
                            }
                        )

                        "home" -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.Center
                        ) {
                            Text(
                                text = "Bienvenido a Lou Araya Macramé",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
