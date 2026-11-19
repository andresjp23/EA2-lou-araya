package com.example.evaluacion2.data

import com.example.evaluacion2.data.remote.dto.CampaignDto
import com.example.evaluacion2.data.remote.dto.ProductDto
import com.example.evaluacion2.data.remote.dto.QuoteRequestDto
import com.example.evaluacion2.data.remote.dto.UserDto
import com.example.evaluacion2.data.remote.mapHttpError
import com.example.evaluacion2.data.remote.requireSuccess
import com.example.evaluacion2.data.remote.safeApiCall
import com.example.evaluacion2.data.remote.toDomain
import com.example.evaluacion2.data.remote.toDto
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.util.Fixtures
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiErrorsAndMappersTest {

    private fun httpError(code: Int, body: String) =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    private suspend fun errorOf(block: suspend () -> Unit): AppException? =
        runCatching { safeApiCall { block() } }.exceptionOrNull() as? AppException

    /** Cada código HTTP de los microservicios se convierte en su tipo de AppException. */
    @Test
    fun codigosHttpSeTraducenAExcepciones() = runTest {
        assertEquals(5, safeApiCall { 5 })
        val json = """{"status":0,"message":"Mensaje del servidor","errors":{"email":"Correo inválido"}}"""
        val validation = errorOf { throw httpError(400, json) } as AppException.Validation
        assertEquals("Mensaje del servidor", validation.message)
        assertEquals("Correo inválido", validation.fieldErrors["email"])
        assertTrue(errorOf { throw httpError(401, json) } is AppException.Unauthorized)
        assertTrue(errorOf { throw httpError(404, json) } is AppException.NotFound)
        assertTrue(errorOf { throw httpError(409, json) } is AppException.Conflict)
        assertTrue(errorOf { throw httpError(422, json) } is AppException.BusinessRule)
        val server = errorOf { throw httpError(500, "no es json") } as AppException.Server
        assertEquals("Error del servidor (500)", server.message)
    }

    @Test
    fun erroresDeRedYRespuestasSinCuerpo() = runTest {
        assertTrue(errorOf { throw IOException("sin red") } is AppException.Network)
        assertTrue(errorOf { throw IllegalStateException() } is AppException.Unknown)
        assertTrue(errorOf { throw AppException.NotFound("x") } is AppException.NotFound)
        Response.success(Unit).requireSuccess()
        val error = runCatching { Response.error<Unit>(404, "{}".toResponseBody(null)).requireSuccess() }.exceptionOrNull()
        assertTrue(error is AppException.NotFound)
        assertTrue(mapHttpError(503, null) is AppException.Server)
    }

    @Test
    fun conversionEntreDtoYModelo() {
        assertEquals("ADMIN", UserDto(1, "Lou", "lou", "lou@mail.cl", "+56912345678", "ADMIN").toDomain().role)

        val product = ProductDto(name = "Tapiz", description = null, dimensions = null, materials = null, category = "Interiores", price = 10, available = null).toDomain()
        assertEquals(0, product.id)
        assertEquals("", product.description)
        assertTrue(product.available)
        assertNull(Fixtures.product(0).toDto().id)

        val campaign = CampaignDto(title = "Verano", description = null, startDate = "2026-01-01", endDate = "2026-02-01", active = null).toDomain()
        assertEquals("", campaign.description)
        assertNull(Fixtures.campaign.copy(id = 0).toDto().id)

        assertNull(Fixtures.request(9).toDto().id)
        val request = QuoteRequestDto(id = 9, userId = 2, userName = null, productName = "Tapiz", eventType = "Otro", dimensions = "1",
            material = "Yute", colors = "Rojo", requiredDate = "2026-12-01", observations = null, status = null, createdAt = null).toDomain()
        assertEquals("PENDIENTE", request.status)
        assertEquals("", request.userName)
    }
}
