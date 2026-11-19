package com.example.evaluacion2.data.repository

import com.example.evaluacion2.data.local.ProductCache
import com.example.evaluacion2.data.remote.api.CatalogoApi
import com.example.evaluacion2.data.remote.dto.ToggleRequestDto
import com.example.evaluacion2.data.remote.requireSuccess
import com.example.evaluacion2.data.remote.safeApiCall
import com.example.evaluacion2.data.remote.toDomain
import com.example.evaluacion2.data.remote.toDto
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Productos desde ms-catalogo. Cada respuesta se guarda en la caché SQLite;
 * si no hay conexión, se devuelve la última copia guardada.
 */
class RemoteProductRepository(
    private val api: CatalogoApi,
    private val cache: ProductCache,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ProductRepository {

    override suspend fun getProducts(onlyAvailable: Boolean): List<Product> = try {
        val products = safeApiCall { api.getProducts() }.map { it.toDomain() }
        withContext(io) { cache.upsertAll(products) }
        if (onlyAvailable) products.filter { it.available } else products
    } catch (e: AppException.Network) {
        val cached = withContext(io) { cache.getAll() }
        if (cached.isEmpty()) throw e
        if (onlyAvailable) cached.filter { it.available } else cached
    }

    override suspend fun findById(productId: Int): Product? = try {
        safeApiCall { api.getProduct(productId) }.toDomain().also { withContext(io) { cache.upsert(it) } }
    } catch (e: AppException.NotFound) {
        withContext(io) { cache.delete(productId) }
        null
    } catch (e: AppException.Network) {
        withContext(io) { cache.findById(productId) }
    }

    override suspend fun findCached(productId: Int): Product? = withContext(io) { cache.findById(productId) }

    override suspend fun save(product: Product): Product {
        val saved = safeApiCall {
            if (product.id > 0) api.updateProduct(product.id, product.toDto()) else api.createProduct(product.toDto())
        }.toDomain()
        withContext(io) { cache.upsert(saved) }
        return saved
    }

    override suspend fun setAvailable(productId: Int, available: Boolean): Product {
        val updated = safeApiCall { api.setProductAvailable(productId, ToggleRequestDto(available)) }.toDomain()
        withContext(io) { cache.upsert(updated) }
        return updated
    }

    override suspend fun delete(productId: Int) {
        safeApiCall { api.deleteProduct(productId).requireSuccess() }
        withContext(io) { cache.delete(productId) }
    }
}
