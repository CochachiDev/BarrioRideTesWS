package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.User
import com.example.ui.components.DriverActiveTripConsole
import com.example.ui.components.DriverIncomingRequestModal
import com.example.ui.components.DriverStatusToggleBar
import com.example.ui.components.InteractiveCommunityMap
import com.example.ui.theme.*
import com.example.ui.viewmodel.DriverConsoleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverConsoleScreen(
    currentUser: User,
    viewModel: DriverConsoleViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.id) {
        viewModel.initializeDriver(currentUser.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BarrioRide Conductor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SlateDark
                        )
                        Text(
                            text = "Conductor: ${currentUser.nombre} (${currentUser.telefono})",
                            fontSize = 11.sp,
                            color = SlateLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("driver_nav_history_button")
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Historial", tint = EcoGreenDark)
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("driver_nav_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil", tint = SlateDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardSurface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Map Canvas
            InteractiveCommunityMap(
                selectedOrigin = null,
                selectedDestination = null,
                activeTrip = uiState.activeDriverTrip ?: uiState.incomingTripRequest,
                driverLocation = uiState.currentDriver?.let { Pair(it.latitudActual, it.longitudActual) },
                availableDriverCount = if (uiState.driverStatus == com.example.domain.model.DriverStatus.DISPONIBLE) 1 else 0,
                onPointSelected = {},
                onRealLocationUpdated = { lat, lng, _ ->
                    viewModel.updateDriverLocation(lat, lng)
                }
            )

            // Top Status & Earnings Bar
            DriverStatusToggleBar(
                currentStatus = uiState.driverStatus,
                onStatusChanged = { viewModel.setStatus(it) },
                completedTripsCount = uiState.completedTripsCount,
                totalEarnings = uiState.totalEarnings,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Bottom Overlays (Incoming Alert OR Active Console)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (uiState.incomingTripRequest != null && uiState.activeDriverTrip == null) {
                    DriverIncomingRequestModal(
                        trip = uiState.incomingTripRequest!!,
                        onAccept = { viewModel.acceptIncomingTrip() },
                        onReject = { viewModel.rejectIncomingTrip() }
                    )
                } else if (uiState.activeDriverTrip != null) {
                    DriverActiveTripConsole(
                        trip = uiState.activeDriverTrip!!,
                        onUpdateProgress = { nextState -> viewModel.updateTripProgress(nextState) }
                    )
                }
            }
        }
    }
}
