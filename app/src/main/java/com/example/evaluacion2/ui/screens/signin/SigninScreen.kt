package com.example.evaluacion2.ui.screens.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.R
import com.example.evaluacion2.viewmodel.SignInViewModel

@Composable
fun SignInScreen(
    viewModel: SignInViewModel,
    onLoginSuccess: (String) -> Unit,
    onBackClick: () -> Unit
) {

    val uiState =
        viewModel.uiState


    LaunchedEffect(
        uiState.loginSuccess
    ) {

        if (
            uiState.loginSuccess
        ) {

            val role =
                uiState.userRole ?: "CLIENT"

            viewModel.loginHandled()

            onLoginSuccess(role)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 16.dp
                )
        ) {

            TextButton(
                onClick =
                    onBackClick,

                modifier =
                    Modifier.align(
                        Alignment.CenterStart
                    )
            ) {

                Text("← Volver")
            }
        }


        Image(
            painter =
                painterResource(
                    id =
                        R.drawable
                            .logo_lou_araya_terracotta
                ),

            contentDescription =
                "Logo Lou Araya Macramé",

            contentScale =
                ContentScale.Fit,

            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        Text(
            text =
                "Iniciar sesión",

            style =
                MaterialTheme
                    .typography
                    .headlineLarge
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        OutlinedTextField(
            value =
                uiState.email,

            onValueChange =
                viewModel::onEmailChange,

            label = {
                Text(
                    "Correo electrónico"
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Email
                ),

            isError =
                uiState.emailError != null,

            trailingIcon = {

                if (
                    uiState.emailError != null
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Error,

                        contentDescription =
                            "Error"
                    )
                }
            },

            supportingText = {

                uiState.emailError?.let {
                    Text(it)
                }
            }
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        OutlinedTextField(
            value =
                uiState.password,

            onValueChange =
                viewModel::onPasswordChange,

            label = {
                Text("Contraseña")
            },

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Password
                ),

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            isError =
                uiState.passwordError != null,

            trailingIcon = {

                if (
                    uiState.passwordError != null
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Error,

                        contentDescription =
                            "Error"
                    )
                }
            },

            supportingText = {

                uiState.passwordError?.let {
                    Text(it)
                }
            }
        )


        uiState.generalError?.let {

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = it,

                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        Button(
            onClick = {
                viewModel.login()
            },

            enabled =
                !uiState.isLoading,

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            if (
                uiState.isLoading
            ) {

                CircularProgressIndicator(
                    modifier =
                        Modifier.size(22.dp),

                    strokeWidth =
                        2.dp
                )

            } else {

                Text("Ingresar")
            }
        }
    }
}