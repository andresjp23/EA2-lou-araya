package com.example.evaluacion2.ui.screens.admin

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.evaluacion2.data.repository.ProductRepository
import com.example.evaluacion2.domain.model.Product
import com.example.evaluacion2.domain.validation.ProductValidator
import com.example.evaluacion2.ui.components.AdminBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(repository: ProductRepository, onBack: () -> Unit, currentRoute: String? = null, onHome: () -> Unit = {}, onCampaigns: () -> Unit = {}, onRequests: () -> Unit = {}, onProfile: () -> Unit = {}) {
    var products by remember { mutableStateOf(repository.getProducts()) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    val visibleProducts = products.filter { it.name.contains(query, true) && (filter == "Todos" || (filter == "Activos") == it.available) }
    var editing by remember { mutableStateOf<Product?>(null) }
    var creating by remember { mutableStateOf(false) }
    var imageCameraOpen by remember { mutableStateOf(false) }
    var pendingImage by remember { mutableStateOf<String?>(null) }
    var pickImage by remember { mutableStateOf(false) }
    fun refresh() { products = repository.getProducts() }
    Scaffold(topBar = { TopAppBar(title = { Text("Gestión de productos") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) }, bottomBar = { AdminBottomBar(currentRoute, onHome, {}, onCampaigns, onRequests, onProfile) }, floatingActionButton = { Button(onClick = { editing = null; creating = true }) { Text("+ Crear nuevo producto") } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), label = { Text("Buscar productos...") }, singleLine = true)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Activos", "Inactivos").forEach { value -> FilterChip(selected = filter == value, onClick = { filter = value }, label = { Text(value) }) }
        }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(visibleProducts, key = { it.id }) { product ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text("${product.category} · $${product.price}")
                        Text(if (product.available) "Disponible" else "No disponible")
                        Row {
                            TextButton(onClick = { repository.updateAvailable(product.id, !product.available); refresh() }) { Text(if (product.available) "Desactivar" else "Activar") }
                            TextButton(onClick = { editing = product; creating = true }) { Text("Editar") }
                        }
                    }
                }
            }
            if (visibleProducts.isEmpty()) item { Text("Aún no hay productos para este filtro.") }
        }
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pendingImage = uri.toString()
    }
    LaunchedEffect(pickImage) { if (pickImage) { picker.launch("image/*"); pickImage = false } }
    if (creating) ProductEditorDialog(
        initial = editing,
        imageUri = pendingImage ?: editing?.imageUri,
        onDismiss = { creating = false; pendingImage = null },
        onSave = { product ->
            val result = repository.save(product.copy(imageUri = pendingImage ?: product.imageUri))
            if (result != -1L) { creating = false; pendingImage = null; refresh() }
        },
        onCamera = { imageCameraOpen = true },
        onPickImage = { pickImage = true }
    )
    if (imageCameraOpen) CameraCaptureDialog(
        onDismiss = { imageCameraOpen = false },
        onCaptured = { uri -> pendingImage = uri.toString(); imageCameraOpen = false },
        onPick = { uri -> pendingImage = uri.toString(); imageCameraOpen = false }
    )
}

@Composable
private fun ProductEditorDialog(
    initial: Product?, imageUri: String?, onDismiss: () -> Unit, onSave: (Product) -> Unit,
    onCamera: () -> Unit, onPickImage: () -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var category by remember(initial) { mutableStateOf(initial?.category.orEmpty()) }
    var price by remember(initial) { mutableStateOf(initial?.price?.toString().orEmpty()) }
    var dimensions by remember(initial) { mutableStateOf(initial?.dimensions.orEmpty()) }
    var materials by remember(initial) { mutableStateOf(initial?.materials.orEmpty()) }
    var collection by remember(initial) { mutableStateOf(initial?.collection.orEmpty()) }
    var available by remember(initial) { mutableStateOf(initial?.available ?: true) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true) }
            item { OutlinedTextField(category, { category = it }, label = { Text("Categoría") }, singleLine = true) }
            item { OutlinedTextField(price, { price = it.filter(Char::isDigit) }, label = { Text("Precio") }, singleLine = true) }
            item { OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, minLines = 2) }
            item { OutlinedTextField(dimensions, { dimensions = it }, label = { Text("Dimensiones") }, singleLine = true) }
            item { OutlinedTextField(materials, { materials = it }, label = { Text("Materiales") }, singleLine = true) }
            item { OutlinedTextField(collection, { collection = it }, label = { Text("Colección") }, singleLine = true) }
            item { Row { Checkbox(available, { available = it }); Text("Disponible", Modifier.padding(top = 12.dp)) } }
            item { Row { TextButton(onClick = onCamera) { Text("Tomar foto") }; TextButton(onClick = onPickImage) { Text("Elegir imagen") } } }
            item { Text(if (imageUri != null) "Imagen seleccionada" else "Sin imagen") }
            item { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        }
    }, confirmButton = { TextButton(onClick = {
        val candidate = Product(id = initial?.id ?: 0, name = name.trim(), description = description.trim(), dimensions = dimensions.trim(), materials = materials.trim(), category = category.trim(), price = price.toIntOrNull() ?: 0, available = available, imageUri = imageUri, collection = collection.trim().ifBlank { null })
        val errors = ProductValidator.validate(candidate)
        error = errors.values.firstOrNull()
        if (errors.isEmpty()) onSave(candidate)
    }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
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
