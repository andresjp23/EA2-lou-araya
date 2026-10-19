package com.example.evaluacion2.domain.validation

import com.example.evaluacion2.domain.model.Product

object ProductValidator {
    fun validate(product: Product): Map<String, String> = buildMap {
        if (product.name.isBlank()) put("name", "Ingresa el nombre del producto")
        if (product.category.isBlank()) put("category", "Ingresa una categoría")
        if (product.price < 0) put("price", "El precio no puede ser negativo")
    }
}
