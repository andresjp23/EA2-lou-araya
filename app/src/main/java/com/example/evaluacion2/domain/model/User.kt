package com.example.evaluacion2.domain.model

data class User(
    val id: Int = 0,
    val name: String,
    val alias: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = ROLE_CLIENT,
    val avatarUri: String? = null
) {
    companion object {
        const val ROLE_CLIENT = "CLIENT"
        const val ROLE_ADMIN = "ADMIN"
    }
}
