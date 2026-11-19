package com.example.evaluacion2.data.remote.api

import com.example.evaluacion2.data.remote.dto.QuoteRequestDto
import com.example.evaluacion2.data.remote.dto.StatusRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Microservicio ms-solicitudes (puerto 8083): solicitudes de cotización. */
interface SolicitudesApi {
    @GET("api/solicitudes")
    suspend fun getRequests(@Query("userId") userId: Int? = null): List<QuoteRequestDto>

    @POST("api/solicitudes")
    suspend fun createRequest(@Body body: QuoteRequestDto): QuoteRequestDto

    @PATCH("api/solicitudes/{id}/estado")
    suspend fun updateStatus(@Path("id") id: Int, @Body body: StatusRequestDto): QuoteRequestDto

    @DELETE("api/solicitudes/{id}")
    suspend fun deleteRequest(@Path("id") id: Int): Response<Unit>
}
