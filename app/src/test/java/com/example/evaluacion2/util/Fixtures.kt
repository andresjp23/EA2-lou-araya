package com.example.evaluacion2.util

import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.model.User

/** Datos de prueba reutilizados por los tests. */
object Fixtures {
    val client = User(id = 2, name = "Cliente Demo", alias = "cliente", email = "cliente@louaraya.cl", phone = "+56911111111", role = User.ROLE_CLIENT)
    val admin = User(id = 1, name = "Administrador", alias = "admin", email = "admin@louaraya.cl", phone = "+56900000000", role = User.ROLE_ADMIN)

    fun product(id: Int, name: String = "Producto $id", available: Boolean = true, category: String = "Interiores") =
        Product(id = id, name = name, category = category, price = 1000 * id, available = available)

    val campaign = Campaign(id = 3, title = "Bodas de Ensueño", description = "Diseños", startDate = "2026-01-01", endDate = "2026-12-31", active = true)

    fun request(id: Int, userId: Int = 2, status: String = "PENDIENTE") = QuoteRequest(
        id = id, userId = userId, userName = "Cliente Demo", productId = 1, productName = "Arco de ceremonia",
        eventType = "Matrimonio", dimensions = "2 x 3 m", material = "Algodón", colors = "Blanco",
        requiredDate = "2026-12-20", status = status, createdAt = "2026-11-19 10:00:00"
    )
}
