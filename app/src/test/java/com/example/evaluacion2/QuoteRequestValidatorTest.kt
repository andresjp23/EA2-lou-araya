package com.example.evaluacion2

import com.example.evaluacion2.domain.validation.QuoteRequestValidator
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRequestValidatorTest {
    @Test fun validRequestHasNoFieldErrors() {
        assertTrue(QuoteRequestValidator.validate("Arco de ceremonia", "Matrimonio", "2 x 3 m", "Madera", "Blanco", "2030-04-25").isEmpty())
    }

    @Test fun reportsMissingFieldsIndividually() {
        val errors = QuoteRequestValidator.validate("", "", "", "", "", "")
        assertTrue(errors.containsKey("productType"))
        assertTrue(errors.containsKey("eventType"))
        assertTrue(errors.containsKey("dimensions"))
        assertTrue(errors.containsKey("material"))
        assertTrue(errors.containsKey("colors"))
        assertTrue(errors.containsKey("requiredDate"))
    }

    @Test fun acceptsSelectedDeadlineOption() {
        assertTrue(QuoteRequestValidator.validate("Tapiz mural", "Evento", "Mediano", "Algodón", "Tonos naturales", "En 1 mes").isEmpty())
    }
}
