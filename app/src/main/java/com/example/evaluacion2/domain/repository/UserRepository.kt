package com.example.evaluacion2.domain.repository

import com.example.evaluacion2.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(user: User): User
    suspend fun getUser(userId: Int): User?
    suspend fun updateProfile(userId: Int, name: String, alias: String, phone: String): User
}
