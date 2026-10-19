package com.example.evaluacion2.domain.model

data class FairEvent(
    val id: Int = 0,
    val name: String,
    val place: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: String
)
