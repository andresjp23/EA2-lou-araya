package com.example.evaluacion2.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evaluacion2.data.repository.CampaignRepository
import com.example.evaluacion2.domain.model.Campaign
import com.example.evaluacion2.domain.validation.CampaignValidator
import com.example.evaluacion2.viewmodel.CampaignsViewModel
import com.example.evaluacion2.ui.components.AdminBottomBar
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes

@Composable
fun CampaignsScreen(repository: CampaignRepository, isAdmin: Boolean, onBack: () -> Unit, currentRoute: String? = null, onHome: () -> Unit = {}, onProducts: () -> Unit = {}, onRequests: () -> Unit = {}, onProfile: () -> Unit = {}) {
    val vm: CampaignsViewModel = viewModel(key = "campaigns-$isAdmin") { CampaignsViewModel(repository) }
    val campaigns by vm.campaigns.collectAsState()
    var editing by remember { mutableStateOf<Campaign?>(null) }
    var showForm by remember { mutableStateOf(false) }
    LaunchedEffect(isAdmin) { vm.refresh(activeOnly = !isAdmin) }

    Scaffold(bottomBar = {
        if (isAdmin) AdminBottomBar(currentRoute, onHome, onProducts, {}, onRequests, onProfile)
        else ClientBottomBar(currentRoute ?: Routes.CAMPAIGNS, onHome, onProducts, {}, onRequests, onProfile)
    }) { inset ->
        Column(Modifier.fillMaxSize().padding(inset).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isAdmin) "Colecciones y Campañas" else "Ofertas y colecciones", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onBack) { Text("Volver") }
            }
            if (isAdmin) Button(onClick = { editing = null; showForm = true }, modifier = Modifier.fillMaxWidth()) { Text("+ Nueva campaña") }
            if (isAdmin) Text("Colecciones · Campañas · Categorías", style = MaterialTheme.typography.labelLarge)
            if (campaigns.isEmpty() && !isAdmin) Text("No hay campañas disponibles.", style = MaterialTheme.typography.bodyLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(campaigns, key = { it.id }) { campaign ->
                    CampaignCard(campaign, isAdmin, onEdit = { editing = campaign; showForm = true }, onToggle = { vm.setActive(campaign, !campaign.active) })
                }
            }
        }
    }
    if (showForm && isAdmin) CampaignEditorDialog(editing, onDismiss = { showForm = false }) { campaign ->
        if (vm.save(campaign)) showForm = false
    }
}

@Composable
private fun CampaignCard(campaign: Campaign, isAdmin: Boolean, onEdit: () -> Unit, onToggle: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(campaign.title, style = MaterialTheme.typography.titleLarge)
            if (campaign.description.isNotBlank()) Text(campaign.description, style = MaterialTheme.typography.bodyMedium)
            Text("${campaign.startDate} — ${campaign.endDate}", style = MaterialTheme.typography.labelLarge)
            Text(if (campaign.active) "Activa" else "Inactiva", color = if (campaign.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            if (isAdmin) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Editar") }
                OutlinedButton(onClick = onToggle, modifier = Modifier.semantics { contentDescription = if (campaign.active) "Desactivar ${campaign.title}" else "Activar ${campaign.title}" }) { Text(if (campaign.active) "Desactivar" else "Activar") }
            }
        }
    }
}

@Composable
private fun CampaignEditorDialog(initial: Campaign?, onDismiss: () -> Unit, onSave: (Campaign) -> Unit) {
    var title by remember(initial) { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var startDate by remember(initial) { mutableStateOf(initial?.startDate.orEmpty()) }
    var endDate by remember(initial) { mutableStateOf(initial?.endDate.orEmpty()) }
    var active by remember(initial) { mutableStateOf(initial?.active ?: true) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Nueva campaña" else "Editar campaña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Título") }, singleLine = true, isError = errors.containsKey("title"), supportingText = { errors["title"]?.let { Text(it) } })
                OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
                OutlinedTextField(startDate, { startDate = it }, label = { Text("Inicio (AAAA-MM-DD)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), isError = errors.containsKey("startDate"), supportingText = { errors["startDate"]?.let { Text(it) } })
                OutlinedTextField(endDate, { endDate = it }, label = { Text("Término (AAAA-MM-DD)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), isError = errors.containsKey("endDate"), supportingText = { errors["endDate"]?.let { Text(it) } })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Campaña activa", modifier = Modifier.padding(top = 12.dp))
                    Switch(checked = active, onCheckedChange = { active = it }, modifier = Modifier.semantics { contentDescription = "Campaña activa" })
                }
            }
        }, confirmButton = { Button(onClick = {
            val campaign = Campaign(id = initial?.id ?: 0, title = title.trim(), description = description.trim(), startDate = startDate.trim(), endDate = endDate.trim(), active = active)
            errors = CampaignValidator.validate(campaign)
            if (errors.isEmpty()) onSave(campaign)
        }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
