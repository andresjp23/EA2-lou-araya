package com.example.evaluacion2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.validation.UserValidator

class SignUpViewModel(
    private val repository: UserRepository
) : ViewModel() {

    var uiState by mutableStateOf(
        SignUpUiState()
    )
        private set


    fun onNameChange(value: String) {

        uiState = uiState.copy(
            name = value,
            nameError = null,
            generalError = null
        )
    }


    fun onAliasChange(value: String) {

        uiState = uiState.copy(
            alias = value,
            aliasError = null,
            generalError = null
        )
    }


    fun onEmailChange(value: String) {

        uiState = uiState.copy(
            email = value,
            emailError = null,
            generalError = null
        )
    }


    fun onPhoneChange(value: String) {

        uiState = uiState.copy(
            phone = value,
            phoneError = null,
            generalError = null
        )
    }


    fun onPasswordChange(value: String) {

        uiState = uiState.copy(
            password = value,
            passwordError = null,
            confirmPasswordError = null,
            generalError = null
        )
    }


    fun onConfirmPasswordChange(
        value: String
    ) {

        uiState = uiState.copy(
            confirmPassword = value,
            confirmPasswordError = null,
            generalError = null
        )
    }


    fun register() {

        val nameError =
            UserValidator.validateName(
                uiState.name
            )

        val aliasError =
            UserValidator.validateAlias(
                uiState.alias
            )

        val emailError =
            UserValidator.validateEmail(
                uiState.email
            )

        val phoneError =
            UserValidator.validatePhone(
                uiState.phone
            )

        val passwordError =
            UserValidator.validatePassword(
                uiState.password
            )

        val confirmPasswordError =
            UserValidator
                .validatePasswordConfirmation(
                    uiState.password,
                    uiState.confirmPassword
                )


        uiState = uiState.copy(
            nameError = nameError,
            aliasError = aliasError,
            emailError = emailError,
            phoneError = phoneError,
            passwordError = passwordError,
            confirmPasswordError =
                confirmPasswordError,
            generalError = null
        )


        val hasErrors =
            nameError != null ||
                    aliasError != null ||
                    emailError != null ||
                    phoneError != null ||
                    passwordError != null ||
                    confirmPasswordError != null


        if (hasErrors) {
            return
        }


        if (
            repository.emailExists(
                uiState.email.trim()
            )
        ) {

            uiState = uiState.copy(
                emailError =
                    "Este correo ya está registrado"
            )

            return
        }


        if (
            repository.aliasExists(
                uiState.alias.trim()
            )
        ) {

            uiState = uiState.copy(
                aliasError =
                    "Este alias ya está registrado"
            )

            return
        }


        uiState = uiState.copy(
            isLoading = true
        )


        val user = User(
            name =
                uiState.name.trim(),

            alias =
                uiState.alias.trim(),

            email =
                uiState.email.trim(),

            phone =
                uiState.phone.trim(),

            password =
                uiState.password,

            role =
                "CLIENT"
        )


        val result =
            repository.registerUser(user)


        if (result.isSuccess) {

            uiState = uiState.copy(
                isLoading = false,
                registrationSuccess = true,
                generalError = null
            )

        } else {

            uiState = uiState.copy(
                isLoading = false,
                registrationSuccess = false,
                generalError =
                    "No fue posible crear la cuenta"
            )
        }
    }


    fun registrationHandled() {

        uiState = uiState.copy(
            registrationSuccess = false
        )
    }
}