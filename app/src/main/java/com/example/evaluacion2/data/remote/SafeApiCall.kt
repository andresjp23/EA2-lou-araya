package com.example.evaluacion2.data.remote

import com.example.evaluacion2.data.remote.dto.ErrorDto
import com.example.evaluacion2.domain.error.AppException
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

private val gson = Gson()

/**
 * Ejecuta una llamada a un microservicio y traduce cualquier fallo a un [AppException]
 * con un mensaje entendible para el usuario.
 */
suspend fun <T> safeApiCall(call: suspend () -> T): T = try {
    call()
} catch (e: CancellationException) {
    throw e
} catch (e: AppException) {
    throw e
} catch (e: HttpException) {
    throw mapHttpError(e.code(), e.response()?.errorBody()?.string())
} catch (e: IOException) {
    throw AppException.Network(e)
} catch (e: Exception) {
    throw AppException.Unknown(e)
}

/** Para endpoints DELETE que devuelven 204 sin cuerpo. */
fun Response<Unit>.requireSuccess() {
    if (!isSuccessful) throw mapHttpError(code(), errorBody()?.string())
}

fun mapHttpError(code: Int, body: String?): AppException {
    val error = runCatching { gson.fromJson(body, ErrorDto::class.java) }.getOrNull()
    val message = error?.message?.takeIf { it.isNotBlank() } ?: "Error del servidor ($code)"
    return when (code) {
        400 -> AppException.Validation(message, error?.errors.orEmpty())
        401 -> AppException.Unauthorized(message)
        404 -> AppException.NotFound(message)
        409 -> AppException.Conflict(message)
        422 -> AppException.BusinessRule(message)
        else -> AppException.Server(code, message)
    }
}
