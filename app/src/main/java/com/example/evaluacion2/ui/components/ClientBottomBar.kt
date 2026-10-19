package com.example.evaluacion2.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.evaluacion2.ui.navigation.Routes

@Composable
fun ClientBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onMapClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.DASHBOARD || currentRoute == Routes.DASHBOARD_CLIENT,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.CATALOG,
            onClick = onCatalogClick,
            icon = { Icon(Icons.Default.Storefront, contentDescription = "Catálogo") },
            label = { Text("Catálogo") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.MAP,
            onClick = onMapClick,
            icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
            label = { Text("Mapa") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.REQUESTS || currentRoute == Routes.QUOTE ||
                currentRoute?.startsWith("${Routes.QUOTE}?") == true,
            onClick = onRequestsClick,
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Solicitudes") },
            label = { Text("Solicitudes") }
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}
