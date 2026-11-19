package com.example.evaluacion2.viewmodel

import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.model.UserRole
import com.example.evaluacion2.domain.repository.CampaignRepository
import com.example.evaluacion2.domain.validation.CampaignValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Campañas y colecciones (CRUD completo para el administrador, solo activas para el cliente). */
class CampaignsViewModel(
    private val role: UserRole,
    private val repository: CampaignRepository
) : BaseViewModel() {
    private val _campaigns = MutableStateFlow<UiState<List<Campaign>>>(UiState.Loading)
    val campaigns: StateFlow<UiState<List<Campaign>>> = _campaigns.asStateFlow()

    private val _formErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val formErrors: StateFlow<Map<String, String>> = _formErrors.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    fun refresh() {
        load(_campaigns) { repository.getAll(activeOnly = role.seesOnlyActiveCampaigns) }
    }

    fun save(campaign: Campaign) {
        if (!role.canManageCatalog) return
        val errors = CampaignValidator.validate(campaign)
        _formErrors.value = errors
        _saved.value = false
        if (errors.isNotEmpty()) return
        launchSafe {
            repository.save(campaign)
            _saved.value = true
            showMessage(if (campaign.id == 0) "Campaña creada" else "Campaña actualizada")
            refresh()
        }
    }

    fun setActive(campaign: Campaign, active: Boolean) {
        if (!role.canManageCatalog) return
        launchSafe {
            repository.setActive(campaign.id, active)
            refresh()
        }
    }

    fun delete(campaign: Campaign) {
        if (!role.canManageCatalog) return
        launchSafe {
            repository.delete(campaign.id)
            showMessage("Campaña eliminada")
            refresh()
        }
    }

    fun formHandled() {
        _saved.value = false
        _formErrors.value = emptyMap()
    }
}
