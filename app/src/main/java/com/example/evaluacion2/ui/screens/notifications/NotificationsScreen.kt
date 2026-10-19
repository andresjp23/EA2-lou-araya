package com.example.evaluacion2.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.data.repository.LocalNotification
import com.example.evaluacion2.ui.components.AdminBottomBar
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<LocalNotification>,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenEvents: () -> Unit,
    onRead: (Int) -> Unit,
    onOpenCampaigns: () -> Unit,
    onHome: () -> Unit,
    onCatalog: () -> Unit,
    onMap: () -> Unit,
    onProfile: () -> Unit,
    onProducts: () -> Unit,
    currentRoute: String?
) {
    var selected by remember { mutableStateOf("Todas") }
    val visibleNotifications = if (isAdmin) {
        notifications.filter { it.category != "Campañas" || it.title.contains("solicitud", ignoreCase = true) }
    } else notifications
    val filters = listOf("Todas", "Campañas", "Solicitudes", "Eventos")
    val filtered = visibleNotifications.filter { selected == "Todas" || it.category == selected }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        },
        bottomBar = {
            if (isAdmin) {
                AdminBottomBar(currentRoute, onHome, onProducts, onOpenCampaigns, onOpenRequests, onProfile)
            } else {
                ClientBottomBar(currentRoute ?: Routes.NOTIFICATION_INBOX, onHome, onCatalog, onMap, onOpenRequests, onProfile)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(selected = selected == filter, onClick = { selected = filter }, label = { Text(filter) })
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                items(filtered, key = { it.id }) { notice ->
                    Card(
                        Modifier.fillMaxWidth().clickable {
                            onRead(notice.id)
                            when (notice.category) {
                                "Eventos" -> onOpenEvents()
                                "Solicitudes" -> onOpenRequests()
                                else -> onOpenCampaigns()
                            }
                        }
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(notice.title, style = MaterialTheme.typography.titleMedium)
                            Text(notice.description)
                            Text("${notice.category} · ${notice.createdAt}", style = MaterialTheme.typography.labelMedium)
                            if (!notice.read) Text("Nuevo", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                if (filtered.isEmpty()) item { Text("No hay notificaciones para este filtro.", Modifier.padding(16.dp)) }
            }
        }
    }
}
