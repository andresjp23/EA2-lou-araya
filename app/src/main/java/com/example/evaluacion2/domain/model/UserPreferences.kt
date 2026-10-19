package com.example.evaluacion2.domain.model

data class UserPreferences(
    val userId: Int,
    val notificationsEnabled: Boolean = true
)
