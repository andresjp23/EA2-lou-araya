package com.example.evaluacion2.domain.repository

import com.example.evaluacion2.domain.model.Campaign

interface CampaignRepository {
    suspend fun getAll(activeOnly: Boolean = false): List<Campaign>
    suspend fun save(campaign: Campaign): Campaign
    suspend fun setActive(campaignId: Int, active: Boolean): Campaign
    suspend fun delete(campaignId: Int)
}
