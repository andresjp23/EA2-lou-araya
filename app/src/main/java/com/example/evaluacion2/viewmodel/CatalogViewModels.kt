package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.data.local.FavoriteRepository
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.repository.ProductRepository
import com.example.evaluacion2.domain.repository.RequestRepository
import com.example.evaluacion2.domain.validation.QuoteRequestValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Catálogo completo de productos (ms-catalogo, con caché local si no hay conexión). */
class CatalogViewModel(private val repository: ProductRepository) : BaseViewModel() {
    private val _products = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val products: StateFlow<UiState<List<Product>>> = _products.asStateFlow()

    fun refresh() {
        load(_products) { repository.getProducts() }
    }

    companion object {
        val CATEGORIES = listOf("Todos", "Matrimonios", "Festivales", "Interiores")

        /** Filtro del catálogo por texto y categoría (lógica separada de la pantalla). */
        fun filter(products: List<Product>, search: String, category: String): List<Product> = products.filter { product ->
            val matchesCategory = category == "Todos" || product.category.contains(category, ignoreCase = true)
            matchesCategory && product.name.contains(search.trim(), ignoreCase = true)
        }
    }
}

/** Favoritos del usuario: los ids se guardan en SQLite y los datos del producto vienen del catálogo. */
class FavoritesViewModel(
    private val userId: Int,
    private val favorites: FavoriteRepository,
    private val products: ProductRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {
    private val _products = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val favoriteProducts: StateFlow<UiState<List<Product>>> = _products.asStateFlow()

    fun refresh() {
        load(_products) {
            val ids = withContext(io) { favorites.getForUser(userId).map { it.productId }.distinct() }
            val catalog = products.getProducts().associateBy { it.id }
            ids.mapNotNull { catalog[it] }
        }
    }
}

/** Pantalla de inicio del cliente: muestra favoritos y productos destacados. */
class DashboardViewModel(
    private val userId: Int,
    private val favorites: FavoriteRepository,
    private val products: ProductRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {
    private val _featured = MutableStateFlow<List<Product>>(emptyList())
    val featured: StateFlow<List<Product>> = _featured.asStateFlow()

    fun refresh() {
        launchSafe {
            val homeIds = withContext(io) { favorites.getHomeFavorites(userId).map { it.productId } }
            val catalog = products.getProducts()
            _featured.value = featuredProducts(homeIds, catalog)
        }
    }

    companion object {
        /** Primero los favoritos marcados para el inicio y luego los disponibles, sin repetir, máximo [limit]. */
        fun featuredProducts(homeFavoriteIds: List<Int>, catalog: List<Product>, limit: Int = 4): List<Product> {
            val byId = catalog.associateBy { it.id }
            return (homeFavoriteIds.mapNotNull { byId[it] } + catalog.filter { it.available })
                .distinctBy { it.id }
                .take(limit)
        }
    }
}

/** Detalle de un producto y su estado de favorito. */
class DetailViewModel(
    private val userId: Int,
    private val productId: Int,
    private val favorites: FavoriteRepository,
    private val products: ProductRepository,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : BaseViewModel() {
    private val _product = MutableStateFlow<UiState<Product?>>(UiState.Loading)
    val product: StateFlow<UiState<Product?>> = _product.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun refresh() {
        load(_product) { products.findById(productId) }
        launchSafe { _isFavorite.value = withContext(io) { isFavoriteNow() } }
    }

    fun toggleFavorite() {
        launchSafe {
            _isFavorite.value = withContext(io) {
                if (isFavoriteNow()) favorites.remove(userId, productId) else favorites.add(userId, productId)
                isFavoriteNow()
            }
            showMessage(if (_isFavorite.value) "Agregado a favoritos" else "Quitado de favoritos")
        }
    }

    private fun isFavoriteNow() = favorites.getForUser(userId).any { it.productId == productId }
}

data class QuoteFormState(
    val productType: String = "",
    val productOptions: List<String> = emptyList(),
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

/** Formulario de solicitud de cotización (crea la solicitud en ms-solicitudes). */
class QuoteViewModel(
    private val userId: Int,
    private val userName: String,
    private val initialProductId: Int?,
    private val repository: RequestRepository,
    private val productRepository: ProductRepository
) : BaseViewModel() {
    private val _state = MutableStateFlow(QuoteFormState())
    val state: StateFlow<QuoteFormState> = _state.asStateFlow()
    private var products: List<Product> = emptyList()

    init {
        launchSafe(onError = { _state.value = _state.value.copy(errorMessage = it.message) }) {
            products = productRepository.getProducts()
            val initial = initialProductId?.let { id -> products.firstOrNull { it.id == id } }
            _state.value = _state.value.copy(
                productType = initial?.name ?: _state.value.productType,
                productOptions = products.map { it.name }.distinct()
            )
        }
    }

    fun setProductType(value: String) = update { copy(productType = value) }
    fun setEventType(value: String) = update { copy(eventType = value) }
    fun setDimensions(value: String) = update { copy(dimensions = value) }
    fun setMaterial(value: String) = update { copy(material = value) }
    fun setColors(value: String) = update { copy(colors = value) }
    fun setRequiredDate(value: String) = update { copy(requiredDate = value) }
    fun setObservations(value: String) = update { copy(observations = value) }

    private fun update(transform: QuoteFormState.() -> QuoteFormState) {
        val next = _state.value.transform()
        // Al corregir un campo se borra solo su error.
        val fixed = next.errors.filterKeys { key -> fieldValue(next, key).isBlank() }
        _state.value = next.copy(errors = fixed, successMessage = null, errorMessage = null)
    }

    private fun fieldValue(s: QuoteFormState, key: String) = when (key) {
        "productType" -> s.productType
        "eventType" -> s.eventType
        "dimensions" -> s.dimensions
        "material" -> s.material
        "colors" -> s.colors
        "requiredDate" -> s.requiredDate
        else -> ""
    }

    fun submit() {
        val current = _state.value
        if (current.isSaving) return
        val errors = QuoteRequestValidator.validate(current.productType, current.eventType, current.dimensions, current.material, current.colors, current.requiredDate)
        if (errors.isNotEmpty()) {
            _state.value = current.copy(errors = errors)
            return
        }
        _state.value = current.copy(errors = emptyMap(), isSaving = true, errorMessage = null)
        launchSafe(onError = { _state.value = _state.value.copy(isSaving = false, errorMessage = it.message) }) {
            val saved = repository.create(
                QuoteRequest(
                    userId = userId,
                    userName = userName,
                    productId = products.firstOrNull { it.name == current.productType }?.id,
                    productName = current.productType.trim(),
                    eventType = current.eventType.trim(),
                    dimensions = current.dimensions.trim(),
                    material = current.material.trim(),
                    colors = current.colors.trim(),
                    requiredDate = current.requiredDate.trim(),
                    observations = current.observations.trim()
                )
            )
            _state.value = _state.value.copy(isSaving = false, successMessage = "Solicitud #${saved.id} enviada correctamente")
        }
    }
}
