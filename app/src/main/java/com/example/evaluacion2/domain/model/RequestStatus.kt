package com.example.evaluacion2.domain.model

/** Estados de una solicitud. Replica la regla de transiciones del microservicio de solicitudes. */
enum class RequestStatus(val label: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    COMPLETADA("Completada");

    val nextStatuses: Set<RequestStatus>
        get() = when (this) {
            PENDIENTE -> setOf(EN_REVISION, APROBADA, RECHAZADA)
            EN_REVISION -> setOf(APROBADA, RECHAZADA, PENDIENTE)
            APROBADA -> setOf(COMPLETADA, RECHAZADA)
            RECHAZADA, COMPLETADA -> emptySet()
        }

    val isFinal: Boolean get() = nextStatuses.isEmpty()
    val canBeCancelledByClient: Boolean get() = this == PENDIENTE

    fun canChangeTo(target: RequestStatus): Boolean = target in nextStatuses

    companion object {
        fun from(value: String?): RequestStatus = entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: PENDIENTE
    }
}
