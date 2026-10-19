package com.example.evaluacion2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion2.data.local.SessionManager
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.validation.UserValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpViewModel(
    private val repository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    var uiState by mutableStateOf(SignUpUiState())
        private set

    fun onNameChange(value: String) { uiState = uiState.copy(name = value, nameError = null, generalError = null) }
    fun onAliasChange(value: String) { uiState = uiState.copy(alias = value, aliasError = null, generalError = null) }
    fun onEmailChange(value: String) { uiState = uiState.copy(email = value, emailError = null, generalError = null) }
    fun onPhoneChange(value: String) { uiState = uiState.copy(phone = value, phoneError = null, generalError = null) }
    fun onPasswordChange(value: String) { uiState = uiState.copy(password = value, passwordError = null, confirmPasswordError = null, generalError = null) }
    fun onAvatarUriChange(value: String?) {
        uiState = uiState.copy(avatarUri = value)
    }

    fun onConfirmPasswordChange(value: String) { uiState = uiState.copy(confirmPassword = value, confirmPasswordError = null, generalError = null) }

    fun register() {
        if (uiState.isLoading) return
        val nameError = UserValidator.validateName(uiState.name)
        val aliasError = UserValidator.validateAlias(uiState.alias)
        val emailError = UserValidator.validateEmail(uiState.email)
        val phoneError = UserValidator.validatePhone(uiState.phone)
        val passwordError = UserValidator.validatePassword(uiState.password)
        val confirmPasswordError = UserValidator.validatePasswordConfirmation(uiState.password, uiState.confirmPassword)
        uiState = uiState.copy(
            nameError = nameError,
            aliasError = aliasError,
            emailError = emailError,
            phoneError = phoneError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            generalError = null
        )
        if (listOf(nameError, aliasError, emailError, phoneError, passwordError, confirmPasswordError).any { it != null }) return

        val user = User(
            name = uiState.name.trim(),
            alias = uiState.alias.trim(),
            email = uiState.email.trim(),
            phone = uiState.phone.trim(),
            password = uiState.password,
            role = User.ROLE_CLIENT,
            avatarUri = uiState.avatarUri
        )
        uiState = uiState.copy(isLoading = true)

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (repository.emailExists(user.email)) throw RegistrationException("email", "Este correo ya está registrado")
                    if (repository.aliasExists(user.alias)) throw RegistrationException("alias", "Este alias ya está registrado")
                    val id = repository.registerUser(user).getOrThrow()
                    user.copy(id = id.toInt())
                }
            }
            result.onSuccess(sessionManager::saveSession)
            val registrationError = result.exceptionOrNull() as? RegistrationException
            val failureMessage = result.exceptionOrNull()?.let { error ->
                (error as? RegistrationException)?.message ?: "No fue posible crear la cuenta"
            }
            uiState = uiState.copy(
                isLoading = false,
                registrationSuccess = result.isSuccess,
                generalError = failureMessage,
                emailError = if (registrationError?.field == "email") failureMessage else uiState.emailError,
                aliasError = if (registrationError?.field == "alias") failureMessage else uiState.aliasError
            )
        }
    }

    fun registrationHandled() {
        uiState = uiState.copy(registrationSuccess = false)
    }

    private class RegistrationException(val field: String, message: String) : Exception(message)
}
