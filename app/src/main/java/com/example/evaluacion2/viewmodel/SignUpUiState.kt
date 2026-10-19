package com.example.evaluacion2.viewmodel

data class SignUpUiState(

    val name: String = "",
    val nameError: String? = null,

    val alias: String = "",
    val aliasError: String? = null,

    val email: String = "",
    val emailError: String? = null,

    val phone: String = "",
    val phoneError: String? = null,

    val password: String = "",
    val passwordError: String? = null,

    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,

    val generalError: String? = null,

    val isLoading: Boolean = false,

    val registrationSuccess: Boolean = false
)