package com.example.evaluacion2.viewmodel

import androidx.lifecycle.ViewModel
import com.example.evaluacion2.data.repository.CampaignRepository
import com.example.evaluacion2.domain.model.Campaign
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CampaignsViewModel(private val repository: CampaignRepository) : ViewModel() {
    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    val campaigns: StateFlow<List<Campaign>> = _campaigns

    fun refresh(activeOnly: Boolean = false) {
        _campaigns.value = repository.getAll(activeOnly)
    }

    fun save(campaign: Campaign): Boolean {
        if (repository.save(campaign) <= 0L) return false
        refresh()
        return true
    }

    fun setActive(campaign: Campaign, active: Boolean) {
        repository.save(campaign.copy(active = active))
        refresh()
    }
}
