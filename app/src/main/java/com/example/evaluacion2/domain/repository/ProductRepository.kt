package com.example.evaluacion2.domain.repository

import com.example.evaluacion2.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(onlyAvailable: Boolean = false): List<Product>
    suspend fun findById(productId: Int): Product?
    /** Busca solo en la caché local (rápido, sin red). Lo usa el escáner QR. */
    suspend fun findCached(productId: Int): Product?
    suspend fun save(product: Product): Product
    suspend fun setAvailable(productId: Int, available: Boolean): Product
    suspend fun delete(productId: Int)
}
