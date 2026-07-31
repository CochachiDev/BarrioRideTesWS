package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.UserRole
import com.example.ui.screens.*
import com.example.ui.theme.BarrioRideTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ClientTripViewModel
import com.example.ui.viewmodel.DriverConsoleViewModel
import com.example.ui.viewmodel.TripHistoryViewModel

import com.example.provider.AppStateProvider
import com.example.provider.rememberAppState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarrioRideTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BarrioRideAppNavigation()
                }
            }
        }
    }
}

@Composable
fun BarrioRideAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val clientTripViewModel: ClientTripViewModel = viewModel()
    val driverConsoleViewModel: DriverConsoleViewModel = viewModel()
    val tripHistoryViewModel: TripHistoryViewModel = viewModel()

    val authState by authViewModel.uiState.collectAsState()
    val appState = rememberAppState(
        initialUser = authState.currentUser,
        initialRole = authState.activeRole
    )

    // Keep global AppState in sync with AuthViewModel
    appState.setUser(authState.currentUser)
    appState.setRole(authState.activeRole)

    AppStateProvider(appState = appState) {
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
        composable("splash") {
            SplashScreen(
                onSelectRole = { role ->
                    authViewModel.switchRole(role)
                    if (role == UserRole.CLIENTE) {
                        navController.navigate("client_map") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("driver_console") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("auth") {
            LoginRegisterScreen(
                initialRole = authState.activeRole,
                onAuthSuccess = {
                    if (authState.activeRole == UserRole.CLIENTE) {
                        navController.navigate("client_map") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        navController.navigate("driver_console") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                },
                onAuthenticate = { nombre, apellido, telefono, email, residencia, role ->
                    authViewModel.registerOrLoginCustomUser(
                        nombre = nombre,
                        apellido = apellido,
                        telefono = telefono,
                        email = email,
                        residencia = residencia,
                        rol = role
                    )
                }
            )
        }

        composable("client_map") {
            val user = authState.currentUser
            if (user != null) {
                ClientMapScreen(
                    currentUser = user,
                    viewModel = clientTripViewModel,
                    onNavigateToHistory = { navController.navigate("trip_history") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            } else {
                authViewModel.loginDemoResident()
            }
        }

        composable("driver_console") {
            val user = authState.currentUser
            if (user != null) {
                DriverConsoleScreen(
                    currentUser = user,
                    viewModel = driverConsoleViewModel,
                    onNavigateToHistory = { navController.navigate("trip_history") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            } else {
                authViewModel.loginDemoDriver()
            }
        }

        composable("trip_history") {
            val user = authState.currentUser
            if (user != null) {
                TripHistoryScreen(
                    currentUser = user,
                    viewModel = tripHistoryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("profile") {
            val user = authState.currentUser
            if (user != null) {
                ProfileScreen(
                    currentUser = user,
                    onSwitchRole = { role ->
                        authViewModel.switchRole(role)
                        if (role == UserRole.CLIENTE) {
                            navController.navigate("client_map") {
                                popUpTo("client_map") { inclusive = true }
                            }
                        } else {
                            navController.navigate("driver_console") {
                                popUpTo("driver_console") { inclusive = true }
                            }
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("splash") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
}

