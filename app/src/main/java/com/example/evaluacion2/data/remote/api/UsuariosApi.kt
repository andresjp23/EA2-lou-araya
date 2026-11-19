package com.example.evaluacion2.data.remote.api

import com.example.evaluacion2.data.remote.dto.LoginRequestDto
import com.example.evaluacion2.data.remote.dto.RegisterRequestDto
import com.example.evaluacion2.data.remote.dto.UpdateProfileRequestDto
import com.example.evaluacion2.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Microservicio ms-usuarios (puerto 8081). */
interface UsuariosApi {
    @POST("api/usuarios/login")
    suspend fun login(@Body body: LoginRequestDto): UserDto

    @POST("api/usuarios/registro")
    suspend fun register(@Body body: RegisterRequestDto): UserDto

    @GET("api/usuarios/{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    @PUT("api/usuarios/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body body: UpdateProfileRequestDto): UserDto
}
