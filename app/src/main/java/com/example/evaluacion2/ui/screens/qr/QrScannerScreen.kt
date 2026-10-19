package com.example.evaluacion2.ui.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.evaluacion2.data.repository.ProductRepository
import com.example.evaluacion2.domain.validation.ProductQrPayload
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalGetImage
@Composable
fun QrScannerScreen(
    repository: ProductRepository,
    onBack: () -> Unit,
    onProduct: (Int) -> Unit,
    onRecord: (Int?) -> Unit,
    currentRoute: String? = null,
    onHome: () -> Unit = {},
    onCatalog: () -> Unit = {},
    onMap: () -> Unit = {},
    onRequests: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var message by remember { mutableStateOf("Apunta la cámara a un código QR de producto.") }
    var scanning by remember { mutableStateOf(true) }
    var flashlightAvailable by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var galleryMessage by remember { mutableStateOf<String?>(null) }
    var cameraController by remember { mutableStateOf<Camera?>(null) }
    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runCatching {
                val image = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
                scanner.process(image).addOnSuccessListener { codes ->
                    val id = ProductQrPayload.parseProductId(codes.firstOrNull()?.rawValue)
                    val product = id?.let(repository::findById)
                    if (product == null) galleryMessage = "No se encontró un producto válido en la imagen."
                    else { onRecord(id); onProduct(id) }
                    scanner.close()
                }.addOnFailureListener { galleryMessage = "No se pudo leer el QR de la imagen."; scanner.close() }
            }.onFailure { galleryMessage = "No se pudo abrir la imagen seleccionada." }
        }
    }
    var lastHandledAt by remember { mutableLongStateOf(0L) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (!granted) message = "Permiso de cámara denegado. Habilítalo en Ajustes para escanear códigos QR."
    }
    LaunchedEffect(Unit) { if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    DisposableEffect(lifecycleOwner, hasPermission, scanning) {
        if (!hasPermission || !scanning) return@DisposableEffect onDispose { }
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build())
        var provider: ProcessCameraProvider? = null
        providerFuture.addListener({
            runCatching {
                provider = providerFuture.get()
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                analysis.setAnalyzer(executor) { proxy ->
                    val mediaImage = proxy.image
                    if (mediaImage == null) proxy.close() else {
                        scanner.process(InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees))
                            .addOnSuccessListener { barcodes ->
                                val value = barcodes.firstOrNull()?.rawValue ?: return@addOnSuccessListener
                                val now = System.currentTimeMillis()
                                if (now - lastHandledAt < 1800) return@addOnSuccessListener
                                lastHandledAt = now
                                val id = ProductQrPayload.parseProductId(value)
                                val product = id?.let(repository::findById)
                                if (product == null) message = "Código QR inválido o producto inexistente."
                                else {
                                    scanning = false
                                    message = "Producto encontrado: ${product.name}"
                                    onRecord(id)
                                    onProduct(id)
                                }
                            }
                            .addOnFailureListener { message = "No se pudo leer el código QR." }
                            .addOnCompleteListener { proxy.close() }
                    }
                }
                provider?.unbindAll()
                cameraController = provider?.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                flashlightAvailable = cameraController?.cameraInfo?.hasFlashUnit() == true
                cameraController?.cameraControl?.enableTorch(torchEnabled)
            }.onFailure { message = "No se pudo iniciar la cámara: ${it.localizedMessage.orEmpty()}" }
        }, ContextCompat.getMainExecutor(context))
        onDispose { provider?.unbindAll(); scanner.close() }
    }
    LaunchedEffect(torchEnabled, cameraController) { cameraController?.cameraControl?.enableTorch(torchEnabled) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Escanear código QR") }, navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }) },
        bottomBar = { ClientBottomBar(currentRoute ?: Routes.QR_SCANNER, onHome, onCatalog, onMap, onRequests, onProfile) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = if (message.startsWith("Código") || message.startsWith("Permiso") || message.startsWith("No se")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            if (!hasPermission) Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Solicitar permiso de cámara") }
            galleryMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (hasPermission && scanning) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)))
                    Text("▢", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                }
                Text("Enfoca el código QR del producto o etiqueta de la feria")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (flashlightAvailable) torchEnabled = !torchEnabled else galleryMessage = "Este dispositivo no cuenta con linterna." }, enabled = flashlightAvailable, modifier = Modifier.weight(1f)) {
                        Icon(if (torchEnabled) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn, contentDescription = null)
                        Text(if (torchEnabled) " Linterna apagada" else " Linterna")
                    }
                    OutlinedButton(onClick = { galleryPicker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text("Galería") }
                }
            }
            if (hasPermission && !scanning) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("Escaneo completado") }
            if (!scanning) Button(onClick = { scanning = true; message = "Apunta la cámara a un código QR de producto." }) { Text("Escanear otro") }
        }
    }
}
