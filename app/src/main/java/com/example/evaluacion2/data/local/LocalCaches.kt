package com.example.evaluacion2.data.local

import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.model.User

/**
 * Caché local de productos. Guarda la última copia recibida del microservicio de catálogo
 * para que la app siga mostrando productos sin conexión y para que favoritos y QR funcionen offline.
 */
interface ProductCache {
    fun getAll(): List<Product>
    fun findById(productId: Int): Product?
    fun upsertAll(products: List<Product>)
    fun upsert(product: Product)
    fun delete(productId: Int)
}

/** Copia local del perfil de los usuarios que iniciaron sesión en este dispositivo (sin contraseña). */
interface UserCache {
    fun getById(userId: Int): User?
    fun upsert(user: User)
}

/** Sesión activa del usuario. La implementa [SessionManager] con SharedPreferences. */
interface SessionStore {
    fun saveSession(user: User)
    fun updateSessionProfile(user: User)
    fun isLoggedIn(): Boolean
    fun getUserId(): Int
    fun getUserName(): String?
    fun getUserRole(): String?
    fun clearSession()
}
