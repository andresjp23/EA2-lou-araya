package com.example.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evaluacion2.data.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(
    private val userId: Int,
    private val productId: Int,
    private val repository: FavoriteRepository
) : ViewModel() {
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    private val _isHomeFavorite = MutableStateFlow(false)
    val isHomeFavorite: StateFlow<Boolean> = _isHomeFavorite.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val values = withContext(Dispatchers.IO) {
                repository.isHomeFavorite(userId, productId) to repository.getForUser(userId).any { it.productId == productId }
            }
            _isHomeFavorite.value = values.first
            _isFavorite.value = values.second
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isFavorite.value) repository.remove(userId, productId) else repository.add(userId, productId)
            refresh()
        }
    }

    fun toggleHomeFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleHomeFavorite(userId, productId)
            refresh()
        }
    }
}
