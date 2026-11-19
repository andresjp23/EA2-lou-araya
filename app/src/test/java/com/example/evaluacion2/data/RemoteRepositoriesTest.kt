package com.example.evaluacion2.data

import com.example.evaluacion2.data.local.ProductCache
import com.example.evaluacion2.data.local.UserCache
import com.example.evaluacion2.data.remote.api.CatalogoApi
import com.example.evaluacion2.data.remote.api.SolicitudesApi
import com.example.evaluacion2.data.remote.api.UsuariosApi
import com.example.evaluacion2.data.remote.dto.LoginRequestDto
import com.example.evaluacion2.data.remote.dto.StatusRequestDto
import com.example.evaluacion2.data.remote.dto.ToggleRequestDto
import com.example.evaluacion2.data.remote.dto.UserDto
import com.example.evaluacion2.data.remote.toDto
import com.example.evaluacion2.data.repository.RemoteCampaignRepository
import com.example.evaluacion2.data.repository.RemoteProductRepository
import com.example.evaluacion2.data.repository.RemoteRequestRepository
import com.example.evaluacion2.data.repository.RemoteUserRepository
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.util.Fixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Repositorios remotos con MockK: la API de Retrofit y la caché SQLite son simuladas. */
class RemoteRepositoriesTest {
    private val usuarios = mockk<UsuariosApi>()
    private val catalogo = mockk<CatalogoApi>()
    private val solicitudes = mockk<SolicitudesApi>()
    private val userCache = mockk<UserCache>(relaxed = true)
    private val productCache = mockk<ProductCache>(relaxed = true)
    private val userDto = UserDto(2, "Cliente Demo", "cliente", "cliente@louaraya.cl", "+56911111111", "CLIENT")

    @Test
    fun loginYRegistroGuardanElPerfilEnCache() = runTest {
        val repo = RemoteUserRepository(usuarios, userCache, StandardTestDispatcher(testScheduler))
        coEvery { usuarios.login(LoginRequestDto("cliente@louaraya.cl", "Cliente123")) } returns userDto
        coEvery { usuarios.register(any()) } returns userDto

        val user = repo.login(" cliente@louaraya.cl ", "Cliente123")
        assertEquals(2, user.id)
        repo.register(User(name = " Cliente Demo ", alias = "cliente", email = "cliente@louaraya.cl", phone = "+56911111111", password = "secreta1"))

        coVerify { usuarios.register(match { it.name == "Cliente Demo" && it.password == "secreta1" }) }
        verify(exactly = 2) { userCache.upsert(user) }
    }

    @Test
    fun perfilSinConexionYEdicion() = runTest {
        val repo = RemoteUserRepository(usuarios, userCache, StandardTestDispatcher(testScheduler))
        coEvery { usuarios.getUser(2) } throws IOException()
        every { userCache.getById(2) } returns Fixtures.client.copy(avatarUri = "content://foto")
        assertEquals("cliente", repo.getUser(2)?.alias)

        coEvery { usuarios.getUser(3) } throws IOException()
        every { userCache.getById(3) } returns null
        assertTrue(runCatching { repo.getUser(3) }.exceptionOrNull() is AppException.Network)

        coEvery { usuarios.updateProfile(2, any()) } returns userDto.copy(name = "Nuevo")
        assertEquals("Nuevo", repo.updateProfile(2, " Nuevo ", "cliente", "+56911111111").name)
        coVerify { usuarios.updateProfile(2, match { it.avatarUri == "content://foto" && it.name == "Nuevo" }) }
    }

    @Test
    fun catalogoSeSincronizaYFuncionaOffline() = runTest {
        val repo = RemoteProductRepository(catalogo, productCache, StandardTestDispatcher(testScheduler))
        val p1 = Fixtures.product(1)
        val p2 = Fixtures.product(2, available = false)
        coEvery { catalogo.getProducts(false) } returns listOf(p1.toDto(), p2.toDto())
        assertEquals(listOf(p1), repo.getProducts(onlyAvailable = true))
        verify { productCache.upsertAll(listOf(p1, p2)) }

        coEvery { catalogo.getProducts(false) } throws IOException()
        every { productCache.getAll() } returns listOf(p1, p2)
        assertEquals(2, repo.getProducts().size)
        every { productCache.getAll() } returns emptyList()
        assertTrue(runCatching { repo.getProducts() }.exceptionOrNull() is AppException.Network)
    }

    @Test
    fun crudDeProductos() = runTest {
        val repo = RemoteProductRepository(catalogo, productCache, StandardTestDispatcher(testScheduler))
        val p1 = Fixtures.product(1)
        coEvery { catalogo.getProduct(1) } returns p1.toDto()
        coEvery { catalogo.getProduct(5) } throws HttpException(Response.error<Any>(404, "{}".toResponseBody(null)))
        coEvery { catalogo.getProduct(6) } throws IOException()
        every { productCache.findById(6) } returns Fixtures.product(6)
        every { productCache.findById(1) } returns p1
        coEvery { catalogo.createProduct(any()) } returns p1.toDto()
        coEvery { catalogo.updateProduct(1, any()) } returns p1.copy(name = "Nuevo").toDto()
        coEvery { catalogo.setProductAvailable(1, any()) } returns p1.copy(available = false).toDto()
        coEvery { catalogo.deleteProduct(1) } returns Response.success(Unit)

        assertEquals(p1, repo.findById(1))
        assertNull(repo.findById(5))
        verify { productCache.delete(5) }
        assertEquals(6, repo.findById(6)?.id)
        assertEquals(p1, repo.findCached(1))
        assertEquals(1, repo.save(p1.copy(id = 0)).id)
        assertEquals("Nuevo", repo.save(p1).name)
        assertEquals(false, repo.setAvailable(1, false).available)
        repo.delete(1)
        verify { productCache.delete(1) }
    }

    @Test
    fun crudDeCampanasYSolicitudes() = runTest {
        val campaigns = RemoteCampaignRepository(catalogo)
        val dto = Fixtures.campaign.toDto()
        coEvery { catalogo.getCampaigns(true) } returns listOf(dto)
        coEvery { catalogo.createCampaign(any()) } returns dto
        coEvery { catalogo.updateCampaign(3, any()) } returns dto
        coEvery { catalogo.setCampaignActive(3, ToggleRequestDto(false)) } returns dto.copy(active = false)
        coEvery { catalogo.deleteCampaign(3) } returns Response.success(Unit)
        coEvery { catalogo.deleteCampaign(9) } returns Response.error(404, "{}".toResponseBody(null))
        assertEquals(1, campaigns.getAll(activeOnly = true).size)
        campaigns.save(Fixtures.campaign.copy(id = 0))
        campaigns.save(Fixtures.campaign)
        assertEquals(false, campaigns.setActive(3, false).active)
        campaigns.delete(3)
        assertTrue(runCatching { campaigns.delete(9) }.exceptionOrNull() is AppException.NotFound)

        val requests = RemoteRequestRepository(solicitudes)
        val req = Fixtures.request(1).toDto().copy(id = 1, status = "PENDIENTE")
        coEvery { solicitudes.getRequests(2) } returns listOf(req)
        coEvery { solicitudes.getRequests(null) } returns listOf(req, req.copy(id = 2))
        coEvery { solicitudes.createRequest(any()) } returns req
        coEvery { solicitudes.updateStatus(1, StatusRequestDto("APROBADA")) } returns req.copy(status = "APROBADA")
        coEvery { solicitudes.deleteRequest(1) } returns Response.success(Unit)
        assertEquals(1, requests.getForUser(2).size)
        assertEquals(2, requests.getAll().size)
        assertEquals(1, requests.create(Fixtures.request(0)).id)
        assertEquals("APROBADA", requests.updateStatus(1, "APROBADA").status)
        requests.delete(1)
        coVerify { solicitudes.deleteRequest(1) }
    }
}
