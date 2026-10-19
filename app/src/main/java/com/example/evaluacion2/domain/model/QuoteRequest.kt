package com.example.evaluacion2.domain.model

data class QuoteRequest(
    val id: Int = 0,
    val userId: Int,
    val productId: Int? = null,
    val eventType: String = "",
    val dimensions: String = "",
    val material: String = "",
    val colors: String = "",
    val requiredDate: String = "",
    val observations: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: String
) {
    companion object {
        const val STATUS_PENDING = "PENDIENTE"
        const val STATUS_IN_REVIEW = "EN_REVISION"
        const val STATUS_APPROVED = "APROBADA"
        const val STATUS_REJECTED = "RECHAZADA"
        const val STATUS_COMPLETED = "COMPLETADA"
        val ALL_STATUSES = setOf(STATUS_PENDING, STATUS_IN_REVIEW, STATUS_APPROVED, STATUS_REJECTED, STATUS_COMPLETED)
    }
}
