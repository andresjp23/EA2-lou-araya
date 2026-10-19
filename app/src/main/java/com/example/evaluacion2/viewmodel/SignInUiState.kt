package com.example.evaluacion2.viewmodel

data class SignInUiState(

    val email: String = "",
    val emailError: String? = null,

    val password: String = "",
    val passwordError: String? = null,

    val generalError: String? = null,

    val isLoading: Boolean = false,

    val loginSuccess: Boolean = false,

    val userRole: String? = null
)