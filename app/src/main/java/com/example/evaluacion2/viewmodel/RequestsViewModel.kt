package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.model.RequestStatus
import com.example.evaluacion2.domain.model.UserRole
import com.example.evaluacion2.domain.repository.RequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Solicitudes de cotización. El administrador ve todas y cambia su estado;
 * el cliente ve las suyas y puede cancelar las pendientes.
 */
class RequestsViewModel(
    private val userId: Int,
    private val role: UserRole,
    private val repository: RequestRepository
) : BaseViewModel() {
    private val _requests = MutableStateFlow<UiState<List<QuoteRequest>>>(UiState.Loading)
    val requests: StateFlow<UiState<List<QuoteRequest>>> = _requests.asStateFlow()

    fun refresh() {
        load(_requests) { if (role.canManageRequests) repository.getAll() else repository.getForUser(userId) }
    }

    fun updateStatus(request: QuoteRequest, target: RequestStatus) {
        if (!role.canManageRequests) return
        if (!request.requestStatus.canChangeTo(target)) {
            showMessage("No se puede pasar de ${request.requestStatus.label} a ${target.label}")
            return
        }
        launchSafe {
            repository.updateStatus(request.id, target.name)
            showMessage("Solicitud #${request.id}: ${target.label}")
            refresh()
        }
    }

    fun cancel(request: QuoteRequest) {
        if (request.userId != userId || !request.requestStatus.canBeCancelledByClient) {
            showMessage("Solo puedes cancelar tus solicitudes pendientes")
            return
        }
        launchSafe {
            repository.delete(request.id)
            showMessage("Solicitud #${request.id} cancelada")
            refresh()
        }
    }

    companion object {
        val FILTERS = listOf("Todas") + RequestStatus.entries.map { it.label }

        fun filter(requests: List<QuoteRequest>, filter: String): List<QuoteRequest> =
            if (filter == "Todas") requests else requests.filter { it.requestStatus.label == filter }
    }
}
