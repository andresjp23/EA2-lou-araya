package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.data.local.SessionStore
import com.example.evaluacion2.data.local.UserPreferencesRepository
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.UserPreferences
import com.example.evaluacion2.domain.repository.UserRepository
import com.example.evaluacion2.util.Fixtures
import com.example.evaluacion2.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val users = mockk<UserRepository>()
    private val prefs = mockk<UserPreferencesRepository>(relaxed = true)
    private val session = mockk<SessionStore>(relaxed = true)
    private fun vm() = ProfileViewModel(2, users, prefs, session, mainRule.dispatcher)

    @Test
    fun cargaPerfilYGuardaPreferencias() {
        coEvery { users.getUser(2) } returns Fixtures.client
        every { prefs.get(2) } returns UserPreferences(2, false)
        val vm = vm().apply { load() }
        assertEquals(UiState.Success(Fixtures.client), vm.user.value)
        assertFalse(vm.notificationsEnabled.value)

        vm.setNotificationsEnabled(true)
        verify { prefs.save(UserPreferences(2, true)) }
        assertEquals("Preferencia de notificaciones guardada.", vm.message.value)
        vm.clearMessage()
        assertNull(vm.message.value)

        coEvery { users.getUser(2) } returns null
        assertTrue(vm().apply { load() }.user.value is UiState.Error)
    }

    @Test
    fun editarPerfilValidaGuardaYDetectaAliasOcupado() {
        val vm = vm()
        vm.updateProfile("", "a", "1")
        assertEquals(setOf("name", "alias", "phone"), vm.formErrors.value.keys)
        coVerify(exactly = 0) { users.updateProfile(any(), any(), any(), any()) }

        val updated = Fixtures.client.copy(name = "Nuevo Nombre")
        coEvery { users.updateProfile(2, "Nuevo Nombre", "cliente", "+56911111111") } returns updated
        vm.updateProfile("Nuevo Nombre", "cliente", "+56911111111")
        assertTrue(vm.saved.value)
        assertEquals(UiState.Success(updated), vm.user.value)
        verify { session.updateSessionProfile(updated) }
        vm.editHandled()
        assertFalse(vm.saved.value)

        coEvery { users.updateProfile(any(), any(), any(), any()) } throws AppException.Conflict("El alias ya está en uso")
        vm.updateProfile("Cliente", "tomado", "+56911111111")
        assertEquals("Ese alias ya está en uso", vm.formErrors.value["alias"])
    }
}
