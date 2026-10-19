package com.example.evaluacion2.ui.screens.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.evaluacion2.domain.model.FairEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMapScreen(
    events: List<FairEvent>,
    onBack: () -> Unit,
    currentRoute: String? = null,
    onHome: () -> Unit = {},
    onCatalog: () -> Unit = {},
    onRequests: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var locationMessage by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf("Todos") }
    var selectedEvent by remember { mutableStateOf<FairEvent?>(null) }
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result.values.any { it }
        if (!permissionGranted) locationMessage = "Permiso de ubicación denegado. Puedes ver las ferias y abrir sus direcciones igualmente."
    }
    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Ferias y Showrooms") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) }, bottomBar = { ClientBottomBar(currentRoute ?: Routes.MAP, onHome, onCatalog, {}, onRequests, onProfile) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Explora ferias, showrooms y encuentros artesanales", style = MaterialTheme.typography.bodyMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Todos", "Ferias", "Showrooms", "Eventos")) { type -> FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type) }) }
            }
            locationMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (permissionGranted) Button(onClick = {
                locationMessage = "Ubicación habilitada. Selecciona una feria para abrirla en Maps."
            }) { Text("Ubicación permitida") }
            val visibleEvents = events.filter {
                selectedType == "Todos" || it.place.contains(selectedType.removeSuffix("s"), ignoreCase = true)
            }
            if (visibleEvents.isEmpty()) Text("No hay ferias o showrooms para este filtro.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visibleEvents, key = { it.id }) { event ->
                    Card(Modifier.fillMaxWidth().clickable { selectedEvent = event }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(event.name, style = MaterialTheme.typography.titleMedium)
                            Text("${event.place} · ${event.date}")
                            if (event.address.isNotBlank()) Text(event.address)
                            TextButton(onClick = { openMap(context, event.latitude, event.longitude, event.name) }) { Text("Ver en Maps") }
                        }
                    }
                }
            }
            selectedEvent?.let { event ->
                AlertDialog(onDismissRequest = { selectedEvent = null }, title = { Text(event.name) }, text = { Text("${event.date}\n${event.place}\n${event.address}\n\nUbicación aproximada. Puedes abrirla en una app de mapas.") }, confirmButton = { TextButton(onClick = { openMap(context, event.latitude, event.longitude, event.name) }) { Text("Ver detalles / mapa") } }, dismissButton = { TextButton(onClick = { selectedEvent = null }) { Text("Cerrar") } })
            }
        }
    }
}

private fun openMap(context: Context, latitude: Double, longitude: Double, label: String) {
    val uri = if (latitude != 0.0 || longitude != 0.0) {
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})")
    } else {
        Uri.parse("geo:0,0?q=${Uri.encode(label)}")
    }
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(label)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
