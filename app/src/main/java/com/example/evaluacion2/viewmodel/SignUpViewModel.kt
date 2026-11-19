package com.example.evaluacion2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.evaluacion2.data.local.SessionStore
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.repository.UserRepository
import com.example.evaluacion2.domain.validation.UserValidator

class SignUpViewModel(
    private val repository: UserRepository,
    private val session: SessionStore
) : BaseViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun onNameChange(value: String) { uiState = uiState.copy(name = value, nameError = null, generalError = null) }
    fun onAliasChange(value: String) { uiState = uiState.copy(alias = value, aliasError = null, generalError = null) }
    fun onEmailChange(value: String) { uiState = uiState.copy(email = value, emailError = null, generalError = null) }
    fun onPhoneChange(value: String) { uiState = uiState.copy(phone = value, phoneError = null, generalError = null) }
    fun onPasswordChange(value: String) { uiState = uiState.copy(password = value, passwordError = null, confirmPasswordError = null, generalError = null) }
    fun onConfirmPasswordChange(value: String) { uiState = uiState.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }
    fun onAvatarUriChange(value: String?) { uiState = uiState.copy(avatarUri = value) }

    fun register() {
        if (uiState.isLoading) return
        val state = uiState
        val nameError = UserValidator.validateName(state.name)
        val aliasError = UserValidator.validateAlias(state.alias)
        val emailError = UserValidator.validateEmail(state.email)
        val phoneError = UserValidator.validatePhone(state.phone)
        val passwordError = UserValidator.validatePassword(state.password)
        val confirmError = UserValidator.validatePasswordConfirmation(state.password, state.confirmPassword)
        uiState = state.copy(
            nameError = nameError, aliasError = aliasError, emailError = emailError, phoneError = phoneError,
            passwordError = passwordError, confirmPasswordError = confirmError, generalError = null
        )
        if (listOf(nameError, aliasError, emailError, phoneError, passwordError, confirmError).any { it != null }) return

        val user = User(
            name = state.name.trim(), alias = state.alias.trim(), email = state.email.trim(), phone = state.phone.trim(),
            password = state.password, role = User.ROLE_CLIENT, avatarUri = state.avatarUri
        )
        uiState = uiState.copy(isLoading = true)
        launchSafe(onError = ::onRegisterError) {
            val created = repository.register(user)
            session.saveSession(created)
            uiState = uiState.copy(isLoading = false, registrationSuccess = true)
        }
    }

    /** Los errores del backend se muestran junto al campo que corresponde. */
    private fun onRegisterError(error: AppException) {
        val text = error.message
        uiState = when {
            error is AppException.Conflict && text?.contains("correo", true) == true ->
                uiState.copy(isLoading = false, emailError = "Este correo ya está registrado", generalError = text)
            error is AppException.Conflict && text?.contains("alias", true) == true ->
                uiState.copy(isLoading = false, aliasError = "Este alias ya está registrado", generalError = text)
            error is AppException.Validation -> uiState.copy(
                isLoading = false,
                generalError = text,
                nameError = error.fieldErrors["name"] ?: uiState.nameError,
                aliasError = error.fieldErrors["alias"] ?: uiState.aliasError,
                emailError = error.fieldErrors["email"] ?: uiState.emailError,
                phoneError = error.fieldErrors["phone"] ?: uiState.phoneError,
                passwordError = error.fieldErrors["password"] ?: uiState.passwordError
            )
            else -> uiState.copy(isLoading = false, generalError = text ?: "No fue posible crear la cuenta")
        }
    }

    fun registrationHandled() {
        uiState = uiState.copy(registrationSuccess = false)
    }
}
