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
import com.example.evaluacion2.ui.navigation.LocalAppNavigator
import com.example.evaluacion2.ui.navigation.Routes

/** Barra inferior del cliente. */
@Composable
fun ClientBottomBar(currentRoute: String?) {
    val navigator = LocalAppNavigator.current
    NavigationBar {
        listOf(
            Triple("Inicio", Icons.Default.Home, Routes.DASHBOARD_CLIENT),
            Triple("Catálogo", Icons.Default.Storefront, Routes.CATALOG),
            Triple("Mapa", Icons.Default.Map, Routes.MAP),
            Triple("Solicitudes", Icons.Default.ReceiptLong, Routes.REQUESTS),
            Triple("Perfil", Icons.Default.Person, Routes.PROFILE)
        ).forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { if (currentRoute != route) navigator.navigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
