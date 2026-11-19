package com.example.evaluacion2.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.evaluacion2.LouArayaApp
import com.example.evaluacion2.data.local.SessionManager
import com.example.evaluacion2.domain.model.UserRole
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
import com.example.evaluacion2.viewmodel.AdminProductsViewModel
import com.example.evaluacion2.viewmodel.CampaignsViewModel
import com.example.evaluacion2.viewmodel.CatalogViewModel
import com.example.evaluacion2.viewmodel.DashboardViewModel
import com.example.evaluacion2.viewmodel.DetailViewModel
import com.example.evaluacion2.viewmodel.FavoritesViewModel
import com.example.evaluacion2.viewmodel.ProfileViewModel
import com.example.evaluacion2.viewmodel.QuoteViewModel
import com.example.evaluacion2.viewmodel.RequestsViewModel
import com.example.evaluacion2.viewmodel.SignInViewModel
import com.example.evaluacion2.viewmodel.SignUpViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TRANSITION_MS = 300

@Composable
fun AppNavigation() {
    val container = (LocalContext.current.applicationContext as LouArayaApp).container
    val session = container.sessionManager
    val navController = rememberNavController()

    var sessionRevision by remember { mutableIntStateOf(0) }
    val initialRoute = remember {
        if (!session.isLoggedIn()) Routes.WELCOME else homeRouteFor(UserRole.from(session.getUserRole()))
    }
    val currentName = remember(sessionRevision) { session.getUserName().orEmpty() }
    val currentRole = remember(sessionRevision) { UserRole.from(session.getUserRole()) }
    val currentUserId = remember(sessionRevision) { session.getUserId() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val authenticated = session.isLoggedIn()
    val isAdmin = currentRole.canManageCatalog

    fun navigateToHome(role: UserRole) {
        sessionRevision++
        navController.navigate(homeRouteFor(role)) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun logout() {
        session.clearSession()
        sessionRevision++
        navController.navigate(Routes.WELCOME) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun go(route: String) = navController.navigate(route) { launchSingleTop = true }
    val home = { go(homeRouteFor(currentRole)) }

    /** Navegación de pestañas: vuelve al inicio del rol y abre la pestaña elegida sin apilar pantallas. */
    fun goTab(route: String) {
        val homeRoute = homeRouteFor(currentRole)
        navController.navigate(route) {
            popUpTo(homeRoute) { inclusive = false }
            launchSingleTop = true
        }
    }
    val navigator = AppNavigator(navigate = ::goTab, home = home)

    BackHandler(enabled = authenticated && currentRoute == homeRouteFor(currentRole)) { logout() }

    // Si una pantalla privada se abre sin sesión, se vuelve al inicio.
    LaunchedEffect(currentRoute, authenticated, currentUserId) {
        val isPublic = currentRoute == null || currentRoute in setOf(Routes.WELCOME, Routes.SIGN_IN, Routes.SIGN_UP)
        if (!isPublic && (!authenticated || currentUserId == SessionManager.NO_USER_ID)) logout()
    }

    CompositionLocalProvider(LocalAppNavigator provides navigator) {
    NavHost(
        navController = navController,
        startDestination = initialRoute,
        enterTransition = { fadeIn(tween(TRANSITION_MS)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_MS)) },
        exitTransition = { fadeOut(tween(TRANSITION_MS)) },
        popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
        popExitTransition = { fadeOut(tween(TRANSITION_MS)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_MS)) }
    ) {
        composable(Routes.WELCOME) {
            LoginScreen(onLoginClick = { go(Routes.SIGN_IN) }, onCreateAccountClick = { go(Routes.SIGN_UP) })
        }
        composable(Routes.SIGN_IN) {
            val vm: SignInViewModel = viewModel { SignInViewModel(container.userRepository, session) }
            SignInScreen(vm, onLoginSuccess = { role -> navigateToHome(UserRole.from(role)) }, onBackClick = { navController.popBackStack() })
        }
        composable(Routes.SIGN_UP) {
            val vm: SignUpViewModel = viewModel { SignUpViewModel(container.userRepository, session) }
            SignUpScreen(vm, onRegisterSuccess = { navigateToHome(UserRole.from(session.getUserRole())) }, onBackClick = { navController.popBackStack() })
        }
        composable(Routes.DASHBOARD_CLIENT) {
            val vm: DashboardViewModel = viewModel(key = "dashboard-$currentUserId") {
                DashboardViewModel(currentUserId, container.favoriteRepository, container.productRepository)
            }
            val featured by vm.featured.collectAsState()
            LaunchedEffect(Unit) { vm.refresh() }
            DashboardScreen(
                currentName, featured, { go("${Routes.PRODUCT_DETAIL}/$it") }, { go(Routes.FAVORITES) }, { go(Routes.CATALOG) },
                { go(Routes.MAP) }, { go(Routes.QR_SCANNER) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) },
                { go(Routes.NOTIFICATION_INBOX) }, ::logout
            )
        }
        composable(Routes.DASHBOARD_ADMIN) {
            AdminDashboardScreen(currentName, { go(Routes.ADMIN_PRODUCTS) }, { go(Routes.CAMPAIGNS) }, { go(Routes.REQUESTS) }, ::logout, { go(Routes.PROFILE) }, { go(Routes.NOTIFICATION_INBOX) })
        }
        composable(Routes.NOTIFICATION_INBOX) {
            val notifications = remember(currentUserId) { container.notificationRepository.getForUser(currentUserId) }
            NotificationsScreen(
                notifications = notifications,
                isAdmin = isAdmin,
                onBack = navController::popBackStack,
                onOpenRequests = { go(Routes.REQUESTS) },
                onOpenEvents = { go(Routes.MAP) },
                onRead = { container.notificationRepository.markRead(it, currentUserId) },
                onOpenCampaigns = { go(Routes.CAMPAIGNS) },
                onHome = home,
                onCatalog = { go(Routes.CATALOG) },
                onMap = { go(Routes.MAP) },
                onProfile = { go(Routes.PROFILE) },
                onProducts = { go(Routes.ADMIN_PRODUCTS) },
                currentRoute = currentRoute
            )
        }
        composable(Routes.CATALOG) {
            val vm: CatalogViewModel = viewModel { CatalogViewModel(container.productRepository) }
            CatalogScreen(vm, { navController.popBackStack() }, { go("${Routes.PRODUCT_DETAIL}/$it") }, isAdmin, currentRoute, home, { go(Routes.MAP) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) }, { go(Routes.ADMIN_PRODUCTS) })
        }
        composable("${Routes.PRODUCT_DETAIL}/{productId}", arguments = listOf(navArgument("productId") { type = NavType.IntType })) { entry ->
            val id = entry.arguments?.getInt("productId") ?: 0
            val vm: DetailViewModel = viewModel(key = "detail-$currentUserId-$id") {
                DetailViewModel(currentUserId, id, container.favoriteRepository, container.productRepository)
            }
            ProductDetailScreen(vm, onQuote = { go("${Routes.QUOTE}?productId=$id") }, onBack = { navController.popBackStack() }, isAdmin = isAdmin)
        }
        composable(Routes.FAVORITES) {
            val vm: FavoritesViewModel = viewModel(key = "favorites-$currentUserId") {
                FavoritesViewModel(currentUserId, container.favoriteRepository, container.productRepository)
            }
            FavoritesScreen(vm, { navController.popBackStack() }, { go("${Routes.PRODUCT_DETAIL}/$it") }, currentRoute, home, { go(Routes.CATALOG) }, { go(Routes.MAP) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) })
        }
        composable("${Routes.QUOTE}?productId={productId}", arguments = listOf(navArgument("productId") { type = NavType.IntType; defaultValue = 0 })) { entry ->
            val productId = entry.arguments?.getInt("productId")?.takeIf { it > 0 }
            val vm: QuoteViewModel = viewModel(key = "quote-${productId ?: 0}-${entry.id}") {
                QuoteViewModel(currentUserId, currentName, productId, container.requestRepository, container.productRepository)
            }
            QuoteFormScreen(vm, { navController.popBackStack() }, {
                navController.navigate(Routes.REQUESTS) { popUpTo(Routes.REQUESTS) { inclusive = true }; launchSingleTop = true }
            }, currentRoute, home, { go(Routes.CATALOG) }, { go(Routes.MAP) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) })
        }
        composable(Routes.REQUESTS) {
            val vm: RequestsViewModel = viewModel(key = "requests-$currentUserId-${currentRole.code}") {
                RequestsViewModel(currentUserId, currentRole, container.requestRepository)
            }
            RequestsScreen(vm, isAdmin, { navController.popBackStack() }, { go(Routes.QUOTE) }, currentRoute, home, { go(Routes.CATALOG) }, { go(Routes.MAP) }, { go(Routes.PROFILE) }, { go(Routes.NOTIFICATION_INBOX) })
        }
        composable(Routes.MAP) {
            val events = remember { container.eventRepository.getAll() }
            EventMapScreen(events, navController::popBackStack, currentRoute, home, { go(Routes.CATALOG) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) })
        }
        composable(Routes.QR_SCANNER) {
            @ExperimentalGetImage
            QrScannerScreen(
                findProduct = { container.productRepository.findCached(it) },
                onBack = navController::popBackStack,
                onProduct = { go("${Routes.PRODUCT_DETAIL}/$it") },
                onRecord = { productId ->
                    container.qrScanRepository.record(
                        currentUserId.takeIf { it != SessionManager.NO_USER_ID }, productId,
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(Date())
                    )
                },
                currentRoute = currentRoute,
                onHome = home,
                onCatalog = { go(Routes.CATALOG) },
                onMap = { go(Routes.MAP) },
                onRequests = { go(Routes.REQUESTS) },
                onProfile = { go(Routes.PROFILE) }
            )
        }
        composable(Routes.PROFILE) {
            val vm: ProfileViewModel = viewModel(key = "profile-$currentUserId") {
                ProfileViewModel(currentUserId, container.userRepository, container.preferencesRepository, session)
            }
            ProfileScreen(
                vm, isAdmin, navController::popBackStack, ::logout, home, { go(Routes.CATALOG) }, { go(Routes.REQUESTS) },
                { go(Routes.NOTIFICATION_INBOX) }, { go(Routes.ADMIN_PRODUCTS) }, { go(Routes.CAMPAIGNS) }
            )
        }
        composable(Routes.ADMIN_PRODUCTS) {
            val vm: AdminProductsViewModel = viewModel { AdminProductsViewModel(container.productRepository) }
            AdminProductsScreen(vm, navController::popBackStack, currentRoute, home, { go(Routes.CAMPAIGNS) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) })
        }
        composable(Routes.CAMPAIGNS) {
            val vm: CampaignsViewModel = viewModel(key = "campaigns-${currentRole.code}") { CampaignsViewModel(currentRole, container.campaignRepository) }
            CampaignsScreen(vm, isAdmin = isAdmin, onBack = navController::popBackStack, currentRoute, home, { go(Routes.ADMIN_PRODUCTS) }, { go(Routes.REQUESTS) }, { go(Routes.PROFILE) }, { go(Routes.MAP) })
        }
    }
    }
}

/** Pantalla de inicio según el rol (polimorfismo de [UserRole]). */
fun homeRouteFor(role: UserRole): String = when (role) {
    UserRole.Admin -> Routes.DASHBOARD_ADMIN
    UserRole.Client -> Routes.DASHBOARD_CLIENT
}
