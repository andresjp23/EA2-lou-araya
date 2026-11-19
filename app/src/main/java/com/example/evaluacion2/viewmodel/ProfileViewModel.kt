package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.data.local.SessionStore
import com.example.evaluacion2.data.local.UserPreferencesRepository
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.model.UserPreferences
import com.example.evaluacion2.domain.repository.UserRepository
import com.example.evaluacion2.domain.validation.UserValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val userId: Int,
    private val userRepository: UserRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val session: SessionStore,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {

    private val _user = MutableStateFlow<UiState<User>>(UiState.Loading)
    val user: StateFlow<UiState<User>> = _user.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _formErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val formErrors: StateFlow<Map<String, String>> = _formErrors.asStateFlow()

    private val _saved = MutableStateFlow(false)
    /** true cuando el perfil se guardó y el diálogo de edición puede cerrarse. */
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun load() {
        load(_user) { userRepository.getUser(userId) ?: throw AppException.NotFound("Usuario no encontrado") }
        launchSafe {
            _notificationsEnabled.value = withContext(io) { preferencesRepository.get(userId)?.notificationsEnabled ?: true }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        launchSafe {
            withContext(io) { preferencesRepository.save(UserPreferences(userId, enabled)) }
            showMessage(if (enabled) "Preferencia de notificaciones guardada." else "Notificaciones desactivadas.")
        }
    }

    fun updateProfile(name: String, alias: String, phone: String) {
        val errors = buildMap {
            UserValidator.validateName(name)?.let { put("name", it) }
            UserValidator.validateAlias(alias)?.let { put("alias", it) }
            UserValidator.validatePhone(phone)?.let { put("phone", it) }
        }
        _formErrors.value = errors
        _saved.value = false
        if (errors.isNotEmpty()) return
        launchSafe(onError = { error ->
            if (error is AppException.Conflict) _formErrors.value = mapOf("alias" to "Ese alias ya está en uso")
            showMessage(error.message)
        }) {
            val updated = userRepository.updateProfile(userId, name, alias, phone)
            session.updateSessionProfile(updated)
            _user.value = UiState.Success(updated)
            _saved.value = true
            showMessage("Perfil actualizado.")
        }
    }

    fun editHandled() {
        _saved.value = false
        _formErrors.value = emptyMap()
    }
}
