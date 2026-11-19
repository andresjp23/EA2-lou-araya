package com.example.evaluacion2.di

import android.content.Context
import com.example.evaluacion2.BuildConfig
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.data.local.EventRepository
import com.example.evaluacion2.data.local.FavoriteRepository
import com.example.evaluacion2.data.local.NotificationRepository
import com.example.evaluacion2.data.local.ProductLocalDataSource
import com.example.evaluacion2.data.local.QrScanRepository
import com.example.evaluacion2.data.local.SessionManager
import com.example.evaluacion2.data.local.UserLocalDataSource
import com.example.evaluacion2.data.local.UserPreferencesRepository
import com.example.evaluacion2.data.remote.ApiClient
import com.example.evaluacion2.data.remote.api.CatalogoApi
import com.example.evaluacion2.data.remote.api.SolicitudesApi
import com.example.evaluacion2.data.remote.api.UsuariosApi
import com.example.evaluacion2.data.repository.RemoteCampaignRepository
import com.example.evaluacion2.data.repository.RemoteProductRepository
import com.example.evaluacion2.data.repository.RemoteRequestRepository
import com.example.evaluacion2.data.repository.RemoteUserRepository
import com.example.evaluacion2.domain.repository.CampaignRepository
import com.example.evaluacion2.domain.repository.ProductRepository
import com.example.evaluacion2.domain.repository.RequestRepository
import com.example.evaluacion2.domain.repository.UserRepository

/**
 * Crea una sola vez las dependencias de la app (inyección de dependencias manual).
 * Las pantallas piden aquí lo que necesitan en vez de construir repositorios por su cuenta.
 *
 * - Remoto (microservicios): usuarios, productos, campañas y solicitudes.
 * - Local (SQLite / SharedPreferences): sesión, caché de productos y perfil, favoritos,
 *   preferencias, notificaciones, historial de QR y ferias del mapa.
 */
class AppContainer(context: Context) {
    private val database = AppDatabaseHelper(context)

    val sessionManager = SessionManager(context)

    private val usuariosApi: UsuariosApi = ApiClient.create(BuildConfig.USUARIOS_BASE_URL, UsuariosApi::class.java)
    private val catalogoApi: CatalogoApi = ApiClient.create(BuildConfig.CATALOGO_BASE_URL, CatalogoApi::class.java)
    private val solicitudesApi: SolicitudesApi = ApiClient.create(BuildConfig.SOLICITUDES_BASE_URL, SolicitudesApi::class.java)

    val userRepository: UserRepository = RemoteUserRepository(usuariosApi, UserLocalDataSource(database))
    val productRepository: ProductRepository = RemoteProductRepository(catalogoApi, ProductLocalDataSource(database))
    val campaignRepository: CampaignRepository = RemoteCampaignRepository(catalogoApi)
    val requestRepository: RequestRepository = RemoteRequestRepository(solicitudesApi)

    val favoriteRepository = FavoriteRepository(database)
    val preferencesRepository = UserPreferencesRepository(database)
    val notificationRepository = NotificationRepository(database)
    val eventRepository = EventRepository(database)
    val qrScanRepository = QrScanRepository(database)
}
