package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.data.local.SessionStore
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.repository.UserRepository
import com.example.evaluacion2.util.Fixtures
import com.example.evaluacion2.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<UserRepository>()
    private val session = mockk<SessionStore>(relaxed = true)

    private fun vm(email: String = "cliente@louaraya.cl", password: String = "Cliente123") = SignInViewModel(repo, session).apply {
        onEmailChange(email)
        onPasswordChange(password)
    }

    @Test
    fun camposVaciosNoLlamanAlServidor() {
        val vm = vm(email = "", password = "")
        vm.login()
        assertEquals("El correo es obligatorio", vm.uiState.emailError)
        assertEquals("La contraseña es obligatoria", vm.uiState.passwordError)
        coVerify(exactly = 0) { repo.login(any(), any()) }
        vm.onEmailChange("a@b.cl")
        vm.onPasswordChange("x")
        assertNull(vm.uiState.emailError)
        assertNull(vm.uiState.passwordError)
    }

    @Test
    fun loginCorrectoGuardaLaSesion() {
        coEvery { repo.login("cliente@louaraya.cl", "Cliente123") } returns Fixtures.client
        val vm = vm()
        vm.loginAs("CLIENT")
        assertTrue(vm.uiState.loginSuccess)
        assertEquals("CLIENT", vm.uiState.userRole)
        verify { session.saveSession(Fixtures.client) }
        vm.loginHandled()
        assertFalse(vm.uiState.loginSuccess)
    }

    @Test
    fun erroresDeLogin() {
        coEvery { repo.login(any(), any()) } throws AppException.Unauthorized("x")
        val wrongPassword = vm().apply { login() }
        assertEquals("Correo o contraseña incorrectos", wrongPassword.uiState.generalError)

        coEvery { repo.login(any(), any()) } throws AppException.Network()
        assertTrue(vm().apply { login() }.uiState.generalError!!.contains("conectar"))

        coEvery { repo.login(any(), any()) } returns Fixtures.client
        val wrongRole = vm().apply { loginAs("ADMIN") }
        assertEquals("Esta cuenta no tiene el rol Administrador", wrongRole.uiState.generalError)
        verify(exactly = 0) { session.saveSession(any()) }
    }
}

class SignUpViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<UserRepository>()
    private val session = mockk<SessionStore>(relaxed = true)

    private fun filled() = SignUpViewModel(repo, session).apply {
        onNameChange("Cliente Demo")
        onAliasChange("cliente")
        onEmailChange("cliente@louaraya.cl")
        onPhoneChange("+56911111111")
        onPasswordChange("secreta1")
        onConfirmPasswordChange("secreta1")
        onAvatarUriChange("content://avatar")
    }

    @Test
    fun formularioVacioYRegistroExitoso() {
        val empty = SignUpViewModel(repo, session).apply { register() }
        with(empty.uiState) {
            assertNotNull(nameError); assertNotNull(aliasError); assertNotNull(emailError)
            assertNotNull(phoneError); assertNotNull(passwordError); assertNotNull(confirmPasswordError)
        }

        coEvery { repo.register(any()) } returns Fixtures.client
        val vm = filled().apply { register() }
        assertTrue(vm.uiState.registrationSuccess)
        coVerify { repo.register(match { it.avatarUri == "content://avatar" && it.role == "CLIENT" }) }
        verify { session.saveSession(Fixtures.client) }
        vm.registrationHandled()
        assertFalse(vm.uiState.registrationSuccess)
    }

    @Test
    fun erroresDelServidorSeMuestranEnCadaCampo() {
        coEvery { repo.register(any()) } throws AppException.Conflict("El correo ya está registrado")
        assertEquals("Este correo ya está registrado", filled().apply { register() }.uiState.emailError)

        coEvery { repo.register(any()) } throws AppException.Conflict("El alias ya está en uso")
        assertEquals("Este alias ya está registrado", filled().apply { register() }.uiState.aliasError)

        coEvery { repo.register(any()) } throws AppException.Validation("Datos inválidos", mapOf("phone" to "Teléfono inválido"))
        assertEquals("Teléfono inválido", filled().apply { register() }.uiState.phoneError)

        coEvery { repo.register(any()) } throws AppException.Network()
        val offline = filled().apply { register() }
        assertFalse(offline.uiState.isLoading)
        assertNotNull(offline.uiState.generalError)
    }
}
