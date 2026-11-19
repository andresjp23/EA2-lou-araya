package com.example.evaluacion2.data.remote.dto

data class UserDto(
    val id: Int,
    val name: String,
    val alias: String,
    val email: String,
    val phone: String,
    val role: String,
    val avatarUri: String? = null
)

data class LoginRequestDto(val email: String, val password: String)

data class RegisterRequestDto(
    val name: String,
    val alias: String,
    val email: String,
    val phone: String,
    val password: String,
    val avatarUri: String? = null
)

data class UpdateProfileRequestDto(val name: String, val alias: String, val phone: String, val avatarUri: String? = null)
