package com.example.evaluacion2.ui.screens.admin

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.validation.ProductQrPayload
import com.example.evaluacion2.ui.components.AdminBottomBar
import com.example.evaluacion2.ui.components.ProductImage
import com.example.evaluacion2.ui.components.ProductQrCode
import com.example.evaluacion2.ui.components.StateContent
import com.example.evaluacion2.viewmodel.AdminProductsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    vm: AdminProductsViewModel,
    onBack: () -> Unit,
    currentRoute: String? = null,
    onHome: () -> Unit = {},
    onCampaigns: () -> Unit = {},
    onRequests: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val state by vm.products.collectAsState()
    val formErrors by vm.formErrors.collectAsState()
    val saved by vm.saved.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }
    var imageCameraOpen by remember { mutableStateOf(false) }
    var pendingImage by remember { mutableStateOf<String?>(null) }
    var pickImage by remember { mutableStateOf(false) }
    var qrProduct by remember { mutableStateOf<Product?>(null) }
    var toDelete by remember { mutableStateOf<Product?>(null) }
    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
    LaunchedEffect(saved) { if (saved) { creating = false; pendingImage = null; vm.formHandled() } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Gestión de productos") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) },
        bottomBar = { AdminBottomBar(currentRoute) },
        floatingActionButton = { Button(onClick = { editing = null; vm.formHandled(); creating = true }) { Text("+ Crear nuevo producto") } },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("Buscar productos...") }, singleLine = true)
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminProductsViewModel.FILTERS.forEach { value -> FilterChip(selected = filter == value, onClick = { filter = value }, label = { Text(value) }) }
            }
            StateContent(state, onRetry = vm::refresh) { products ->
                val visibleProducts = AdminProductsViewModel.filter(products, query, filter)
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleProducts, key = { it.id }) { product ->
                        Card(Modifier.fillMaxWidth().animateItem()) {
                            Column {
                                ProductImage(product = product, modifier = Modifier.fillMaxWidth().height(170.dp))
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${product.category} · $${product.price}")
                                    Text(if (product.available) "Disponible" else "No disponible", color = if (product.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                    Row {
                                        TextButton(onClick = { vm.toggleAvailable(product) }) { Text(if (product.available) "Desactivar" else "Activar") }
                                        TextButton(onClick = { editing = product; vm.formHandled(); creating = true }) { Text("Editar") }
                                        TextButton(onClick = { qrProduct = product }) { Text("QR") }
                                        TextButton(onClick = { toDelete = product }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                                    }
                                }
                            }
                        }
                    }
                    if (visibleProducts.isEmpty()) item { Text("Aún no hay productos para este filtro.") }
                }
            }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) pendingImage = uri.toString() }
    LaunchedEffect(pickImage) { if (pickImage) { picker.launch("image/*"); pickImage = false } }
    if (creating) ProductEditorDialog(
        initial = editing,
        imageUri = pendingImage ?: editing?.imageUri,
        errors = formErrors,
        onDismiss = { creating = false; pendingImage = null; vm.formHandled() },
        onSave = { product -> vm.save(product.copy(imageUri = pendingImage ?: product.imageUri)) },
        onCamera = { imageCameraOpen = true },
        onPickImage = { pickImage = true }
    )
    if (imageCameraOpen) CameraCaptureDialog(
        onDismiss = { imageCameraOpen = false },
        onCaptured = { uri -> pendingImage = uri.toString(); imageCameraOpen = false },
        onPick = { uri -> pendingImage = uri.toString(); imageCameraOpen = false }
    )
    qrProduct?.let { product ->
        ProductQrDialog(product = product, onDismiss = { qrProduct = null })
    }
    toDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar \"${product.name}\" del catálogo? Esta acción no se puede deshacer.") },
            confirmButton = { TextButton(onClick = { vm.delete(product); toDelete = null }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProductQrDialog(product: Product, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR de ${product.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductQrCode(
                    productId = product.id,
                    productName = product.name,
                    modifier = Modifier.size(240.dp)
                )
                Text("Al escanearlo desde la app se abrirá el detalle de ${product.name}.")
                Text(ProductQrPayload.forProduct(product.id), style = MaterialTheme.typography.labelMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun ProductEditorDialog(initial: Product?, imageUri: String?, errors: Map<String, String>, onDismiss: () -> Unit, onSave: (Product) -> Unit, onCamera: () -> Unit, onPickImage: () -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var category by remember(initial) { mutableStateOf(initial?.category.orEmpty()) }
    var price by remember(initial) { mutableStateOf(initial?.price?.toString().orEmpty()) }
    var dimensions by remember(initial) { mutableStateOf(initial?.dimensions.orEmpty()) }
    var materials by remember(initial) { mutableStateOf(initial?.materials.orEmpty()) }
    var collection by remember(initial) { mutableStateOf(initial?.collection.orEmpty()) }
    var available by remember(initial) { mutableStateOf(initial?.available ?: true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { ValidatedField(name, { name = it }, "Nombre", errors["name"]) }
            item { ValidatedField(category, { category = it }, "Categoría", errors["category"]) }
            item { ValidatedField(price, { price = it.filter(Char::isDigit) }, "Precio", errors["price"]) }
            item { OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, minLines = 2) }
            item { OutlinedTextField(dimensions, { dimensions = it }, label = { Text("Dimensiones") }, singleLine = true) }
            item { OutlinedTextField(materials, { materials = it }, label = { Text("Materiales") }, singleLine = true) }
            item { OutlinedTextField(collection, { collection = it }, label = { Text("Colección") }, singleLine = true) }
            item { Row { Checkbox(available, { available = it }); Text("Disponible", Modifier.padding(top = 12.dp)) } }
            item { Row { TextButton(onClick = onCamera) { Text("Tomar foto") }; TextButton(onClick = onPickImage) { Text("Elegir imagen") } } }
            item { Text(if (imageUri != null) "Imagen seleccionada" else "Sin imagen") }
        }
    }, confirmButton = { TextButton(onClick = {
        onSave(Product(id = initial?.id ?: 0, name = name.trim(), description = description.trim(), dimensions = dimensions.trim(), materials = materials.trim(), category = category.trim(), price = price.toIntOrNull() ?: -1, available = available, imageUri = imageUri, collection = collection.trim().ifBlank { null }))
    }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

/** Campo de texto con ícono y mensaje de error cuando la validación falla. */
@Composable
private fun ValidatedField(value: String, onValueChange: (String) -> Unit, label: String, error: String?) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        trailingIcon = { if (error != null) Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) },
        supportingText = { error?.let { Text(it) } }
    )
}

@Composable
private fun CameraCaptureDialog(onDismiss: () -> Unit, onCaptured: (Uri) -> Unit, onPick: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) onPick(uri) }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA) }
    val previewView = remember { PreviewView(context) }
    DisposableEffect(granted, lifecycleOwner) {
        if (!granted) return@DisposableEffect onDispose { }
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        future.addListener({
            runCatching {
                provider = future.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val capture = ImageCapture.Builder().build()
                imageCapture = capture
                provider?.unbindAll()
                provider?.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
            }.onFailure { captureError = it.localizedMessage ?: "No se pudo iniciar la cámara" }
        }, ContextCompat.getMainExecutor(context))
        onDispose { provider?.unbindAll() }
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Foto del producto") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (granted) AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            else Text("Sin permiso de cámara. Puedes elegir una imagen existente.")
            captureError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        Row {
            TextButton(onClick = { picker.launch("image/*") }) { Text("Elegir imagen") }
            if (granted) TextButton(onClick = {
                runCatching {
                    val uri = createImageUri(context)
                    val options = ImageCapture.OutputFileOptions.Builder(context.contentResolver, uri, ContentValues()).build()
                    imageCapture?.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(result: ImageCapture.OutputFileResults) { onCaptured(uri) }
                        override fun onError(exception: ImageCaptureException) { captureError = exception.localizedMessage ?: "No se pudo guardar la foto" }
                    }) ?: error("La cámara aún no está lista")
                }.onFailure { captureError = it.localizedMessage ?: "No se pudo capturar" }
            }) { Text("Capturar") }
        }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } })
}

private fun createImageUri(context: Context): Uri {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "product_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Evaluacion2")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: throw IllegalStateException("No se pudo crear el archivo de imagen")
}
