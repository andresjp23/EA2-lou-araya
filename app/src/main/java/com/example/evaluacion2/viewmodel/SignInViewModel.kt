package com.example.evaluacion2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.domain.validation.UserValidator

class SignInViewModel(
    private val repository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        SignInUiState()
    )
        private set


    fun onEmailChange(
        value: String
    ) {

        uiState = uiState.copy(
            email = value,
            emailError = null,
            generalError = null
        )
    }


    fun onPasswordChange(
        value: String
    ) {

        uiState = uiState.copy(
            password = value,
            passwordError = null,
            generalError = null
        )
    }


    fun login() {

        val emailError =
            UserValidator.validateEmail(
                uiState.email
            )

        val passwordError =
            UserValidator.validatePassword(
                uiState.password
            )


        uiState = uiState.copy(
            emailError = emailError,
            passwordError = passwordError,
            generalError = null
        )


        if (
            emailError != null ||
            passwordError != null
        ) {
            return
        }


        uiState = uiState.copy(
            isLoading = true
        )


        val user =
            repository.login(
                email =
                    uiState.email,

                password =
                    uiState.password
            )


        if (user != null) {

            uiState = uiState.copy(
                isLoading = false,
                loginSuccess = true,
                userRole = user.role,
                generalError = null
            )

        } else {

            uiState = uiState.copy(
                isLoading = false,
                loginSuccess = false,
                generalError =
                    "Correo o contraseña incorrectos"
            )
        }
    }


    fun loginHandled() {

        uiState = uiState.copy(
            loginSuccess = false
        )
    }
}