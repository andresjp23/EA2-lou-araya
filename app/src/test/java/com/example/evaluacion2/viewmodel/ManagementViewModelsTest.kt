package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.RequestStatus
import com.example.evaluacion2.domain.model.UserRole
import com.example.evaluacion2.domain.repository.CampaignRepository
import com.example.evaluacion2.domain.repository.ProductRepository
import com.example.evaluacion2.domain.repository.RequestRepository
import com.example.evaluacion2.util.Fixtures
import com.example.evaluacion2.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RequestsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<RequestRepository>(relaxed = true)

    @Test
    fun adminVeTodasYCambiaEstadosValidos() {
        coEvery { repo.getAll() } returns listOf(Fixtures.request(1), Fixtures.request(2, userId = 5))
        coEvery { repo.getForUser(2) } returns listOf(Fixtures.request(1))
        assertEquals(1, RequestsViewModel(2, UserRole.Client, repo).apply { refresh() }.requests.value.dataOrNull?.size)
        val admin = RequestsViewModel(1, UserRole.Admin, repo).apply { refresh() }
        assertEquals(2, admin.requests.value.dataOrNull?.size)

        admin.updateStatus(Fixtures.request(1), RequestStatus.APROBADA)
        coVerify { repo.updateStatus(1, "APROBADA") }
        assertEquals("Solicitud #1: Aprobada", admin.message.value)

        admin.updateStatus(Fixtures.request(1, status = "COMPLETADA"), RequestStatus.PENDIENTE)
        assertTrue(admin.message.value!!.startsWith("No se puede pasar"))

        coEvery { repo.updateStatus(any(), any()) } throws AppException.BusinessRule("No permitido")
        admin.updateStatus(Fixtures.request(1), RequestStatus.RECHAZADA)
        assertEquals("No permitido", admin.message.value)

        RequestsViewModel(2, UserRole.Client, repo).updateStatus(Fixtures.request(1), RequestStatus.APROBADA)
        coVerify(exactly = 2) { repo.updateStatus(any(), any()) }
    }

    @Test
    fun clienteCancelaSoloSusPendientesYFiltro() {
        val vm = RequestsViewModel(2, UserRole.Client, repo)
        vm.cancel(Fixtures.request(1))
        vm.cancel(Fixtures.request(2, status = "APROBADA"))
        vm.cancel(Fixtures.request(3, userId = 9))
        coVerify(exactly = 1) { repo.delete(1) }
        assertEquals("Solo puedes cancelar tus solicitudes pendientes", vm.message.value)

        val list = listOf(Fixtures.request(1), Fixtures.request(2, status = "APROBADA"))
        assertEquals(2, RequestsViewModel.filter(list, "Todas").size)
        assertEquals(listOf(list[1]), RequestsViewModel.filter(list, "Aprobada"))
    }
}

class CampaignsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<CampaignRepository>(relaxed = true)

    @Test
    fun adminCreaEditaActivaYElimina() {
        coEvery { repo.getAll(false) } returns listOf(Fixtures.campaign, Fixtures.campaign.copy(id = 4, active = false))
        val vm = CampaignsViewModel(UserRole.Admin, repo).apply { refresh() }
        assertEquals(2, vm.campaigns.value.dataOrNull?.size)

        vm.save(Fixtures.campaign.copy(id = 0))
        assertTrue(vm.saved.value)
        assertEquals("Campaña creada", vm.message.value)
        vm.save(Fixtures.campaign)
        assertEquals("Campaña actualizada", vm.message.value)
        vm.setActive(Fixtures.campaign, false)
        coVerify { repo.setActive(3, false) }
        vm.delete(Fixtures.campaign)
        coVerify { repo.delete(3) }
        vm.formHandled()
        assertFalse(vm.saved.value)
    }

    @Test
    fun validaFechasYElClienteSoloLee() {
        val admin = CampaignsViewModel(UserRole.Admin, repo)
        admin.save(Fixtures.campaign.copy(title = "", endDate = "2025-01-01"))
        assertTrue(admin.formErrors.value.containsKey("title") && admin.formErrors.value.containsKey("endDate"))

        coEvery { repo.getAll(true) } returns listOf(Fixtures.campaign)
        val client = CampaignsViewModel(UserRole.Client, repo).apply { refresh() }
        assertEquals(1, client.campaigns.value.dataOrNull?.size)
        client.save(Fixtures.campaign)
        client.setActive(Fixtures.campaign, false)
        client.delete(Fixtures.campaign)
        coVerify(exactly = 0) { repo.save(any()); repo.setActive(any(), any()); repo.delete(any()) }
    }
}

class AdminProductsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<ProductRepository>(relaxed = true)

    @Test
    fun crudFiltrosYValidacionDeProductos() {
        val list = listOf(Fixtures.product(1, "Arco"), Fixtures.product(2, "Tapiz", available = false))
        coEvery { repo.getProducts(false) } returns list
        val vm = AdminProductsViewModel(repo).apply { refresh() }
        assertEquals(2, vm.products.value.dataOrNull?.size)
        assertEquals(listOf(list[0]), AdminProductsViewModel.filter(list, "", "Activos"))
        assertEquals(listOf(list[1]), AdminProductsViewModel.filter(list, "tap", "Todos"))

        vm.save(Fixtures.product(0).copy(name = "", category = "", price = -1))
        assertEquals(setOf("name", "category", "price"), vm.formErrors.value.keys)
        coVerify(exactly = 0) { repo.save(any()) }

        vm.save(Fixtures.product(0))
        assertEquals("Producto creado", vm.message.value)
        vm.save(Fixtures.product(1))
        assertEquals("Producto actualizado", vm.message.value)
        vm.toggleAvailable(Fixtures.product(1))
        coVerify { repo.setAvailable(1, false) }
        vm.delete(Fixtures.product(1))
        assertEquals("Producto eliminado", vm.message.value)
        vm.formHandled()
        assertFalse(vm.saved.value)

        coEvery { repo.delete(any()) } throws IllegalStateException("boom")
        vm.delete(Fixtures.product(1))
        assertTrue(vm.message.value!!.contains("inesperado"))
    }
}
