package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.User
import com.example.ui.components.ActiveTripStatusCard
import com.example.ui.components.BookingCard
import com.example.ui.components.InteractiveCommunityMap
import com.example.ui.components.TripReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.ClientTripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMapScreen(
    currentUser: User,
    viewModel: ClientTripViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.id) {
        viewModel.observeActiveTripForUser(currentUser.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BarrioRide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SlateDark
                        )
                        Text(
                            text = "Hola, ${currentUser.nombre} (${currentUser.residencia})",
                            fontSize = 11.sp,
                            color = SlateLight
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("nav_history_button")
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "Historial", tint = EcoGreenDark)
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("nav_profile_button")
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
                selectedOrigin = uiState.selectedOrigin,
                selectedDestination = uiState.selectedDestination,
                activeTrip = uiState.activeTrip,
                driverLocation = uiState.liveDriverLocation,
                availableDriverCount = uiState.availableDrivers.size,
                onPointSelected = { point ->
                    viewModel.selectDestination(point)
                },
                onRealLocationUpdated = { lat, lng, address ->
                    viewModel.updateRealLocation(lat, lng, address)
                }
            )

            // Booking or Active Overlay Card at Bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (uiState.activeTrip == null) {
                    BookingCard(
                        availablePoints = uiState.availablePoints,
                        selectedOrigin = uiState.selectedOrigin,
                        selectedDestination = uiState.selectedDestination,
                        distanceKm = uiState.estimatedDistanceKm,
                        fare = uiState.estimatedFare,
                        estimatedMinutes = uiState.estimatedMinutes,
                        isRequesting = uiState.isRequesting,
                        onOriginSelected = { viewModel.selectOrigin(it) },
                        onDestinationSelected = { viewModel.selectDestination(it) },
                        onSwapLocations = { viewModel.swapOriginAndDestination() },
                        onRequestRide = { viewModel.requestTrip(currentUser) }
                    )
                } else {
                    ActiveTripStatusCard(
                        trip = uiState.activeTrip!!,
                        onCancelTrip = { viewModel.cancelActiveTrip() }
                    )
                }
            }

            // Receipt Modal upon completion
            if (uiState.showReceiptModal && uiState.activeTrip != null) {
                TripReceiptDialog(
                    trip = uiState.activeTrip!!,
                    onDismiss = { viewModel.dismissReceiptModal() }
                )
            }
        }
    }
}
