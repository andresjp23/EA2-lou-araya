package com.example.evaluacion2.ui.screens.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.evaluacion2.domain.model.FairEvent
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val EVENT_ZOOM = 13.5
private val SANTIAGO_CENTER = GeoPoint(-33.4489, -70.6693)

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
    val mapView = rememberEventMapView()
    val locationOverlay = remember { MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) }
    var selectedType by remember { mutableStateOf("Todos") }
    var selectedEvent by remember { mutableStateOf<FairEvent?>(null) }
    var locationMessage by remember { mutableStateOf<String?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(context.hasLocationPermission()) }
    val visibleEvents = events.filter { event ->
        event.hasCoordinates() && (selectedType == "Todos" || event.place.contains(selectedType.removeSuffix("s"), ignoreCase = true))
    }

    fun showCurrentLocation() {
        if (locationOverlay !in mapView.overlays) mapView.overlays.add(locationOverlay)
        locationOverlay.enableMyLocation()
        locationOverlay.myLocation?.let { point ->
            mapView.controller.animateTo(point, EVENT_ZOOM, 700L)
        } ?: locationOverlay.runOnFirstFix {
            mapView.post {
                locationOverlay.myLocation?.let { point ->
                    mapView.controller.animateTo(point, EVENT_ZOOM, 700L)
                }
            }
        }
        locationMessage = "Mostrando tu ubicación en el mapa."
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        locationPermissionGranted = grants.values.any { it }
        if (locationPermissionGranted) showCurrentLocation()
        else locationMessage = "Puedes explorar las ferias, pero se necesita permiso para mostrar tu ubicación."
    }

    LaunchedEffect(visibleEvents) {
        mapView.overlays.removeAll { it is Marker }
        visibleEvents.forEach { event ->
            Marker(mapView).apply {
                position = GeoPoint(event.latitude, event.longitude)
                title = event.name
                snippet = "${event.place} · ${event.date}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    selectedEvent = event
                    mapView.controller.animateTo(position, EVENT_ZOOM, 500L)
                    true
                }
                mapView.overlays.add(this)
            }
        }
        mapView.invalidate()
        if (selectedEvent?.let { it !in visibleEvents } == true) selectedEvent = null
        if (visibleEvents.isNotEmpty()) mapView.centerOnEvents(visibleEvents)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ferias y Showrooms") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        },
        bottomBar = {
            ClientBottomBar(currentRoute ?: Routes.MAP, onHome, onCatalog, {}, onRequests, onProfile)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Explora nuestros próximos encuentros", style = MaterialTheme.typography.titleMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Todos", "Ferias", "Showrooms", "Eventos")) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    if (visibleEvents.isEmpty()) {
                        Text("No hay ubicaciones disponibles para este filtro.", style = MaterialTheme.typography.bodyMedium)
                    }
                    locationMessage?.let { message ->
                        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            MapControls(
                onZoomIn = { mapView.controller.zoomIn() },
                onZoomOut = { mapView.controller.zoomOut() },
                onMyLocation = {
                    if (locationPermissionGranted) showCurrentLocation()
                    else locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            )

            selectedEvent?.let { event ->
                EventMapCard(
                    event = event,
                    onDismiss = { selectedEvent = null },
                    onDirections = { openExternalMap(context, event) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberEventMapView(): MapView {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(EVENT_ZOOM)
            controller.setCenter(SANTIAGO_CENTER)
        }
    }
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
    return mapView
}

@Composable
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        SmallFloatingActionButton(onClick = onZoomIn, containerColor = MaterialTheme.colorScheme.surface) {
            Icon(Icons.Default.Add, contentDescription = "Acercar mapa")
        }
        SmallFloatingActionButton(onClick = onZoomOut, containerColor = MaterialTheme.colorScheme.surface) {
            Icon(Icons.Default.Remove, contentDescription = "Alejar mapa")
        }
        FloatingActionButton(onClick = onMyLocation, containerColor = MaterialTheme.colorScheme.primary) {
            Icon(Icons.Default.LocationOn, contentDescription = "Mi ubicación", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun EventMapCard(
    event: FairEvent,
    onDismiss: () -> Unit,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(event.name, style = MaterialTheme.typography.titleMedium)
                Text("${event.place} · ${event.date}", style = MaterialTheme.typography.bodyMedium)
                if (event.address.isNotBlank()) Text(event.address, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onDirections) { Text("Abrir indicaciones") }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar detalle")
            }
        }
    }
}

private fun FairEvent.hasCoordinates(): Boolean = latitude != 0.0 || longitude != 0.0

private fun MapView.centerOnEvents(events: List<FairEvent>) {
    val latitude = events.map { it.latitude }.average()
    val longitude = events.map { it.longitude }.average()
    controller.setCenter(GeoPoint(latitude, longitude))
    controller.setZoom(if (events.size == 1) EVENT_ZOOM else 12.2)
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun openExternalMap(context: Context, event: FairEvent) {
    val uri = Uri.parse(
        "geo:${event.latitude},${event.longitude}?q=${event.latitude},${event.longitude}(${Uri.encode(event.name)})"
    )
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${event.latitude},${event.longitude}")
            )
        )
    }
}
