package com.example.evaluacion2.domain.model

/**
 * Rol del usuario. Cada rol define sus propios privilegios (polimorfismo):
 * la UI pregunta al rol qué puede hacer en vez de comparar textos.
 */
sealed class UserRole(val code: String, val label: String) {
    abstract val canManageCatalog: Boolean
    abstract val canManageRequests: Boolean
    abstract val canCreateRequests: Boolean
    abstract val seesOnlyActiveCampaigns: Boolean

    data object Client : UserRole(User.ROLE_CLIENT, "Cliente") {
        override val canManageCatalog = false
        override val canManageRequests = false
        override val canCreateRequests = true
        override val seesOnlyActiveCampaigns = true
    }

    data object Admin : UserRole(User.ROLE_ADMIN, "Administrador") {
        override val canManageCatalog = true
        override val canManageRequests = true
        override val canCreateRequests = false
        override val seesOnlyActiveCampaigns = false
    }

    companion object {
        fun from(code: String?): UserRole = if (code.equals(Admin.code, ignoreCase = true)) Admin else Client
    }
}
