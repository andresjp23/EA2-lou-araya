package com.example.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.evaluacion2.data.local.SessionManager
import com.example.evaluacion2.data.repository.UserPreferencesRepository
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(private val userId: Int, private val userRepository: UserRepository, private val preferencesRepository: UserPreferencesRepository, private val sessionManager: SessionManager) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun load() {
        _user.value = userRepository.getUserById(userId)
        _notificationsEnabled.value = preferencesRepository.get(userId)?.notificationsEnabled ?: true
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        preferencesRepository.save(UserPreferences(userId, enabled))
        _message.value = if (enabled) "Preferencia de notificaciones guardada." else "Notificaciones desactivadas."
    }

    fun updateProfile(name: String, alias: String, phone: String): Boolean {
        if (alias.trim().equals(_user.value?.alias, ignoreCase = true).not() && userRepository.aliasExists(alias)) {
            _message.value = "Ese alias ya está en uso."
            return false
        }
        val success = userRepository.updateProfile(userId, name, alias, phone).getOrDefault(false)
        if (success) {
            userRepository.getUserById(userId)?.let { user -> _user.value = user; sessionManager.updateSessionProfile(user) }
            _message.value = "Perfil actualizado."
        } else _message.value = "No se pudo actualizar el perfil."
        return success
    }
}
