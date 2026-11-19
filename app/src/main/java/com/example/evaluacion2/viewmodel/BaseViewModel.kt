package com.example.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion2.domain.error.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Clase base de los ViewModels de la app. Centraliza el manejo de errores:
 * cualquier excepción de una corrutina se convierte en un mensaje para el usuario.
 */
abstract class BaseViewModel : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    /** Mensaje puntual (éxito o error) para mostrar en un Snackbar o texto. */
    val message: StateFlow<String?> = _message.asStateFlow()

    protected fun showMessage(text: String?) {
        _message.value = text
    }

    fun clearMessage() {
        _message.value = null
    }

    /** Lanza una corrutina y envía los errores a [onError] (por defecto, se muestran como mensaje). */
    protected fun launchSafe(
        onError: (AppException) -> Unit = { showMessage(it.message) },
        block: suspend () -> Unit
    ): Job = viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: AppException) {
            onError(e)
        } catch (e: Exception) {
            onError(AppException.Unknown(e))
        }
    }

    /** Carga una lista u objeto y publica el resultado en [state] como [UiState]. */
    protected fun <T> load(state: MutableStateFlow<UiState<T>>, block: suspend () -> T): Job {
        if (state.value !is UiState.Success) state.value = UiState.Loading
        return launchSafe(onError = { state.value = UiState.Error(it.message ?: "Error") }) {
            state.value = UiState.Success(block())
        }
    }
}
