package com.example.evaluacion2.domain.error

/**
 * Jerarquía de errores de la app. Cada subclase representa un tipo de fallo distinto
 * y la UI muestra el mensaje sin necesidad de conocer detalles de HTTP o de red.
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** El servidor no respondió (microservicio apagado, sin internet, timeout). */
    class Network(cause: Throwable? = null) :
        AppException("No se pudo conectar con el servidor. Revisa tu conexión e inténtalo de nuevo.", cause)

    class Unauthorized(message: String) : AppException(message)
    class NotFound(message: String) : AppException(message)
    class Conflict(message: String) : AppException(message)
    class BusinessRule(message: String) : AppException(message)

    /** Datos rechazados por el backend, con el error de cada campo. */
    class Validation(message: String, val fieldErrors: Map<String, String>) : AppException(message)

    class Server(val code: Int, message: String) : AppException(message)

    class Unknown(cause: Throwable? = null) :
        AppException("Ocurrió un error inesperado. Inténtalo nuevamente.", cause)
}
