package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DriverStatus
import com.example.domain.model.Trip
import com.example.domain.model.TripStatus
import com.example.ui.theme.*

@Composable
fun DriverStatusToggleBar(
    currentStatus: DriverStatus,
    onStatusChanged: (DriverStatus) -> Unit,
    completedTripsCount: Int,
    totalEarnings: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(20.dp),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CONSOLA DE CONDUCTOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                    Text(text = "Trimoto #04 (TM-8821)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EcoGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = EcoGreenDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "S/. ${String.format("%.2f", totalEarnings)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Way Status Switcher: DISPONIBLE, OCUPADO, DESCONECTADO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DriverStatus.values().forEach { status ->
                    val isSelected = currentStatus == status
                    Surface(
                        onClick = { onStatusChanged(status) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) {
                            when (status) {
                                DriverStatus.DISPONIBLE -> EcoGreenDark
                                DriverStatus.OCUPADO -> AmberAccent
                                DriverStatus.DESCONECTADO -> SlateDark
                            }
                        } else Color.Transparent,
                        contentColor = if (isSelected) Color.White else SlateMedium,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("driver_status_${status.name.lowercase()}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White else when (status) {
                                                DriverStatus.DISPONIBLE -> EcoGreenDark
                                                DriverStatus.OCUPADO -> AmberAccent
                                                DriverStatus.DESCONECTADO -> SlateMedium
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (status) {
                                        DriverStatus.DISPONIBLE -> "Disponible"
                                        DriverStatus.OCUPADO -> "Ocupado"
                                        DriverStatus.DESCONECTADO -> "Offline"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverIncomingRequestModal(
    trip: Trip,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(24.dp),
        color = CardSurface,
        border = androidx.compose.foundation.BorderStroke(2.dp, EcoGreenPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = EcoGreenContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Solicitud",
                                tint = EcoGreenDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "¡NUEVO VIAJE SOLICITADO!", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = EcoGreenDark)
                        Text(text = "Vecino: ${trip.clienteNombre}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                }

                Text(
                    text = "S/. ${String.format("%.2f", trip.precioEstimado)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EcoGreenDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Origen & Destino Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RadioButtonChecked, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Recoger en: ", fontSize = 12.sp, color = SlateLight)
                    Text(text = trip.origenTexto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = SlateDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Llevar a: ", fontSize = 12.sp, color = SlateLight)
                    Text(text = trip.destinoTexto, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Driver Buttons (Accept / Reject)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("driver_reject_trip_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "RECHAZAR", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp)
                        .testTag("driver_accept_trip_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenDark)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ACEPTAR VIAJE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun DriverActiveTripConsole(
    trip: Trip,
    onUpdateProgress: (TripStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 14.dp,
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Status & Client Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (trip.estado) {
                            TripStatus.ACEPTADO, TripStatus.CONDUCTOR_EN_CAMINO -> "EN CAMINO AL CLIENTE"
                            TripStatus.CONDUCTOR_LLEGO -> "ESPERANDO AL VECINO"
                            TripStatus.EN_CURSO -> "TRANSPORTANDO VECINO"
                            else -> trip.estado.name
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EcoGreenDark
                    )
                    Text(text = "Cliente: ${trip.clienteNombre}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                    Text(text = "Residencia: ${trip.clienteResidencia}", fontSize = 12.sp, color = SlateLight)
                }

                Text(
                    text = "S/. ${String.format("%.2f", trip.precioEstimado)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Target Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Ruta",
                        tint = EcoGreenDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (trip.estado == TripStatus.EN_CURSO) "Destino Final:" else "Punto de Recogida:",
                            fontSize = 11.sp,
                            color = SlateLight
                        )
                        Text(
                            text = if (trip.estado == TripStatus.EN_CURSO) trip.destinoTexto else trip.origenTexto,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large Touch Target Driver Control Action Button
            when (trip.estado) {
                TripStatus.ACEPTADO, TripStatus.CONDUCTOR_EN_CAMINO -> {
                    Button(
                        onClick = { onUpdateProgress(TripStatus.CONDUCTOR_LLEGO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("driver_arrived_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(imageVector = Icons.Default.Hail, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "NOTIFICAR: LLEGUÉ AL ORIGEN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                TripStatus.CONDUCTOR_LLEGO -> {
                    Button(
                        onClick = { onUpdateProgress(TripStatus.EN_CURSO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("driver_start_trip_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenDark)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "INICIAR VIAJE AHORA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                TripStatus.EN_CURSO -> {
                    Button(
                        onClick = { onUpdateProgress(TripStatus.FINALIZADO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("driver_finish_trip_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenDark)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "FINALIZAR VIAJE Y COBRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {}
            }
        }
    }
}
