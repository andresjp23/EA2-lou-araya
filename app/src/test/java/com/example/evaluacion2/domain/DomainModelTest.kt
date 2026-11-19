package com.example.evaluacion2.domain

import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.RequestStatus
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.model.UserRole
import com.example.evaluacion2.util.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {

    /** Polimorfismo: cada rol responde distinto a las mismas preguntas. */
    @Test
    fun rolesYSusPrivilegios() {
        val admin: UserRole = UserRole.Admin
        val client: UserRole = UserRole.Client
        assertTrue(admin.canManageCatalog && admin.canManageRequests && !admin.canCreateRequests && !admin.seesOnlyActiveCampaigns)
        assertTrue(!client.canManageCatalog && !client.canManageRequests && client.canCreateRequests && client.seesOnlyActiveCampaigns)
        assertEquals("Administrador", admin.label)
        assertEquals(UserRole.Admin, UserRole.from("admin"))
        assertEquals(UserRole.Client, UserRole.from(null))
        assertEquals(UserRole.Admin, Fixtures.admin.userRole)
        assertEquals(UserRole.Client, User(name = "a", alias = "b", email = "c", phone = "d").userRole)
    }

    @Test
    fun estadosDeSolicitudYExcepciones() {
        assertTrue(RequestStatus.PENDIENTE.canChangeTo(RequestStatus.EN_REVISION))
        assertTrue(RequestStatus.EN_REVISION.canChangeTo(RequestStatus.PENDIENTE))
        assertTrue(RequestStatus.APROBADA.canChangeTo(RequestStatus.COMPLETADA))
        assertFalse(RequestStatus.PENDIENTE.canChangeTo(RequestStatus.COMPLETADA))
        assertTrue(RequestStatus.RECHAZADA.isFinal && RequestStatus.COMPLETADA.isFinal)
        assertTrue(RequestStatus.PENDIENTE.canBeCancelledByClient)
        assertFalse(RequestStatus.APROBADA.canBeCancelledByClient)
        assertEquals(RequestStatus.APROBADA, RequestStatus.from("aprobada"))
        assertEquals(RequestStatus.PENDIENTE, RequestStatus.from(null))
        assertEquals(RequestStatus.COMPLETADA, Fixtures.request(1, status = "COMPLETADA").requestStatus)

        assertTrue(AppException.Network().message!!.contains("conectar"))
        assertTrue(AppException.Unknown().message!!.contains("inesperado"))
        assertEquals("Correo malo", AppException.Validation("x", mapOf("email" to "Correo malo")).fieldErrors["email"])
        assertEquals(500, AppException.Server(500, "x").code)
    }
}
