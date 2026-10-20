package com.example.evaluacion2.domain.validation


object QuoteRequestValidator {
    fun validate(productType: String, eventType: String, dimensions: String, material: String, colors: String, requiredDate: String): Map<String, String> = buildMap {
        if (productType.isBlank()) put("productType", "Selecciona el tipo de producto")
        if (eventType.isBlank()) put("eventType", "Selecciona el tipo de evento")
        if (dimensions.isBlank()) put("dimensions", "Selecciona las dimensiones")
        if (material.isBlank()) put("material", "Selecciona un material")
        if (colors.isBlank()) put("colors", "Selecciona los colores")
        if (requiredDate.isBlank()) put("requiredDate", "Selecciona un plazo requerido")
        if (requiredDate.isNotBlank() && requiredDate.length < 8) put("requiredDate", "Ingresa una fecha o plazo válido")
    }
}
