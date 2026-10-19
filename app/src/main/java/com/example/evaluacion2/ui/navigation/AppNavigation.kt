package com.example.evaluacion2.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.evaluacion2.ui.screens.common.PlaceholderScreen
import com.example.evaluacion2.ui.screens.dashboard.DashboardScreen
import com.example.evaluacion2.ui.screens.login.LoginScreen
import com.example.evaluacion2.ui.screens.signin.SignInScreen
import com.example.evaluacion2.ui.screens.signup.SignUpScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evaluacion2.data.local.AppDatabaseHelper
import com.example.evaluacion2.data.repository.UserRepository
import com.example.evaluacion2.viewmodel.SignUpViewModel
import com.example.evaluacion2.viewmodel.SignUpViewModelFactory
import com.example.evaluacion2.viewmodel.SignInViewModel
import com.example.evaluacion2.viewmodel.SignInViewModelFactory


@Composable
fun AppNavigation() {

    val navController =
        rememberNavController()

    val context =
        LocalContext.current.applicationContext

    val databaseHelper =
        remember {
            AppDatabaseHelper(context)
        }

    val userRepository =
        remember {
            UserRepository(
                databaseHelper
            )
        }


    NavHost(
        navController =
            navController,

        startDestination =
            Routes.WELCOME
    ) {


        // =================================================
        // BIENVENIDA
        // =================================================

        composable(
            Routes.WELCOME
        ) {

            LoginScreen(

                onLoginClick = {

                    navController.navigate(
                        Routes.SIGN_IN
                    )
                },

                onCreateAccountClick = {

                    navController.navigate(
                        Routes.SIGN_UP
                    )
                }
            )
        }


        // =================================================
        // INICIO DE SESIÓN
        // =================================================

        composable(
            Routes.SIGN_IN
        ) {

            val signInViewModel:
                    SignInViewModel =
                viewModel(
                    factory =
                        SignInViewModelFactory(
                            userRepository
                        )
                )


            SignInScreen(

                viewModel =
                    signInViewModel,

                onLoginSuccess = { role ->

                    if (
                        role == "ADMIN"
                    ) {

                        // Por ahora, hasta construir
                        // el Dashboard Administrador.
                        navController.navigate(
                            Routes.DASHBOARD
                        ) {

                            popUpTo(
                                Routes.WELCOME
                            ) {
                                inclusive = true
                            }
                        }

                    } else {

                        navController.navigate(
                            Routes.DASHBOARD
                        ) {

                            popUpTo(
                                Routes.WELCOME
                            ) {
                                inclusive = true
                            }
                        }
                    }
                },

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // REGISTRO
        // =================================================

        composable(
            Routes.SIGN_UP
        ) {

            val signUpViewModel:
                    SignUpViewModel =
                viewModel(
                    factory =
                        SignUpViewModelFactory(
                            userRepository
                        )
                )

            SignUpScreen(

                viewModel =
                    signUpViewModel,

                onRegisterSuccess = {

                    navController.navigate(
                        Routes.DASHBOARD
                    ) {

                        popUpTo(
                            Routes.WELCOME
                        ) {
                            inclusive = true
                        }
                    }
                },

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // DASHBOARD
        // =================================================

        composable(
            Routes.DASHBOARD
        ) {

            DashboardScreen(

                onCatalogClick = {

                    navController.navigate(
                        Routes.CATALOG
                    )
                },

                onMapClick = {

                    navController.navigate(
                        Routes.MAP
                    )
                },

                onQrClick = {

                    navController.navigate(
                        Routes.QR_SCANNER
                    )
                },

                onRequestsClick = {

                    navController.navigate(
                        Routes.REQUESTS
                    )
                },

                onProfileClick = {

                    navController.navigate(
                        Routes.PROFILE
                    )
                },

                onNotificationsClick = {

                    navController.navigate(
                        Routes.NOTIFICATIONS
                    )
                }
            )
        }


        // =================================================
        // CATÁLOGO
        // =================================================

        composable(
            Routes.CATALOG
        ) {

            PlaceholderScreen(
                title =
                    "Catálogo de Diseños",

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // MAPA
        // =================================================

        composable(
            Routes.MAP
        ) {

            PlaceholderScreen(
                title =
                    "Ferias y Showrooms",

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // QR
        // =================================================

        composable(
            Routes.QR_SCANNER
        ) {

            PlaceholderScreen(
                title =
                    "Escáner QR",

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // SOLICITUDES
        // =================================================

        composable(
            Routes.REQUESTS
        ) {

            PlaceholderScreen(
                title =
                    "Mis Solicitudes",

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // PERFIL
        // =================================================

        composable(
            Routes.PROFILE
        ) {

            PlaceholderScreen(
                title =
                    "Perfil y Configuración",

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =================================================
        // NOTIFICACIONES
        // =================================================

        composable(
            Routes.NOTIFICATIONS
        ) {

            PlaceholderScreen(
                title =
                    "Notificaciones",

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }
    }
}