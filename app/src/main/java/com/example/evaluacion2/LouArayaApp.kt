package com.example.evaluacion2

import android.app.Application
import com.example.evaluacion2.di.AppContainer
import com.example.evaluacion2.notifications.LocalNotificationHelper
import kotlin.concurrent.thread

class LouArayaApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        LocalNotificationHelper.createChannel(this)
        thread(name = "seed-notifications") { seedInboxIfEmpty() }
    }

    /** Mensajes de bienvenida de la bandeja local (solo la primera vez). */
    private fun seedInboxIfEmpty() {
        val notifications = container.notificationRepository
        if (notifications.hasAny()) return
        listOf(
            Triple("Campañas", "Nueva campaña disponible", "Descubre nuestra colección Bodas de Ensueño."),
            Triple("Eventos", "Feria este fin de semana", "Te esperamos en el Parque Bicentenario, Vitacura."),
            Triple("Campañas", "Nuevo producto en catálogo", "Conoce nuestros nuevos maceteros colgantes."),
            Triple("Solicitudes", "Revisa tus solicitudes", "Puedes seguir el estado de tus cotizaciones en Mis Solicitudes.")
        ).forEach { (category, title, description) -> notifications.add(null, category, title, description, "19 Nov 2026") }
    }
}
