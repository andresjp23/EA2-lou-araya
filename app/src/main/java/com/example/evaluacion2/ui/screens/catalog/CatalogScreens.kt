package com.example.evaluacion2.ui.screens.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.model.QuoteRequest
import com.example.evaluacion2.ui.components.AdminBottomBar
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes
import com.example.evaluacion2.viewmodel.CatalogViewModel
import com.example.evaluacion2.viewmodel.FavoritesViewModel
import com.example.evaluacion2.viewmodel.QuoteViewModel
import com.example.evaluacion2.viewmodel.RequestsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun money(value: Int) = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")).format(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    vm: CatalogViewModel,
    onBack: () -> Unit,
    onProduct: (Int) -> Unit,
    isAdmin: Boolean = false,
    currentRoute: String? = null,
    onHome: () -> Unit = {},
    onMap: () -> Unit = {},
    onRequests: () -> Unit = {},
    onProfile: () -> Unit = {},
    onManageProduct: (Int) -> Unit = {}
) {
    val products by vm.products.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todas") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val filtered = products.filter {
        (category == "Todas" || it.category.contains(category, true) ||
            (category == "Interiores" && it.category.contains("Decor", true))) && it.name.contains(search, true)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Catálogo") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) },
        bottomBar = {
            if (isAdmin) AdminBottomBar(currentRoute, onHome, {}, {}, onRequests, onProfile)
            else ClientBottomBar(currentRoute ?: Routes.CATALOG, onHome, {}, onMap, onRequests, onProfile)
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text("Buscar diseños...") }, singleLine = true)
            LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Todos", "Matrimonios", "Festivales", "Interiores")) { item ->
                    FilterChip(selected = category == item || (item == "Todos" && category == "Todas"), onClick = { category = if (item == "Todos") "Todas" else item }, label = { Text(item) })
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(filtered, key = { it.id }) { product -> ProductCard(product, onClick = { if (isAdmin) selectedProduct = product else onProduct(product.id) }) }
                if (filtered.isEmpty()) item { Text("No hay productos que coincidan con la búsqueda.", Modifier.padding(16.dp)) }
            }
        }
    }
    if (isAdmin && selectedProduct != null) {
        AlertDialog(onDismissRequest = { selectedProduct = null }, title = { Text(selectedProduct?.name.orEmpty()) }, text = { Text("Gestiona este diseño desde la sección de productos.") }, confirmButton = { TextButton(onClick = { onManageProduct(selectedProduct!!.id); selectedProduct = null }) { Text("Editar producto") } }, dismissButton = { TextButton(onClick = { selectedProduct = null }) { Text("Cerrar") } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(product: Product?, isFavorite: Boolean, isHomeFavorite: Boolean, onToggleFavorite: () -> Unit, onToggleHomeFavorite: () -> Unit, onQuote: () -> Unit, onBack: () -> Unit, isAdmin: Boolean = false) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Detalle del producto") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } },
            actions = { if (!isAdmin) TextButton(onClick = onToggleHomeFavorite) { Icon(Icons.Default.Home, contentDescription = if (isHomeFavorite) "Quitar de inicio" else "Agregar a inicio") } }
        )
    }) { padding ->
        if (product == null) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Producto no encontrado") }
        else Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("★★★★★  ·  5,0 (12 reseñas)", color = MaterialTheme.colorScheme.primary)
            Text(product.category, style = MaterialTheme.typography.titleMedium)
            Text(money(product.price), style = MaterialTheme.typography.titleLarge)
            Text(if (product.available) "Disponible" else "No disponible", color = if (product.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            product.collection?.let { Text("Colección: $it") }
            Text(product.description)
            if (product.dimensions.isNotBlank()) Text("Dimensiones: ${product.dimensions}")
            if (product.materials.isNotBlank()) Text("Materiales: ${product.materials}")
            if (!isAdmin) {
                OutlinedButton(onClick = onToggleFavorite) { Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null); Text(if (isFavorite) " Quitar de favoritos" else " Agregar a favoritos") }
                OutlinedButton(onClick = onToggleHomeFavorite) { Icon(Icons.Default.Home, contentDescription = null); Text(if (isHomeFavorite) " Quitar del inicio" else " Mostrar en inicio") }
                Button(onClick = onQuote) { Text(if (product.available) "Solicitar producto / cotización" else "Solicitar trabajo a pedido") }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(product.category)
            Text(money(product.price), style = MaterialTheme.typography.titleSmall)
            product.collection?.let { Text("Colección: $it", style = MaterialTheme.typography.bodySmall) }
            Text(if (product.available) "Disponible" else "No disponible", color = if (product.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(vm: FavoritesViewModel, onBack: () -> Unit, onProduct: (Int) -> Unit, currentRoute: String? = null, onHome: () -> Unit = {}, onCatalog: () -> Unit = {}, onMap: () -> Unit = {}, onRequests: () -> Unit = {}, onProfile: () -> Unit = {}) {
    val products by vm.products.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Favoritos") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) },
        bottomBar = { ClientBottomBar(currentRoute ?: Routes.FAVORITES, onHome, onCatalog, onMap, onRequests, onProfile) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(products, key = { it.id }) { ProductCard(it) { onProduct(it.id) } }
            if (products.isEmpty()) item { Text("Aún no tienes productos favoritos.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteFormScreen(vm: QuoteViewModel, onBack: () -> Unit, onSuccess: () -> Unit, currentRoute: String? = null, onHome: () -> Unit = {}, onCatalog: () -> Unit = {}, onMap: () -> Unit = {}, onRequests: () -> Unit = {}, onProfile: () -> Unit = {}) {
    val state by vm.state.collectAsState()
    var showSuccess by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.successMessage) { if (state.successMessage != null) showSuccess = true }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Solicitar cotización") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) },
        bottomBar = { ClientBottomBar(currentRoute ?: Routes.QUOTE, onHome, onCatalog, onMap, onRequests, onProfile) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.productName.takeIf { it.isNotBlank() }?.let { Text("Producto: $it", style = MaterialTheme.typography.titleMedium) }
            SelectField("Tipo de evento", state.eventType, listOf("Matrimonio", "Celebración", "Decoración de hogar", "Otro"), vm::setEventType, state.errors["eventType"])
            Text("Tipo: diseño artesanal personalizado", style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(state.dimensions, vm::setDimensions, modifier = Modifier.fillMaxWidth(), label = { Text("Dimensiones aproximadas") }, placeholder = { Text("3 x 2 metros") }, isError = state.errors["dimensions"] != null, supportingText = state.errors["dimensions"]?.let { { Text(it) } })
            SelectField("Material preferido", state.material, listOf("Algodón natural", "Yute", "Lana", "Mezcla de fibras", "A definir"), vm::setMaterial, state.errors["material"])
            SelectField("Color o paleta de colores", state.colors, listOf("Tonos naturales", "Blanco y beige", "Tonos tierra", "Personalizados"), vm::setColors, state.errors["colors"])
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.requiredDate,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fecha requerida") },
                    placeholder = { Text("Selecciona una fecha") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Abrir calendario") },
                    isError = state.errors["requiredDate"] != null,
                    supportingText = state.errors["requiredDate"]?.let { { Text(it) } }
                )
                Box(Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            OutlinedTextField(value = state.observations, onValueChange = vm::setObservations, modifier = Modifier.fillMaxWidth(), label = { Text("Observaciones") }, isError = state.errors["observations"] != null, supportingText = state.errors["observations"]?.let { message -> { Text(message) } }, minLines = 3)
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = vm::submit, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) { Text(if (state.isSaving) "Guardando…" else "Enviar solicitud") }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.requiredDate.toUtcDateMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { vm.setRequiredDate(it.toIsoDate()) }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Selecciona la fecha requerida", Modifier.padding(16.dp)) }
            )
        }
    }
    if (showSuccess && state.successMessage != null) AlertDialog(onDismissRequest = { showSuccess = false; onSuccess() }, title = { Text("Solicitud enviada") }, text = { Text(state.successMessage.orEmpty()) }, confirmButton = { TextButton(onClick = { showSuccess = false; onSuccess() }) { Text("Ver solicitudes") } })
}

private fun String.toUtcDateMillis(): Long? = runCatching {
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(this)?.time
}.getOrNull()

private fun Long.toIsoDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}.format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectField(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit, error: String?) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), label = { Text(label) }, placeholder = { Text("Selecciona una opción") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, isError = error != null, supportingText = error?.let { { Text(it) } })
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(vm: RequestsViewModel, isAdmin: Boolean, onBack: () -> Unit, onQuote: () -> Unit, currentRoute: String? = null, onHome: () -> Unit = {}, onCatalog: () -> Unit = {}, onMap: () -> Unit = {}, onProfile: () -> Unit = {}, onNotifications: () -> Unit = {}, ownerName: (Int) -> String = { "Cliente" }) {
    val requests by vm.requests.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (isAdmin) "Gestión de Solicitudes" else "Mis Solicitudes") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) },
        bottomBar = { if (isAdmin) AdminBottomBar(currentRoute, onHome, {}, {}, onNotifications, onProfile) else ClientBottomBar(currentRoute ?: Routes.REQUESTS, onHome, onCatalog, onMap, onBack, onProfile) },
        floatingActionButton = { if (!isAdmin) FloatingActionButton(onClick = onQuote) { Text("+") } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            var requestFilter by remember { mutableStateOf("Todas") }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                items(listOf("Todas", "Exploración", "Pendiente", "Confirmada", "Entregada")) { status -> FilterChip(selected = requestFilter == status, onClick = { requestFilter = status }, label = { Text(status) }) }
            }
            val visibleRequests = requests.filter { requestFilter == "Todas" || it.status.contains(requestFilter, true) }
            LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(visibleRequests, key = { it.id }) { request -> RequestCard(request, isAdmin, vm::updateStatus, if (isAdmin) ownerName(request.userId) else null) }
                if (requests.isEmpty()) item { Text("No hay solicitudes registradas.") }
            }
        }
    }
}

@Composable
private fun RequestCard(request: QuoteRequest, isAdmin: Boolean, updateStatus: (Int, String) -> Unit, ownerName: String?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Solicitud #${request.id} · ${request.eventType}", style = MaterialTheme.typography.titleMedium)
            request.productId?.let { Text("Producto #$it") }
            ownerName?.let { Text("Cliente: $it") }
            Text("Estado: ${request.status}")
            Text("Creada: ${request.createdAt}")
            if (request.observations.isNotBlank()) Text(request.observations)
            if (isAdmin) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) { Text("Cambiar estado") }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        QuoteRequest.ALL_STATUSES.forEach { status -> DropdownMenuItem(text = { Text(status) }, onClick = { updateStatus(request.id, status); expanded = false }) }
                    }
                }
            }
        }
    }
}
