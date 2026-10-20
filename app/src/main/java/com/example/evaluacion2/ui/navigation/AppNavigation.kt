package com.example.evaluacion2.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.data.local.SessionManager
import com.example.evaluacion2.data.repository.CampaignRepository
import com.example.evaluacion2.data.repository.EventRepository
import com.example.evaluacion2.data.repository.FavoriteRepository
import com.example.evaluacion2.data.repository.NotificationRepository
import com.example.evaluacion2.data.repository.ProductRepository
import com.example.evaluacion2.data.repository.QrScanRepository
import com.example.evaluacion2.data.repository.RequestRepository
import com.example.evaluacion2.data.repository.UserPreferencesRepository
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.domain.model.User
import com.example.evaluacion2.notifications.LocalNotificationHelper
import com.example.evaluacion2.ui.screens.admin.AdminProductsScreen
import com.example.evaluacion2.ui.screens.admin.CampaignsScreen
import com.example.evaluacion2.ui.screens.catalog.CatalogScreen
import com.example.evaluacion2.ui.screens.catalog.FavoritesScreen
import com.example.evaluacion2.ui.screens.catalog.ProductDetailScreen
import com.example.evaluacion2.ui.screens.catalog.QuoteFormScreen
import com.example.evaluacion2.ui.screens.catalog.RequestsScreen
import com.example.evaluacion2.ui.screens.dashboard.AdminDashboardScreen
import com.example.evaluacion2.ui.screens.dashboard.DashboardScreen
import com.example.evaluacion2.ui.screens.login.LoginScreen
import com.example.evaluacion2.ui.screens.map.EventMapScreen
import com.example.evaluacion2.ui.screens.notifications.NotificationsScreen
import com.example.evaluacion2.ui.screens.profile.ProfileScreen
import com.example.evaluacion2.ui.screens.qr.QrScannerScreen
import com.example.evaluacion2.ui.screens.signin.SignInScreen
import com.example.evaluacion2.ui.screens.signup.SignUpScreen
import com.example.evaluacion2.viewmodel.CatalogViewModel
import com.example.evaluacion2.viewmodel.DashboardFavoritesViewModel
import com.example.evaluacion2.viewmodel.DetailViewModel
import com.example.evaluacion2.viewmodel.FavoritesViewModel
import com.example.evaluacion2.viewmodel.QuoteViewModel
import com.example.evaluacion2.viewmodel.RequestsViewModel
import com.example.evaluacion2.viewmodel.SignInViewModel
import com.example.evaluacion2.viewmodel.SignInViewModelFactory
import com.example.evaluacion2.viewmodel.SignUpViewModel
import com.example.evaluacion2.viewmodel.SignUpViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavigation() {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val databaseHelper = remember { AppDatabaseHelper(context) }
    val userRepository = remember { UserRepository(databaseHelper) }
    val productRepository = remember { ProductRepository(databaseHelper) }
    val favoriteRepository = remember { FavoriteRepository(databaseHelper) }
    val requestRepository = remember { RequestRepository(databaseHelper) }
    val eventRepository = remember { EventRepository(databaseHelper) }
    val campaignRepository = remember { CampaignRepository(databaseHelper) }
    val preferencesRepository = remember { UserPreferencesRepository(databaseHelper) }
    val notificationRepository = remember { NotificationRepository(databaseHelper) }
    LaunchedEffect(Unit) {
        LocalNotificationHelper.createChannel(context)
        if (!notificationRepository.hasAny()) {
            listOf(
                Triple("Campañas", "Nueva campaña disponible", "Descubre nuestra colección Bodas de Ensueño."),
                Triple("Solicitudes", "Tu solicitud fue confirmada", "#SOL-0013 – Arco de ceremonia personalizado ha sido confirmado."),
                Triple("Eventos", "Feria este fin de semana", "Te esperamos en el Parque Bicentenario, Vitacura."),
                Triple("Campañas", "Nuevo producto en catálogo", "Conoce nuestros nuevos maceteros colgantes."),
                Triple("Solicitudes", "Recordatorio de solicitud", "Tu solicitud sigue pendiente de revisión.")
            ).forEach { (category, title, description) -> notificationRepository.add(null, category, title, description, "12 Abr 2025") }
        }
    }
    val qrScanRepository = remember { QrScanRepository(databaseHelper) }
    val sessionManager = remember { SessionManager(context) }
    var sessionRevision by remember { mutableStateOf(0) }
    val initialRoute = remember {
        when {
            !sessionManager.isLoggedIn() -> Routes.WELCOME
            sessionManager.getUserRole() == User.ROLE_ADMIN -> Routes.DASHBOARD_ADMIN
            else -> Routes.DASHBOARD_CLIENT
        }
    }
    val currentName = remember(sessionRevision) { sessionManager.getUserName().orEmpty() }
    val currentRole = remember(sessionRevision) { sessionManager.getUserRole() ?: User.ROLE_CLIENT }
    val currentUserId = remember(sessionRevision) { sessionManager.getUserId() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val authenticated = sessionManager.isLoggedIn()
    fun dashboardFor(role: String) = if (role == User.ROLE_ADMIN) Routes.DASHBOARD_ADMIN else Routes.DASHBOARD_CLIENT
    fun navigateToRoleDashboard(role: String) {
        sessionRevision++
        navController.navigate(dashboardFor(role)) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }
    fun logout() {
        sessionManager.clearSession()
        sessionRevision++
        navController.navigate(Routes.WELCOME) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    BackHandler(enabled = authenticated && currentRoute == dashboardFor(currentRole)) { logout() }
    LaunchedEffect(currentRoute, authenticated, currentUserId) {
        val needsSession = currentRoute == Routes.PROFILE || currentRoute == Routes.NOTIFICATIONS || currentRoute == Routes.NOTIFICATION_INBOX || currentRoute == Routes.CAMPAIGNS || currentRoute == Routes.FAVORITES || currentRoute == Routes.REQUESTS ||
            currentRoute?.startsWith("${Routes.QUOTE}?") == true || currentRoute?.startsWith("${Routes.PRODUCT_DETAIL}/") == true
        if (needsSession && (!authenticated || currentUserId == SessionManager.NO_USER_ID)) {
            sessionManager.clearSession()
            sessionRevision++
            navController.navigate(Routes.WELCOME) { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
        }
    }

    NavHost(navController = navController, startDestination = initialRoute) {
        composable(Routes.WELCOME) {
            LoginScreen(onLoginClick = { navController.navigate(Routes.SIGN_IN) }, onCreateAccountClick = { navController.navigate(Routes.SIGN_UP) })
        }
        composable(Routes.SIGN_IN) {
            val vm: SignInViewModel = viewModel(factory = SignInViewModelFactory(userRepository, sessionManager))
            SignInScreen(vm, onLoginSuccess = ::navigateToRoleDashboard, onBackClick = { navController.popBackStack() })
        }
        composable(Routes.SIGN_UP) {
            val vm: SignUpViewModel = viewModel(factory = SignUpViewModelFactory(userRepository, sessionManager))
            SignUpScreen(vm, onRegisterSuccess = { navigateToRoleDashboard(sessionManager.getUserRole() ?: User.ROLE_CLIENT) }, onBackClick = { navController.popBackStack() })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(currentName, emptyList(), {}, { navController.navigate(Routes.FAVORITES) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.MAP) }, { navController.navigate(Routes.QR_SCANNER) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) }, { navController.navigate(Routes.NOTIFICATION_INBOX) }, ::logout, isAdmin = true)
        }
        composable(Routes.DASHBOARD_CLIENT) {
            val vm: DashboardFavoritesViewModel = viewModel(key = "home-favorites-$currentUserId") { DashboardFavoritesViewModel(currentUserId, favoriteRepository, productRepository) }
            val favoriteProducts by vm.favorites.collectAsState()
            val featuredProducts = remember(favoriteProducts, currentRoute) {
                (favoriteProducts + productRepository.getProducts().filter { it.available })
                    .distinctBy { it.id }
                    .take(4)
            }
            LaunchedEffect(currentRoute) { vm.refresh() }
            DashboardScreen(currentName, featuredProducts, { navController.navigate("${Routes.PRODUCT_DETAIL}/$it") }, { navController.navigate(Routes.FAVORITES) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.MAP) }, { navController.navigate(Routes.QR_SCANNER) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) }, { navController.navigate(Routes.NOTIFICATION_INBOX) }, ::logout)
        }
        composable(Routes.NOTIFICATION_INBOX) {
            val notifications = remember(currentUserId) { notificationRepository.getForUser(currentUserId) }
            NotificationsScreen(
                notifications = notifications,
                isAdmin = currentRole == User.ROLE_ADMIN,
                onBack = navController::popBackStack,
                onOpenRequests = { navController.navigate(Routes.REQUESTS) },
                onOpenEvents = { navController.navigate(Routes.MAP) },
                onRead = { notificationRepository.markRead(it, currentUserId) },
                onOpenCampaigns = { navController.navigate(Routes.CAMPAIGNS) },
                onHome = { navController.navigate(dashboardFor(currentRole)) },
                onCatalog = { navController.navigate(Routes.CATALOG) },
                onMap = { navController.navigate(Routes.MAP) },
                onProfile = { navController.navigate(Routes.PROFILE) },
                onProducts = { navController.navigate(Routes.ADMIN_PRODUCTS) },
                currentRoute = currentRoute
            )
        }
        composable(Routes.DASHBOARD_ADMIN) {
            AdminDashboardScreen(currentName, { navController.navigate(Routes.ADMIN_PRODUCTS) }, { navController.navigate(Routes.CAMPAIGNS) }, { navController.navigate(Routes.REQUESTS) }, ::logout, { navController.navigate(Routes.PROFILE) }, { navController.navigate(Routes.NOTIFICATION_INBOX) })
        }
        composable(Routes.CATALOG) {
            val vm: CatalogViewModel = viewModel { CatalogViewModel(productRepository) }
            CatalogScreen(vm, { navController.popBackStack() }, { navController.navigate("${Routes.PRODUCT_DETAIL}/$it") }, currentRole == User.ROLE_ADMIN, currentRoute, { navController.navigate(dashboardFor(currentRole)) }, { navController.navigate(Routes.MAP) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) }, { navController.navigate(Routes.ADMIN_PRODUCTS) })
        }
        composable("${Routes.PRODUCT_DETAIL}/{productId}", arguments = listOf(navArgument("productId") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("productId") ?: 0
            val product = remember(id) { productRepository.findById(id) }
            val vm: DetailViewModel = viewModel(key = "detail-$currentUserId-$id") { DetailViewModel(currentUserId, id, favoriteRepository) }
            val isFavorite by vm.isFavorite.collectAsState()
            LaunchedEffect(Unit) { vm.refresh() }
            ProductDetailScreen(product, isFavorite, vm::toggleFavorite, { navController.navigate("${Routes.QUOTE}?productId=$id") }, { navController.popBackStack() }, currentRole == User.ROLE_ADMIN)
        }
        composable(Routes.FAVORITES) {
            val vm: FavoritesViewModel = viewModel(key = "favorites-$currentUserId") { FavoritesViewModel(currentUserId, favoriteRepository, productRepository) }
            LaunchedEffect(Unit) { vm.refresh() }
            FavoritesScreen(vm, { navController.popBackStack() }, { navController.navigate("${Routes.PRODUCT_DETAIL}/$it") }, currentRoute, { navController.navigate(Routes.DASHBOARD_CLIENT) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.MAP) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) })
        }
        composable("${Routes.QUOTE}?productId={productId}", arguments = listOf(navArgument("productId") { type = NavType.IntType; defaultValue = 0 })) { entry ->
            val productId = entry.arguments?.getInt("productId")?.takeIf { it > 0 }
            val vm: QuoteViewModel = viewModel(key = "quote-${productId ?: 0}-${entry.id}") {
                QuoteViewModel(currentUserId, productId, requestRepository, productRepository)
            }
            QuoteFormScreen(vm, { navController.popBackStack() }, { navController.navigate(Routes.REQUESTS) { launchSingleTop = true } }, currentRoute, { navController.navigate(Routes.DASHBOARD_CLIENT) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.MAP) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) })
        }
        composable(Routes.REQUESTS) {
            val vm: RequestsViewModel = viewModel(key = "requests-$currentUserId-$currentRole") { RequestsViewModel(currentUserId, currentRole == User.ROLE_ADMIN, requestRepository) }
            LaunchedEffect(currentRoute) { vm.refresh() }
            RequestsScreen(vm, currentRole == User.ROLE_ADMIN, { navController.popBackStack() }, { navController.navigate(Routes.QUOTE) }, currentRoute, { navController.navigate(dashboardFor(currentRole)) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.MAP) }, { navController.navigate(Routes.PROFILE) }, { navController.navigate(Routes.NOTIFICATION_INBOX) }, requestRepository::getOwnerName) { productId ->
                productRepository.findById(productId)?.name ?: "Producto no disponible"
            }
        }
        composable(Routes.MAP) {
            EventMapScreen(eventRepository.getAll(), navController::popBackStack, currentRoute, { navController.navigate(Routes.DASHBOARD_CLIENT) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) })
        }
        composable(Routes.QR_SCANNER) {
            @ExperimentalGetImage
            QrScannerScreen(
                productRepository,
                navController::popBackStack,
                { navController.navigate("${Routes.PRODUCT_DETAIL}/$it") { launchSingleTop = true } },
                { productId -> qrScanRepository.record(currentUserId.takeIf { it != SessionManager.NO_USER_ID }, productId, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(Date())) },
                currentRoute,
                { navController.navigate(Routes.DASHBOARD_CLIENT) },
                { navController.navigate(Routes.CATALOG) },
                { navController.navigate(Routes.MAP) },
                { navController.navigate(Routes.REQUESTS) },
                { navController.navigate(Routes.PROFILE) }
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(currentUserId, userRepository, preferencesRepository, sessionManager, navController::popBackStack, ::logout, { navController.navigate(dashboardFor(currentRole)) }, { navController.navigate(Routes.CATALOG) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.NOTIFICATION_INBOX) }, currentRole == User.ROLE_ADMIN, { navController.navigate(Routes.ADMIN_PRODUCTS) }, { navController.navigate(Routes.CAMPAIGNS) })
        }
        composable(Routes.ADMIN_PRODUCTS) { AdminProductsScreen(productRepository, navController::popBackStack, currentRoute, { navController.navigate(Routes.DASHBOARD_ADMIN) }, { navController.navigate(Routes.CAMPAIGNS) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) }) }
        composable(Routes.CAMPAIGNS) { CampaignsScreen(campaignRepository, isAdmin = currentRole == User.ROLE_ADMIN, onBack = navController::popBackStack, currentRoute, { navController.navigate(Routes.DASHBOARD_ADMIN) }, { navController.navigate(Routes.ADMIN_PRODUCTS) }, { navController.navigate(Routes.REQUESTS) }, { navController.navigate(Routes.PROFILE) }) }
    }
}
