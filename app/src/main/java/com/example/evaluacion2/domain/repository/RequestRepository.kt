package com.example.evaluacion2.domain.repository

import com.example.evaluacion2.domain.model.QuoteRequest

interface RequestRepository {
    suspend fun getForUser(userId: Int): List<QuoteRequest>
    suspend fun getAll(): List<QuoteRequest>
    suspend fun create(request: QuoteRequest): QuoteRequest
    suspend fun updateStatus(requestId: Int, status: String): QuoteRequest
    suspend fun delete(requestId: Int)
}
