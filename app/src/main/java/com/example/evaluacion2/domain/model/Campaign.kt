package com.example.evaluacion2.domain.model

data class Campaign(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val startDate: String,
    val endDate: String,
    val active: Boolean = true
)
