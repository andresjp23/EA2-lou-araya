package com.example.evaluacion2

import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.validation.CampaignValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CampaignValidatorTest {
    @Test fun validCampaignHasNoErrors() {
        assertTrue(CampaignValidator.validate(Campaign(title = "Verano", description = "Colección", startDate = "2030-01-01", endDate = "2030-02-01")).isEmpty())
    }

    @Test fun missingTitleAndInvalidDatesAreReported() {
        val errors = CampaignValidator.validate(Campaign(title = " ", startDate = "2030-02-30", endDate = "no-date"))
        assertEquals("Ingresa el nombre de la campaña", errors["title"])
        assertTrue(errors.containsKey("startDate"))
        assertTrue(errors.containsKey("endDate"))
    }

    @Test fun rejectsEndDateBeforeStartDate() {
        val errors = CampaignValidator.validate(Campaign(title = "Oferta", startDate = "2030-04-10", endDate = "2030-04-09"))
        assertEquals("La fecha de término debe ser posterior al inicio", errors["endDate"])
    }
}
