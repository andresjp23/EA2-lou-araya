package com.example.evaluacion2.domain.validation

import com.example.evaluacion2.domain.model.Campaign
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

object CampaignValidator {
    fun validate(campaign: Campaign): Map<String, String> = buildMap {
        if (campaign.title.isBlank()) put("title", "Ingresa el nombre de la campaña")
        if (!campaign.startDate.isIsoDate()) put("startDate", "Ingresa una fecha válida (AAAA-MM-DD)")
        if (!campaign.endDate.isIsoDate()) put("endDate", "Ingresa una fecha válida (AAAA-MM-DD)")
        if (campaign.startDate.isIsoDate() && campaign.endDate.isIsoDate() && campaign.endDate < campaign.startDate) {
            put("endDate", "La fecha de término debe ser posterior al inicio")
        }
    }

    private fun String.isIsoDate(): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
        val position = ParsePosition(0)
        return formatter.parse(this, position) != null && position.index == length
    }
}
