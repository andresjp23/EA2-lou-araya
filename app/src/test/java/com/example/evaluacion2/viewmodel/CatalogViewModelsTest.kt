package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.data.local.FavoriteRepository
import com.example.evaluacion2.domain.error.AppException
import com.example.evaluacion2.domain.model.Favorite
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.domain.repository.ProductRepository
import com.example.evaluacion2.domain.repository.RequestRepository
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

class CatalogViewModelsTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val products = mockk<ProductRepository>()
    private val favorites = mockk<FavoriteRepository>(relaxed = true)
    private val requests = mockk<RequestRepository>()
    private val catalog = listOf(
        Fixtures.product(1, "Arco de ceremonia", category = "Matrimonios"),
        Fixtures.product(2, "Tapiz mural", category = "Interiores"),
        Fixtures.product(3, "Cortina", available = false, category = "Festivales")
    )

    @Test
    fun catalogoCargaFiltraYMuestraErrores() {
        coEvery { products.getProducts(false) } returns catalog
        val vm = CatalogViewModel(products).apply { refresh() }
        assertEquals(3, vm.products.value.dataOrNull?.size)
        assertEquals(1, CatalogViewModel.filter(catalog, "tapiz", "Todos").size)
        assertEquals(listOf(catalog[0]), CatalogViewModel.filter(catalog, "", "Matrimonios"))
        assertTrue(CatalogViewModel.filter(catalog, "arco", "Festivales").isEmpty())

        coEvery { products.getProducts(false) } throws AppException.Network()
        val offline = CatalogViewModel(products).apply { refresh() }
        assertTrue(offline.products.value is UiState.Error)
        assertNull(offline.products.value.dataOrNull)
    }

    @Test
    fun favoritosYDestacadosDelInicio() {
        coEvery { products.getProducts(false) } returns catalog
        every { favorites.getForUser(2) } returns listOf(Favorite(1, 2, 2), Favorite(2, 2, 99))
        val favoritesVm = FavoritesViewModel(2, favorites, products, mainRule.dispatcher).apply { refresh() }
        assertEquals(listOf(catalog[1]), favoritesVm.favoriteProducts.value.dataOrNull)

        every { favorites.getHomeFavorites(2) } returns listOf(Favorite(1, 2, 3))
        val dashboard = DashboardViewModel(2, favorites, products, mainRule.dispatcher).apply { refresh() }
        assertEquals(listOf(3, 1, 2), dashboard.featured.value.map { it.id })
        assertEquals(1, DashboardViewModel.featuredProducts(emptyList(), catalog, limit = 1).size)
    }

    @Test
    fun detalleYFavorito() {
        coEvery { products.findById(1) } returns catalog[0]
        every { favorites.getForUser(2) } returnsMany listOf(emptyList(), emptyList(), listOf(Favorite(1, 2, 1)), listOf(Favorite(1, 2, 1)), emptyList())
        val vm = DetailViewModel(2, 1, favorites, products, mainRule.dispatcher).apply { refresh() }
        assertEquals(catalog[0], vm.product.value.dataOrNull)
        assertFalse(vm.isFavorite.value)
        vm.toggleFavorite()
        verify { favorites.add(2, 1) }
        assertEquals("Agregado a favoritos", vm.message.value)
        vm.toggleFavorite()
        verify { favorites.remove(2, 1) }
        assertEquals("Quitado de favoritos", vm.message.value)
    }

    @Test
    fun cotizacionValidaYSeEnviaAlMicroservicio() {
        coEvery { products.getProducts(false) } returns catalog
        coEvery { requests.create(any()) } returns Fixtures.request(15)
        val vm = QuoteViewModel(2, "Cliente Demo", 1, requests, products)
        assertEquals("Arco de ceremonia", vm.state.value.productType)

        vm.setProductType("")
        vm.submit()
        assertTrue(vm.state.value.errors.containsKey("productType"))
        coVerify(exactly = 0) { requests.create(any()) }

        vm.setProductType("Tapiz mural")
        vm.setEventType("Matrimonio")
        vm.setDimensions(" 2 x 3 m ")
        vm.setMaterial("Algodón natural")
        vm.setColors("Tonos naturales")
        vm.setRequiredDate("2026-12-20")
        vm.setObservations("Sin flores")
        vm.submit()
        assertEquals("Solicitud #15 enviada correctamente", vm.state.value.successMessage)
        coVerify { requests.create(match<QuoteRequest> { it.productId == 2 && it.dimensions == "2 x 3 m" && it.userName == "Cliente Demo" }) }

        coEvery { requests.create(any()) } throws AppException.BusinessRule("Regla")
        vm.submit()
        assertEquals("Regla", vm.state.value.errorMessage)
        assertFalse(vm.state.value.isSaving)
    }
}
