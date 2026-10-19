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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes


data class QuickAccessItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)


@Composable
fun DashboardScreen(
    onCatalogClick: () -> Unit,
    onMapClick: () -> Unit,
    onQrClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {

    Scaffold(

        bottomBar = {

            ClientBottomBar(
                currentRoute = Routes.DASHBOARD,
                onHomeClick = {},
                onCatalogClick = onCatalogClick,
                onMapClick = onMapClick,
                onRequestsClick = onRequestsClick,
                onProfileClick = onProfileClick
            )
        }

    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 16.dp
            ),

            verticalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {


            // =================================================
            // ENCABEZADO
            // =================================================

            item {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Person,

                            contentDescription =
                                "Perfil",

                            modifier =
                                Modifier.padding(12.dp)
                        )
                    }


                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    ) {

                        Text(
                            text = "Hola",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                        Text(
                            text = "Bienvenida a Lou Araya",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    IconButton(
                        onClick =
                            onNotificationsClick
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Notifications,

                            contentDescription =
                                "Notificaciones"
                        )
                    }
                }
            }


            // =================================================
            // COLECCIÓN DESTACADA
            // =================================================

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(20.dp)
                    ) {

                        Text(
                            text =
                                "Colección destacada",

                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(
                            text =
                                "Bodas de Ensueño",

                            style =
                                MaterialTheme
                                    .typography
                                    .headlineSmall,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Diseños únicos para crear espacios memorables.",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,

                            modifier =
                                Modifier.padding(
                                    top = 6.dp,
                                    bottom = 14.dp
                                )
                        )

                        Button(
                            onClick =
                                onCatalogClick
                        ) {

                            Text("Ver colección")
                        }
                    }
                }
            }


            // =================================================
            // ACCESOS RÁPIDOS
            // =================================================

            item {

                Text(
                    text = "Accesos rápidos",
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            item {

                val accesos =
                    listOf(

                        QuickAccessItem(
                            title = "Catálogo",
                            icon =
                                Icons.Default.Storefront,
                            onClick =
                                onCatalogClick
                        ),

                        QuickAccessItem(
                            title = "Mapa",
                            icon =
                                Icons.Default.Map,
                            onClick =
                                onMapClick
                        ),

                        QuickAccessItem(
                            title = "Escanear QR",
                            icon =
                                Icons.Default.QrCodeScanner,
                            onClick =
                                onQrClick
                        ),

                        QuickAccessItem(
                            title = "Solicitudes",
                            icon =
                                Icons.Default.ReceiptLong,
                            onClick =
                                onRequestsClick
                        ),

                        QuickAccessItem(
                            title = "Perfil",
                            icon =
                                Icons.Default.Person,
                            onClick =
                                onProfileClick
                        )
                    )


                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(accesos) { item ->

                        QuickAccessCard(
                            item = item
                        )
                    }
                }
            }


            // =================================================
            // PRODUCTOS DESTACADOS
            // =================================================

            item {

                Text(
                    text =
                        "Productos destacados",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.Bold
                )
            }


            item {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    ProductPreviewCard(
                        name =
                            "Tapiz mural",

                        category =
                            "Decoración",

                        price =
                            "$65.000",

                        modifier =
                            Modifier.weight(1f)
                    )


                    ProductPreviewCard(
                        name =
                            "Arco de ceremonia",

                        category =
                            "Matrimonios",

                        price =
                            "$120.000",

                        modifier =
                            Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
fun QuickAccessCard(
    item: QuickAccessItem
) {

    Card(
        onClick = item.onClick,

        shape =
            RoundedCornerShape(16.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    item.icon,

                contentDescription =
                    item.title
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    item.title,

                style =
                    MaterialTheme
                        .typography
                        .labelLarge
            )
        }
    }
}


@Composable
fun ProductPreviewCard(
    name: String,
    category: String,
    price: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,

        shape =
            RoundedCornerShape(18.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Icon(
                imageVector =
                    Icons.Default.Favorite,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Text(
                text = name,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text = category,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = price,

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}