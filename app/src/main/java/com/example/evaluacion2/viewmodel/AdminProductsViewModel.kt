package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.repository.ProductRepository
import com.example.evaluacion2.domain.validation.ProductValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Gestión de productos del administrador: crear, editar, activar/desactivar y eliminar. */
class AdminProductsViewModel(private val repository: ProductRepository) : BaseViewModel() {
    private val _products = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val products: StateFlow<UiState<List<Product>>> = _products.asStateFlow()

    private val _formErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val formErrors: StateFlow<Map<String, String>> = _formErrors.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun refresh() {
        load(_products) { repository.getProducts() }
    }

    fun save(product: Product) {
        val errors = ProductValidator.validate(product)
        _formErrors.value = errors
        _saved.value = false
        if (errors.isNotEmpty()) return
        launchSafe {
            repository.save(product)
            _saved.value = true
            showMessage(if (product.id == 0) "Producto creado" else "Producto actualizado")
            refresh()
        }
    }

    fun toggleAvailable(product: Product) {
        launchSafe {
            repository.setAvailable(product.id, !product.available)
            refresh()
        }
    }

    fun delete(product: Product) {
        launchSafe {
            repository.delete(product.id)
            showMessage("Producto eliminado")
            refresh()
        }
    }

    fun formHandled() {
        _saved.value = false
        _formErrors.value = emptyMap()
    }

    companion object {
        val FILTERS = listOf("Todos", "Activos", "Inactivos")

        fun filter(products: List<Product>, query: String, filter: String): List<Product> = products.filter {
            it.name.contains(query.trim(), ignoreCase = true) &&
                (filter == "Todos" || (filter == "Activos") == it.available)
        }
    }
}
