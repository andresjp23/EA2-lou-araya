package com.example.evaluacion2.domain.validation

object ProductQrPayload {
    fun forProduct(productId: Int): String {
        require(productId > 0) { "El identificador del producto debe ser mayor que cero" }
        return "product:$productId"
    }

    fun parseProductId(payload: String?): Int? {
        val value = payload?.trim().orEmpty()
        val idText = if (value.startsWith("product:", ignoreCase = true)) {
            value.substringAfter(':').trim()
        } else {
            value
        }
        return idText.toIntOrNull()?.takeIf { it > 0 }
    }
}
