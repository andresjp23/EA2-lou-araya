package com.example.evaluacion2.data.remote.dto

data class QuoteRequestDto(
    val id: Int? = null,
    val userId: Int,
    val userName: String? = "",
    val productId: Int? = null,
    val productName: String,
    val eventType: String,
    val dimensions: String,
    val material: String,
    val colors: String,
    val requiredDate: String,
    val observations: String? = "",
    val status: String? = null,
    val createdAt: String? = null
)

data class StatusRequestDto(val status: String)

/** Formato de error que devuelven los tres microservicios. */
data class ErrorDto(val status: Int? = null, val message: String? = null, val errors: Map<String, String>? = null)
