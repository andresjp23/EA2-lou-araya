package com.example.evaluacion2.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.ui.components.ClientBottomBar
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import com.example.evaluacion2.ui.components.AdminBottomBar
import com.example.evaluacion2.ui.navigation.Routes

data class QuickAccessItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun DashboardScreen(
    userName: String,
    favoriteProducts: List<Product>,
    onFavoriteProductClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    onCatalogClick: () -> Unit,
    onMapClick: () -> Unit,
    onQrClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogout: () -> Unit,
    isAdmin: Boolean = false
) {
    var search by remember { mutableStateOf("") }
    Scaffold(bottomBar = {
        if (isAdmin) AdminBottomBar(Routes.DASHBOARD_ADMIN, {}, onCatalogClick, onFavoritesClick, onRequestsClick, onProfileClick)
        else ClientBottomBar(Routes.DASHBOARD_CLIENT, {}, onCatalogClick, onMapClick, onRequestsClick, onProfileClick)
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", modifier = Modifier.padding(12.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Hola, $userName", style = MaterialTheme.typography.bodyMedium)
                        Text("Qué lindo tenerte aquí", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onNotificationsClick) { Icon(Icons.Default.Notifications, contentDescription = "Notificaciones") }
                }
            }
            item { OutlinedTextField(search, { search = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar productos, ferias, inspiración...") }, singleLine = true, trailingIcon = { IconButton(onClick = onCatalogClick) { Icon(Icons.Default.Search, contentDescription = "Buscar en el catálogo") } }) }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Colección destacada", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(6.dp))
                        Text("Bodas de Ensueño", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Diseños únicos para tu gran día", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
                        Button(onClick = onCatalogClick) { Text("Ver colección") }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    TextButton(onClick = onFavoritesClick) { Text("Favoritos") }
                }
            }
            item {
                val shortcuts = listOf(
                    QuickAccessItem("Catálogo", Icons.Default.Storefront, onCatalogClick),
                    QuickAccessItem("Favoritos", Icons.Default.Favorite, onFavoritesClick),
                    QuickAccessItem("Mapa", Icons.Default.Map, onMapClick),
                    QuickAccessItem("Escanear QR", Icons.Default.QrCodeScanner, onQrClick),
                    QuickAccessItem("Solicitudes", Icons.AutoMirrored.Filled.ReceiptLong, onRequestsClick),
                    QuickAccessItem("Perfil", Icons.Default.Person, onProfileClick)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shortcuts) { QuickAccessCard(it) }
                }
            }
            item { Text(if (isAdmin) "Resumen del catálogo" else "Productos destacados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            if (favoriteProducts.isEmpty()) {
                item { Text(if (isAdmin) "Gestiona productos, colecciones y solicitudes pendientes desde los accesos." else "Aún no has agregado productos a favoritos.", style = MaterialTheme.typography.bodyMedium) }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(favoriteProducts.take(4), key = { "home-favorite-${it.id}" }) { product ->
                            Card(onClick = { onFavoriteProductClick(product.id) }, modifier = Modifier.width(220.dp), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(product.category, style = MaterialTheme.typography.bodySmall)
                                    Text("$${product.price}", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }
            item { TextButton(onClick = onLogout) { Text("Cerrar sesión") } }
        }
    }
}

@Composable
fun QuickAccessCard(item: QuickAccessItem) {
    Card(onClick = item.onClick, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(item.icon, contentDescription = item.title)
            Spacer(Modifier.height(8.dp))
            Text(item.title, style = MaterialTheme.typography.labelLarge)
        }
    }
}
