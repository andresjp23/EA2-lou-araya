package com.example.evaluacion2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.evaluacion2.data.local.SessionStore
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.UserRole
import com.example.evaluacion2.domain.repository.UserRepository
import com.example.evaluacion2.domain.validation.UserValidator

class SignInViewModel(
    private val repository: UserRepository,
    private val session: SessionStore
) : BaseViewModel() {
    var uiState by mutableStateOf(SignInUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, emailError = null, generalError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null, generalError = null)
    }

    /** Inicia sesión exigiendo que la cuenta tenga el rol elegido en la pantalla. */
    fun loginAs(roleCode: String) {
        login(expectedRole = UserRole.from(roleCode))
    }

    fun login(expectedRole: UserRole? = null) {
        if (uiState.isLoading || uiState.loginSuccess) return
        val email = uiState.email.trim()
        val emailError = UserValidator.validateEmail(email)
        val passwordError = if (uiState.password.isBlank()) "La contraseña es obligatoria" else null
        uiState = uiState.copy(emailError = emailError, passwordError = passwordError, generalError = null)
        if (emailError != null || passwordError != null) return

        uiState = uiState.copy(isLoading = true)
        launchSafe(onError = { error ->
            val text = if (error is AppException.Unauthorized) "Correo o contraseña incorrectos" else error.message
            uiState = uiState.copy(isLoading = false, generalError = text)
        }) {
            val user = repository.login(email, uiState.password)
            if (expectedRole != null && user.userRole != expectedRole) {
                uiState = uiState.copy(isLoading = false, generalError = "Esta cuenta no tiene el rol ${expectedRole.label}")
                return@launchSafe
            }
            session.saveSession(user)
            uiState = uiState.copy(isLoading = false, loginSuccess = true, userRole = user.role)
        }
    }

    fun loginHandled() {
        uiState = uiState.copy(loginSuccess = false)
    }
}
