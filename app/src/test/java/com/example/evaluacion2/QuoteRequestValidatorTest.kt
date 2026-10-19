package com.example.evaluacion2

import com.example.evaluacion2.domain.validation.QuoteRequestValidator
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRequestValidatorTest {
    @Test fun validRequestHasNoFieldErrors() {
        assertTrue(QuoteRequestValidator.validate("Matrimonio", "2 x 3 m", "Madera", "Blanco", "2030-04-25").isEmpty())
    }

    @Test fun reportsMissingFieldsIndividually() {
        val errors = QuoteRequestValidator.validate("", "", "", "", "")
        assertTrue(errors.containsKey("eventType"))
        assertTrue(errors.containsKey("dimensions"))
        assertTrue(errors.containsKey("material"))
        assertTrue(errors.containsKey("colors"))
        assertTrue(errors.containsKey("requiredDate"))
    }

    @Test fun acceptsSelectedDeadlineOption() {
        assertTrue(QuoteRequestValidator.validate("Evento", "Mediano", "Algodón", "Tonos naturales", "En 1 mes").isEmpty())
    }
}
