package com.example.evaluacion2.domain.model

data class QuoteRequest(
    val id: Int = 0,
    val userId: Int,
    val userName: String = "",
    val productId: Int? = null,
    val productName: String = "",
    val eventType: String = "",
    val dimensions: String = "",
    val material: String = "",
    val colors: String = "",
    val requiredDate: String = "",
    val observations: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: String = ""
) {
    val requestStatus: RequestStatus get() = RequestStatus.from(status)

    companion object {
        const val STATUS_PENDING = "PENDIENTE"
    }
}
