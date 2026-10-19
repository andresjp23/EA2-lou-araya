package com.example.evaluacion2.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
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
fun AdminBottomBar(currentRoute: String?, onHome: () -> Unit, onProducts: () -> Unit, onCampaigns: () -> Unit, onRequests: () -> Unit, onProfile: () -> Unit) {
    NavigationBar {
        listOf(
            Triple("Inicio", Icons.Default.Home, onHome),
            Triple("Productos", Icons.Default.Storefront, onProducts),
            Triple("Campañas", Icons.Default.Campaign, onCampaigns),
            Triple("Solicitudes", Icons.Default.ReceiptLong, onRequests),
            Triple("Perfil", Icons.Default.Person, onProfile)
        ).forEach { (label, icon, action) ->
            val route = when (label) {
                "Inicio" -> Routes.DASHBOARD_ADMIN
                "Productos" -> Routes.ADMIN_PRODUCTS
                "Campañas" -> Routes.CAMPAIGNS
                "Solicitudes" -> Routes.REQUESTS
                else -> Routes.PROFILE
            }
            NavigationBarItem(selected = currentRoute == route, onClick = action, icon = { Icon(icon, contentDescription = label) }, label = { Text(label) })
        }
    }
}
