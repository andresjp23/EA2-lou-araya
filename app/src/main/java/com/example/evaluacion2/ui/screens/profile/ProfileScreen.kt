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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evaluacion2.data.local.SessionManager
import com.example.evaluacion2.data.repository.UserPreferencesRepository
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.domain.validation.UserValidator
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
fun ProfileScreen(userId: Int, userRepository: UserRepository, preferencesRepository: UserPreferencesRepository, sessionManager: SessionManager, onBack: () -> Unit, onLogout: () -> Unit, onHome: () -> Unit = {}, onCatalog: () -> Unit = {}, onRequests: () -> Unit = {}, onNotifications: () -> Unit = {}, isAdmin: Boolean = sessionManager.getUserRole() == User.ROLE_ADMIN, onProducts: () -> Unit = {}, onCampaigns: () -> Unit = {}) {
    val context = LocalContext.current
    val vm: ProfileViewModel = viewModel(key = "profile-$userId") { ProfileViewModel(userId, userRepository, preferencesRepository, sessionManager) }
    val user by vm.user.collectAsState()
    val notificationsEnabled by vm.notificationsEnabled.collectAsState()
    val message by vm.message.collectAsState()
    var showEdit by remember { mutableStateOf(false) }
    var showChangeUser by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.setNotificationsEnabled(granted)
        if (granted) LocalNotificationHelper.show(context, "Notificaciones activadas", "Recibirás avisos locales de campañas y novedades.")
    }
    LaunchedEffect(Unit) { vm.load() }
    Scaffold(bottomBar = { if (isAdmin) AdminBottomBar(Routes.PROFILE, onHome, onProducts, onCampaigns, onRequests, {}) else ClientBottomBar(Routes.PROFILE, onHome, onCatalog, {}, onRequests, {}) }) { inset ->
        Column(Modifier.fillMaxSize().padding(inset).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Perfil y preferencias", style = MaterialTheme.typography.headlineMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card { Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.padding(14.dp)) }
                Column { Text(user?.name ?: "Cargando perfil…", style = MaterialTheme.typography.titleLarge); Text(user?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium) }
            }
            Text("Alias: ${user?.alias.orEmpty()}")
            Text("Correo: ${user?.email.orEmpty()}")
            Text("Teléfono: ${user?.phone.orEmpty()}")
            Text("Rol: ${user?.role.orEmpty()}")
            Text("Permisos", style = MaterialTheme.typography.titleMedium)
            Text("Cámara: ${if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) "Permitida" else "No concedida"}")
            Text("Ubicación: ${if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) "Permitida" else "No concedida"}")
            TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }, modifier = Modifier.fillMaxWidth()) { Text("Configurar permisos del dispositivo") }
            Text("Preferencias de productos", style = MaterialTheme.typography.titleMedium)
            Text("Tus favoritos y solicitudes se guardan por perfil en este dispositivo.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { showEdit = true }, modifier = Modifier.fillMaxWidth()) { Text("Mis datos personales · Editar") }
            TextButton(onClick = { showEdit = true }, modifier = Modifier.fillMaxWidth()) { Text("Dirección de envío") }
            TextButton(onClick = { showEdit = true }, modifier = Modifier.fillMaxWidth()) { Text("Ayuda y soporte") }
            Button(onClick = { showEdit = true }, modifier = Modifier.fillMaxWidth()) { Text("Editar perfil") }
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
            Button(onClick = { LocalNotificationHelper.show(context, "Lou Araya", "Las notificaciones locales están listas.") }, enabled = notificationsEnabled, modifier = Modifier.fillMaxWidth()) { Text("Probar notificación local") }
            TextButton(onClick = { showChangeUser = true }, modifier = Modifier.fillMaxWidth()) { Text("Cambiar usuario") }
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
            if (message != null) Text(message.orEmpty(), color = MaterialTheme.colorScheme.primary)
        }
    }
    if (showEdit && user != null) ProfileEditorDialog(user!!, onDismiss = { showEdit = false }) { name, alias, phone ->
        if (vm.updateProfile(name, alias, phone)) showEdit = false
    }
    if (showChangeUser) AlertDialog(onDismissRequest = { showChangeUser = false }, title = { Text("Cambiar usuario") }, text = { Text("Se cerrará la sesión actual y podrás ingresar con otra cuenta guardada en este dispositivo.") }, confirmButton = { TextButton(onClick = onLogout) { Text("Continuar") } }, dismissButton = { TextButton(onClick = { showChangeUser = false }) { Text("Cancelar") } })
}

@Composable
private fun ProfileEditorDialog(user: User, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var name by remember(user) { mutableStateOf(user.name) }
    var alias by remember(user) { mutableStateOf(user.alias) }
    var phone by remember(user) { mutableStateOf(user.phone) }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Editar perfil") }, text = {
        Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, isError = errors.containsKey("name"), supportingText = { errors["name"]?.let { Text(it) } })
            OutlinedTextField(alias, { alias = it }, label = { Text("Alias") }, isError = errors.containsKey("alias"), supportingText = { errors["alias"]?.let { Text(it) } })
            OutlinedTextField(phone, { phone = it }, label = { Text("Teléfono") }, isError = errors.containsKey("phone"), supportingText = { errors["phone"]?.let { Text(it) } })
            Text("El correo y la contraseña no se modifican desde esta pantalla.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = {
        errors = buildMap {
            UserValidator.validateName(name)?.let { put("name", it) }
            UserValidator.validateAlias(alias)?.let { put("alias", it) }
            UserValidator.validatePhone(phone)?.let { put("phone", it) }
        }
        if (errors.isEmpty()) onSave(name.trim(), alias.trim(), phone.trim())
    }) { Text("Guardar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
