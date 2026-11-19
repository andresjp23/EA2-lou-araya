package com.example.evaluacion2.data.remote.api

import com.example.evaluacion2.data.remote.dto.CampaignDto
import com.example.evaluacion2.data.remote.dto.ProductDto
import com.example.evaluacion2.data.remote.dto.ToggleRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Microservicio ms-catalogo (puerto 8082): productos y campañas. */
interface CatalogoApi {
    @GET("api/productos")
    suspend fun getProducts(@Query("disponibles") onlyAvailable: Boolean = false): List<ProductDto>

    @GET("api/productos/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @POST("api/productos")
    suspend fun createProduct(@Body body: ProductDto): ProductDto

    @PUT("api/productos/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body body: ProductDto): ProductDto

    @PATCH("api/productos/{id}/disponibilidad")
    suspend fun setProductAvailable(@Path("id") id: Int, @Body body: ToggleRequestDto): ProductDto

    @DELETE("api/productos/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>

    @GET("api/campanas")
    suspend fun getCampaigns(@Query("activas") onlyActive: Boolean = false): List<CampaignDto>

    @POST("api/campanas")
    suspend fun createCampaign(@Body body: CampaignDto): CampaignDto

    @PUT("api/campanas/{id}")
    suspend fun updateCampaign(@Path("id") id: Int, @Body body: CampaignDto): CampaignDto

    @PATCH("api/campanas/{id}/estado")
    suspend fun setCampaignActive(@Path("id") id: Int, @Body body: ToggleRequestDto): CampaignDto

    @DELETE("api/campanas/{id}")
    suspend fun deleteCampaign(@Path("id") id: Int): Response<Unit>
}
