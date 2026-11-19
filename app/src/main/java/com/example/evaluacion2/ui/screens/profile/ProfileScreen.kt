package com.example.evaluacion2.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.example.evaluacion2.ui.components.StateContent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.evaluacion2.domain.model.User
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import com.example.evaluacion2.notifications.LocalNotificationHelper
import com.example.evaluacion2.viewmodel.ProfileViewModel
import com.example.evaluacion2.ui.components.AdminBottomBar
import com.example.evaluacion2.ui.components.ClientBottomBar
import com.example.evaluacion2.ui.navigation.Routes

@Composable
fun ProfileScreen(vm: ProfileViewModel, isAdmin: Boolean, onBack: () -> Unit, onLogout: () -> Unit, onHome: () -> Unit = {}, onCatalog: () -> Unit = {}, onRequests: () -> Unit = {}, onNotifications: () -> Unit = {}, onProducts: () -> Unit = {}, onCampaigns: () -> Unit = {}) {
    val context = LocalContext.current
    val state by vm.user.collectAsState()
    val notificationsEnabled by vm.notificationsEnabled.collectAsState()
    val message by vm.message.collectAsState()
    val formErrors by vm.formErrors.collectAsState()
    val saved by vm.saved.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showEdit by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.setNotificationsEnabled(granted)
        if (granted) LocalNotificationHelper.show(context, "Notificaciones activadas", "Recibirás avisos locales de campañas y novedades.")
    }
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); vm.clearMessage() } }
    LaunchedEffect(saved) { if (saved) { showEdit = false; vm.editHandled() } }
    Scaffold(
        bottomBar = { if (isAdmin) AdminBottomBar(Routes.PROFILE) else ClientBottomBar(Routes.PROFILE) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inset ->
        StateContent(state, onRetry = vm::load, modifier = Modifier.padding(inset)) { user ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Perfil y preferencias", style = MaterialTheme.typography.headlineMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card { Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.padding(14.dp)) }
                    Column { Text(user.name, style = MaterialTheme.typography.titleLarge); Text(user.email, style = MaterialTheme.typography.bodyMedium) }
                }
                Text("Alias: ${user.alias}")
                Text("Correo: ${user.email}")
                Text("Teléfono: ${user.phone}")
                Text("Rol: ${user.userRole.label}")
                Text("Permisos", style = MaterialTheme.typography.titleMedium)
                Text("Cámara: ${if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) "Permitida" else "No concedida"}")
                Text("Ubicación: ${if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) "Permitida" else "No concedida"}")
                TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }, modifier = Modifier.fillMaxWidth()) { Text("Configurar permisos del dispositivo") }
                Button(onClick = { vm.editHandled(); showEdit = true }, modifier = Modifier.fillMaxWidth()) { Text("Editar perfil") }
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                        Text("Avisos locales sobre novedades y campañas", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { enabled ->
                        if (!enabled) vm.setNotificationsEnabled(false)
                        else if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else vm.setNotificationsEnabled(true)
                    }, modifier = Modifier.semantics { contentDescription = "Preferencia de notificaciones" })
                }
            }
            if (showEdit) ProfileEditorDialog(user, formErrors, onDismiss = { showEdit = false; vm.editHandled() }, onSave = vm::updateProfile)
        }
    }
}

@Composable
private fun ProfileEditorDialog(user: User, errors: Map<String, String>, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember(user) { mutableStateOf(user.name) }
    var alias by remember(user) { mutableStateOf(user.alias) }
    var phone by remember(user) { mutableStateOf(user.phone) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Editar perfil") }, text = {
        Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileField(name, { name = it }, "Nombre", errors["name"])
            ProfileField(alias, { alias = it }, "Alias", errors["alias"])
            ProfileField(phone, { phone = it }, "Teléfono", errors["phone"])
            Text("El correo y la contraseña no se modifican desde esta pantalla.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = { onSave(name.trim(), alias.trim(), phone.trim()) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable
private fun ProfileField(value: String, onValueChange: (String) -> Unit, label: String, error: String?) {
    OutlinedTextField(
        value, onValueChange, label = { Text(label) }, isError = error != null,
        trailingIcon = { if (error != null) Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) },
        supportingText = { error?.let { Text(it) } }
    )
}
