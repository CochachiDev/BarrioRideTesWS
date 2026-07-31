package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.domain.model.Trip
import com.example.domain.model.TripStatus
import com.example.domain.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.TripHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    currentUser: User,
    viewModel: TripHistoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(currentUser.id) {
        viewModel.loadHistoryForUser(currentUser.id, isDriver = (currentUser.rol == com.example.domain.model.UserRole.CONDUCTOR))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Viajes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = EcoGreenDark)
            } else if (uiState.trips.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricRickshaw,
                        contentDescription = null,
                        tint = SlateLight,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Aún no tienes viajes registrados", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    Text(text = "Tus viajes realizados en la urbanización aparecerán aquí.", fontSize = 12.sp, color = SlateLight)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.trips) { trip ->
                        TripHistoryItemCard(trip = trip)
                    }
                }
            }
        }
    }
}

@Composable
fun TripHistoryItemCard(trip: Trip) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trip.fechaSolicitud))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().testTag("history_item_${trip.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = dateStr, fontSize = 11.sp, color = SlateLight)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (trip.estado) {
                        TripStatus.FINALIZADO -> EcoGreenContainer
                        TripStatus.CANCELADO -> Color(0xFFFEE2E2)
                        else -> AmberContainer
                    }
                ) {
                    Text(
                        text = when (trip.estado) {
                            TripStatus.FINALIZADO -> "FINALIZADO"
                            TripStatus.CANCELADO -> "CANCELADO"
                            else -> "EN PROCESO"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (trip.estado) {
                            TripStatus.FINALIZADO -> EcoGreenDark
                            TripStatus.CANCELADO -> Color(0xFFDC2626)
                            else -> AmberAccent
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${trip.origenTexto} ➔ ${trip.destinoTexto}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateDark)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SlateLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trip.conductorNombre ?: "Conductor Asignado",
                        fontSize = 12.sp,
                        color = SlateLight
                    )
                }

                Text(
                    text = "S/. ${String.format("%.2f", trip.precioFinal ?: trip.precioEstimado)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )
            }
        }
    }
}
