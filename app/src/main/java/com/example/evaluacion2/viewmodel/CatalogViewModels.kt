package com.example.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion2.data.repository.FavoriteRepository
import com.example.evaluacion2.data.repository.ProductRepository
import com.example.evaluacion2.data.repository.RequestRepository
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.validation.QuoteRequestValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CatalogViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _products = MutableStateFlow(emptyList<Product>())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _products.value = withContext(Dispatchers.IO) { repository.getProducts() }
        }
    }
}

class FavoritesViewModel(
    private val userId: Int,
    private val favorites: FavoriteRepository,
    private val productsRepository: ProductRepository
) : ViewModel() {
    private val _products = MutableStateFlow(emptyList<Product>())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _products.value = withContext(Dispatchers.IO) {
                favorites.getForUser(userId).map { it.productId }.distinct().mapNotNull(productsRepository::findById)
            }
        }
    }
}

data class QuoteFormState(
    val productName: String = "",
    val eventType: String = "",
    val dimensions: String = "",
    val material: String = "",
    val colors: String = "",
    val requiredDate: String = "",
    val observations: String = "",
    val errors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class QuoteViewModel(
    private val userId: Int,
    private val productId: Int?,
    private val repository: RequestRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuoteFormState())
    val state: StateFlow<QuoteFormState> = _state.asStateFlow()

    fun setEventType(value: String) = update { copy(eventType = value) }
        fun setDimensions(value: String) = update { copy(dimensions = value) }
    fun setMaterial(value: String) = update { copy(material = value) }
    fun setColors(value: String) = update { copy(colors = value) }
    fun setRequiredDate(value: String) = update { copy(requiredDate = value) }
    fun setObservations(value: String) = update { copy(observations = value) }

    private fun update(transform: QuoteFormState.() -> QuoteFormState) {
        _state.value = _state.value.transform().copy(successMessage = null, errorMessage = null)
    }

    fun submit() {
        val current = _state.value.copy(productName = productId?.let { "Producto #$it" }.orEmpty())
        val errors = QuoteRequestValidator.validate(current.eventType, current.dimensions, current.material, current.colors, current.requiredDate)
        if (errors.isNotEmpty()) {
            _state.value = current.copy(errors = errors)
            return
        }
        if (current.isSaving) return
        _state.value = current.copy(errors = emptyMap(), isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    repository.save(
                        QuoteRequest(
                            userId = userId,
                            productId = productId,
                            eventType = if (current.productName.isBlank()) current.eventType.trim() else "${current.productName} · ${current.eventType.trim()}",
                            dimensions = current.dimensions.trim(),
                            material = current.material.trim(),
                            colors = current.colors.trim(),
                            requiredDate = current.requiredDate.trim(),
                            observations = current.observations.trim(),
                            status = QuoteRequest.STATUS_PENDING,
                            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(Date())
                        )
                    )
                }
            }
            _state.value = result.fold(
                onSuccess = { savedId -> current.copy(isSaving = false, successMessage = "Solicitud #$savedId enviada correctamente") },
                onFailure = { current.copy(isSaving = false, errorMessage = it.message ?: "No se pudo guardar la solicitud") }
            )
        }
    }
}

class RequestsViewModel(
    private val userId: Int,
    private val isAdmin: Boolean,
    private val repository: RequestRepository
) : ViewModel() {
    private val _requests = MutableStateFlow(emptyList<QuoteRequest>())
    val requests: StateFlow<List<QuoteRequest>> = _requests.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _requests.value = withContext(Dispatchers.IO) {
                if (isAdmin) repository.getAllWithOwners().map { it.first } else repository.getForUser(userId)
            }
        }
    }

    fun updateStatus(id: Int, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStatus(id, status)
            refresh()
        }
    }
}
