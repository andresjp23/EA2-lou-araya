package com.example.evaluacion2.ui.screens.signup

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.R
import com.example.evaluacion2.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {

    val uiState = viewModel.uiState
    var avatar by remember { mutableStateOf("🌿") }
    var avatarUri by remember { mutableStateOf<String?>(null) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> avatarUri = uri?.toString(); viewModel.onAvatarUriChange(uri?.toString()) }

    // Cuando SQLite confirma el registro,
    // recién entonces navegamos al Dashboard.
    LaunchedEffect(uiState.registrationSuccess) {

        if (uiState.registrationSuccess) {

            viewModel.registrationHandled()

            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        // -------------------------------------------------
        // VOLVER
        // -------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {

            TextButton(
                onClick = onBackClick,
                modifier =
                    Modifier.align(
                        Alignment.CenterStart
                    )
            ) {

                Text("← Volver")
            }
        }


        // -------------------------------------------------
        // LOGO
        // -------------------------------------------------

        Image(
            painter = painterResource(
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
            text = "Crear cuenta",
            style =
                MaterialTheme
                    .typography
                    .headlineLarge
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Text(
            text =
                "Únete a Lou Araya Macramé y sé parte de nuestra comunidad",

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // -------------------------------------------------
        // NOMBRE
        // -------------------------------------------------

        OutlinedTextField(
            value =
                uiState.name,

            onValueChange =
                viewModel::onNameChange,

            label = {
                Text("Nombre")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            isError =
                uiState.nameError != null,

            trailingIcon = {

                if (
                    uiState.nameError != null
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

                uiState.nameError?.let {

                    Text(it)
                }
            }
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // -------------------------------------------------
        // ALIAS
        // -------------------------------------------------

        OutlinedTextField(
            value =
                uiState.alias,

            onValueChange =
                viewModel::onAliasChange,

            label = {
                Text("Alias")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            isError =
                uiState.aliasError != null,

            trailingIcon = {

                if (
                    uiState.aliasError != null
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

                uiState.aliasError?.let {

                    Text(it)
                }
            }
        )


        Spacer(modifier = Modifier.height(12.dp))
        Text("Selecciona un avatar", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🌿", "🌸", "🧶", "✨").forEach { option ->
                FilterChip(selected = avatar == option, onClick = { avatar = option; avatarUri = null; viewModel.onAvatarUriChange(null) }, label = { Text(option) })
            }
            TextButton(onClick = { avatarPicker.launch("image/*") }) { Text(if (avatarUri == null) "+ Agregar otro" else "Avatar personalizado seleccionado") }
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // -------------------------------------------------
        // CORREO
        // -------------------------------------------------

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
                Modifier.height(12.dp)
        )


        // -------------------------------------------------
        // TELÉFONO
        // -------------------------------------------------

        OutlinedTextField(
            value =
                uiState.phone,

            onValueChange =
                viewModel::onPhoneChange,

            label = {
                Text("Teléfono")
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Phone
                ),

            isError =
                uiState.phoneError != null,

            trailingIcon = {

                if (
                    uiState.phoneError != null
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

                uiState.phoneError?.let {

                    Text(it)
                }
            }
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // -------------------------------------------------
        // CONTRASEÑA
        // -------------------------------------------------

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


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // -------------------------------------------------
        // CONFIRMAR CONTRASEÑA
        // -------------------------------------------------

        OutlinedTextField(
            value =
                uiState.confirmPassword,

            onValueChange =
                viewModel::
                onConfirmPasswordChange,

            label = {
                Text(
                    "Confirmar contraseña"
                )
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
                uiState
                    .confirmPasswordError != null,

            trailingIcon = {

                if (
                    uiState
                        .confirmPasswordError != null
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

                uiState
                    .confirmPasswordError
                    ?.let {

                        Text(it)
                    }
            }
        )


        // -------------------------------------------------
        // ERROR GENERAL
        // -------------------------------------------------

        uiState.generalError?.let {

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text = it,

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // -------------------------------------------------
        // CREAR CUENTA
        // -------------------------------------------------

        Button(
            onClick = {
                viewModel.register()
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

                Text(
                    "Crear cuenta"
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Al registrarte aceptas nuestros Términos y Condiciones", style = MaterialTheme.typography.bodySmall)
    }
}