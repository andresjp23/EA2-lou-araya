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

class SignInViewModel(
    private val repository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    var uiState by mutableStateOf(SignInUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, emailError = null, generalError = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, passwordError = null, generalError = null)
    }

    fun loginAs(role: String) {
        if (role !in setOf(User.ROLE_CLIENT, User.ROLE_ADMIN)) return
        loginExpectedRole = role
        login()
    }

    private var loginExpectedRole: String? = null

    fun login() {
        if (uiState.isLoading || uiState.loginSuccess) return
        val email = uiState.email.trim()
        val emailError = UserValidator.validateEmail(email)
        val passwordError = if (uiState.password.isBlank()) "La contraseña es obligatoria" else null
        uiState = uiState.copy(emailError = emailError, passwordError = passwordError, generalError = null)
        if (emailError != null || passwordError != null) return

        val password = uiState.password
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            val user = runCatching {
                withContext(Dispatchers.IO) { repository.login(email, password) }
            }.getOrNull()
            val acceptedUser = user?.takeIf { loginExpectedRole == null || it.role == loginExpectedRole }
            if (acceptedUser != null) sessionManager.saveSession(acceptedUser)
            uiState = if (acceptedUser != null) {
                uiState.copy(isLoading = false, loginSuccess = true, userRole = acceptedUser.role, generalError = null)
            } else {
                uiState.copy(isLoading = false, loginSuccess = false, generalError = "Correo o contraseña incorrectos")
            }
        }
    }

    fun loginHandled() {
        uiState = uiState.copy(loginSuccess = false)
    }
}
