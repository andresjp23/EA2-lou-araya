package com.example.evaluacion2.data.repository

import com.example.evaluacion2.data.remote.api.CatalogoApi
import com.example.evaluacion2.data.remote.dto.ToggleRequestDto
import com.example.evaluacion2.data.remote.requireSuccess
import com.example.evaluacion2.data.remote.safeApiCall
import com.example.evaluacion2.data.remote.toDomain
import com.example.evaluacion2.data.remote.toDto
import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.repository.CampaignRepository

/** Campañas y colecciones desde ms-catalogo. */
class RemoteCampaignRepository(private val api: CatalogoApi) : CampaignRepository {

    override suspend fun getAll(activeOnly: Boolean): List<Campaign> =
        safeApiCall { api.getCampaigns(activeOnly) }.map { it.toDomain() }

    override suspend fun save(campaign: Campaign): Campaign = safeApiCall {
        if (campaign.id > 0) api.updateCampaign(campaign.id, campaign.toDto()) else api.createCampaign(campaign.toDto())
    }.toDomain()

    override suspend fun setActive(campaignId: Int, active: Boolean): Campaign =
        safeApiCall { api.setCampaignActive(campaignId, ToggleRequestDto(active)) }.toDomain()

    override suspend fun delete(campaignId: Int) {
        safeApiCall { api.deleteCampaign(campaignId).requireSuccess() }
    }
}
