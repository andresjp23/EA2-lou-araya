package com.example.evaluacion2.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Navegación de las barras inferiores. Se entrega a toda la app con [LocalAppNavigator],
 * así cada pantalla no tiene que recibir (y conectar) una función por botón.
 */
class AppNavigator(val navigate: (String) -> Unit, val home: () -> Unit)

val LocalAppNavigator = staticCompositionLocalOf { AppNavigator(navigate = {}, home = {}) }
