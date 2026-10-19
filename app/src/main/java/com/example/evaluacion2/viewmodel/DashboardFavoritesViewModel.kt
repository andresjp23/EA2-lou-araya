package com.example.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion2.data.repository.FavoriteRepository
import com.example.evaluacion2.data.repository.ProductRepository
import com.example.evaluacion2.domain.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFavoritesViewModel(
    private val userId: Int,
    private val favoriteRepository: FavoriteRepository,
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _favorites = MutableStateFlow(emptyList<Product>())
    val favorites: StateFlow<List<Product>> = _favorites.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _favorites.value = withContext(Dispatchers.IO) {
                favoriteRepository.getHomeFavorites(userId)
                    .map { it.productId }
                    .distinct()
                    .mapNotNull(productRepository::findById)
            }
        }
    }
}
