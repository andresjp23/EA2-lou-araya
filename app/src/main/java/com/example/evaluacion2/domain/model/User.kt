package com.example.evaluacion2.domain.model

data class User(
    val id: Int = 0,
    val name: String,
    val alias: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "CLIENT"
)