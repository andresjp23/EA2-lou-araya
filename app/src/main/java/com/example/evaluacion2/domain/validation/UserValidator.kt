package com.example.evaluacion2.domain.validation


object UserValidator {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateName(name: String): String? {
        return when {
            name.isBlank() ->
                "El nombre es obligatorio"

            name.length < 3 ->
                "El nombre debe tener al menos 3 caracteres"

            else -> null
        }
    }

    fun validateAlias(alias: String): String? {
        return when {
            alias.isBlank() ->
                "El alias es obligatorio"

            alias.length < 3 ->
                "El alias debe tener al menos 3 caracteres"

            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() ->
                "El correo es obligatorio"

            !EMAIL_REGEX.matches(email.trim()) ->
                "Ingresa un correo válido"

            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() ->
                "El teléfono es obligatorio"

            phone.filter(Char::isDigit).length < 8 ->
                "Ingresa un teléfono válido"

            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() ->
                "La contraseña es obligatoria"

            password.length < 6 ->
                "Debe tener al menos 6 caracteres"

            else -> null
        }
    }

    fun validatePasswordConfirmation(
        password: String,
        confirmation: String
    ): String? {

        return when {
            confirmation.isBlank() ->
                "Confirma tu contraseña"

            password != confirmation ->
                "Las contraseñas no coinciden"

            else -> null
        }
    }
}