package com.example.evaluacion2.domain.model

data class Product(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val dimensions: String = "",
    val materials: String = "",
    val category: String = "",
    val price: Int = 0,
    val available: Boolean = true,
    val imageUri: String? = null,
    val collection: String? = null
)
