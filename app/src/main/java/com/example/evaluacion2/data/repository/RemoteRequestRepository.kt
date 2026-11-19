package com.example.evaluacion2.data.repository

import com.example.evaluacion2.data.remote.api.SolicitudesApi
import com.example.evaluacion2.data.remote.dto.StatusRequestDto
import com.example.evaluacion2.data.remote.requireSuccess
import com.example.evaluacion2.data.remote.safeApiCall
import com.example.evaluacion2.data.remote.toDomain
import com.example.evaluacion2.data.remote.toDto
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.repository.RequestRepository

/** Solicitudes de cotización desde ms-solicitudes. */
class RemoteRequestRepository(private val api: SolicitudesApi) : RequestRepository {

    override suspend fun getForUser(userId: Int): List<QuoteRequest> =
        safeApiCall { api.getRequests(userId) }.map { it.toDomain() }

    override suspend fun getAll(): List<QuoteRequest> =
        safeApiCall { api.getRequests() }.map { it.toDomain() }

    override suspend fun create(request: QuoteRequest): QuoteRequest =
        safeApiCall { api.createRequest(request.toDto()) }.toDomain()

    override suspend fun updateStatus(requestId: Int, status: String): QuoteRequest =
        safeApiCall { api.updateStatus(requestId, StatusRequestDto(status)) }.toDomain()

    override suspend fun delete(requestId: Int) {
        safeApiCall { api.deleteRequest(requestId).requireSuccess() }
    }
}
