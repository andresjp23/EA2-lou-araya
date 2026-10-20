package com.example.evaluacion2

import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.validation.ProductQrPayload
import com.example.evaluacion2.domain.validation.ProductValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductFeatureValidationTest {
    @Test
    fun parsesProductQrFormats() {
        assertEquals("product:42", ProductQrPayload.forProduct(42))
        assertEquals(42, ProductQrPayload.parseProductId("product:42"))
        assertEquals(42, ProductQrPayload.parseProductId(" 42 "))
        assertNull(ProductQrPayload.parseProductId("product:0"))
        assertNull(ProductQrPayload.parseProductId("https://example.com/product/42"))
    }

    @Test
    fun validatesRequiredAdminProductFieldsAndPrice() {
        val errors = ProductValidator.validate(Product(name = "", category = "", price = -1))
        assertEquals("Ingresa el nombre del producto", errors["name"])
        assertEquals("Ingresa una categoría", errors["category"])
        assertEquals("El precio no puede ser negativo", errors["price"])
        assertEquals(emptyMap<String, String>(), ProductValidator.validate(Product(name = "Alfombra", category = "Hogar", price = 100)))
    }
}
