package com.example.evaluacion2.domain

import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.validation.CampaignValidator
import com.example.evaluacion2.domain.validation.ProductQrPayload
import com.example.evaluacion2.domain.validation.ProductValidator
import com.example.evaluacion2.domain.validation.QuoteRequestValidator
import com.example.evaluacion2.domain.validation.UserValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validaciones de los formularios: la lógica vive fuera de las pantallas. */
class ValidatorsTest {

    @Test
    fun validacionesDelUsuario() {
        assertEquals("El nombre es obligatorio", UserValidator.validateName(" "))
        assertEquals("El nombre debe tener al menos 3 caracteres", UserValidator.validateName("Lu"))
        assertNull(UserValidator.validateName("Lou Araya"))
        assertEquals("El alias es obligatorio", UserValidator.validateAlias(""))
        assertEquals("El alias debe tener al menos 3 caracteres", UserValidator.validateAlias("lo"))
        assertNull(UserValidator.validateAlias("louaraya"))
        assertEquals("El correo es obligatorio", UserValidator.validateEmail(""))
        assertEquals("Ingresa un correo válido", UserValidator.validateEmail("lou@"))
        assertNull(UserValidator.validateEmail("lou@louaraya.cl"))
        assertEquals("El teléfono es obligatorio", UserValidator.validatePhone(""))
        assertEquals("Ingresa un teléfono válido", UserValidator.validatePhone("+56 9 12"))
        assertNull(UserValidator.validatePhone("+56 9 1234 5678"))
        assertEquals("La contraseña es obligatoria", UserValidator.validatePassword(""))
        assertEquals("Debe tener al menos 6 caracteres", UserValidator.validatePassword("123"))
        assertNull(UserValidator.validatePassword("secreta1"))
        assertEquals("Confirma tu contraseña", UserValidator.validatePasswordConfirmation("secreta1", ""))
        assertEquals("Las contraseñas no coinciden", UserValidator.validatePasswordConfirmation("secreta1", "otra"))
        assertNull(UserValidator.validatePasswordConfirmation("secreta1", "secreta1"))
    }

    @Test
    fun productoYCodigoQr() {
        val errors = ProductValidator.validate(Product(name = "", category = "", price = -1))
        assertEquals("Ingresa el nombre del producto", errors["name"])
        assertEquals("Ingresa una categoría", errors["category"])
        assertEquals("Ingresa un precio válido (0 o más)", errors["price"])
        assertTrue(ProductValidator.validate(Product(name = "Alfombra", category = "Hogar", price = 100)).isEmpty())
        assertEquals("product:42", ProductQrPayload.forProduct(42))
        assertEquals(42, ProductQrPayload.parseProductId("product:42"))
        assertEquals(42, ProductQrPayload.parseProductId(" 42 "))
        assertNull(ProductQrPayload.parseProductId("product:0"))
        assertNull(ProductQrPayload.parseProductId("https://example.com/product/42"))
    }

    @Test
    fun campanaConFechas() {
        assertTrue(CampaignValidator.validate(Campaign(title = "Verano", startDate = "2030-01-01", endDate = "2030-02-01")).isEmpty())
        val invalid = CampaignValidator.validate(Campaign(title = " ", startDate = "2030-02-30", endDate = "no-date"))
        assertEquals("Ingresa el nombre de la campaña", invalid["title"])
        assertTrue(invalid.containsKey("startDate") && invalid.containsKey("endDate"))
        val reversed = CampaignValidator.validate(Campaign(title = "Oferta", startDate = "2030-04-10", endDate = "2030-04-09"))
        assertEquals("La fecha de término debe ser posterior al inicio", reversed["endDate"])
    }

    @Test
    fun solicitudDeCotizacion() {
        assertTrue(QuoteRequestValidator.validate("Arco de ceremonia", "Matrimonio", "2 x 3 m", "Madera", "Blanco", "2030-04-25").isEmpty())
        val errors = QuoteRequestValidator.validate("", "", "", "", "", "")
        assertEquals(setOf("productType", "eventType", "dimensions", "material", "colors", "requiredDate"), errors.keys)
        assertTrue(QuoteRequestValidator.validate("Tapiz", "Evento", "Mediano", "Algodón", "Natural", "1 mes").containsKey("requiredDate"))
    }
}
