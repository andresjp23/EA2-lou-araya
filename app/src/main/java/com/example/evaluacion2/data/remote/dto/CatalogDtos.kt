package com.example.evaluacion2.data.remote.dto

data class ProductDto(
    val id: Int? = null,
    val name: String,
    val description: String? = "",
    val dimensions: String? = "",
    val materials: String? = "",
    val category: String,
    val price: Int,
    val available: Boolean? = true,
    val imageUri: String? = null,
    val collection: String? = null
)

data class CampaignDto(
    val id: Int? = null,
    val title: String,
    val description: String? = "",
    val startDate: String,
    val endDate: String,
    val active: Boolean? = true
)

/** Cuerpo usado por los endpoints PATCH que activan o desactivan algo. */
data class ToggleRequestDto(val value: Boolean)
