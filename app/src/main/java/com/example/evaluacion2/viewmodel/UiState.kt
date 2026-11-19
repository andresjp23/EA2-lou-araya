package com.example.evaluacion2.viewmodel

/** Estado de una pantalla que carga datos: cargando, con datos o con error. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

/** Datos actuales si el estado es Success, o null. */
val <T> UiState<T>.dataOrNull: T?
    get() = (this as? UiState.Success)?.data
