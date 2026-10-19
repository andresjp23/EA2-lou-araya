package com.example.evaluacion2.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.evaluacion2.ui.components.AdminBottomBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.ui.navigation.Routes

@Composable
fun AdminDashboardScreen(
    adminName: String,
    onProductsClick: () -> Unit,
    onCampaignsClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Scaffold(bottomBar = { AdminBottomBar(Routes.DASHBOARD_ADMIN, onHome = {}, onProducts = onProductsClick, onCampaigns = onCampaignsClick, onRequests = onRequestsClick, onProfile = onProfileClick) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Administración", style = MaterialTheme.typography.headlineMedium)
            Text("Hola, $adminName", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Panel principal", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onNotificationsClick) { Icon(Icons.Default.Notifications, contentDescription = "Notificaciones") }
            }
            AdminActionCard("Gestionar productos", "Crear y administrar el catálogo local", onProductsClick)
            AdminActionCard("Campañas y colecciones", "Administrar contenido promocional", onCampaignsClick)
            AdminActionCard("Solicitudes", "Revisar solicitudes de cotización", onRequestsClick)
            AdminActionCard("Perfil", "Ver datos y configuración", onProfileClick)
            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
        }
    }
}

@Composable
private fun AdminActionCard(title: String, description: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick) { Text("Abrir") }
        }
    }
}
