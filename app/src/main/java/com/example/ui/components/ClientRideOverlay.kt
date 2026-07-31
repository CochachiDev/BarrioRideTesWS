package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.domain.model.*
import com.example.ui.theme.*

@Composable
fun BookingCard(
    availablePoints: List<CommunityPoint>,
    selectedOrigin: CommunityPoint,
    selectedDestination: CommunityPoint,
    distanceKm: Double,
    fare: Double,
    estimatedMinutes: Int,
    isRequesting: Boolean,
    onOriginSelected: (CommunityPoint) -> Unit,
    onDestinationSelected: (CommunityPoint) -> Unit,
    onSwapLocations: () -> Unit,
    onRequestRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(24.dp),
        color = CardSurface
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = EcoGreenContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ElectricRickshaw,
                            contentDescription = "Trimoto",
                            tint = EcoGreenDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Solicitar Trimoto Eléctrica",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Text(
                        text = "Movilidad interna exclusiva de la comunidad",
                        fontSize = 12.sp,
                        color = SlateLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Origen & Destino Selector Boxes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Pickup Point
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = "Origen",
                        tint = EcoGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "PUNTO DE RECOGIDA (ORIGEN)", fontSize = 10.sp, color = SlateLight, fontWeight = FontWeight.Bold)
                        Text(text = selectedOrigin.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = RoadGray
                )

                // Dropoff Point
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Destino",
                        tint = SlateDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DESTINO EN LA URBANIZACIÓN", fontSize = 10.sp, color = SlateLight, fontWeight = FontWeight.Bold)
                        Text(text = selectedDestination.nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                    }
                    IconButton(
                        onClick = onSwapLocations,
                        modifier = Modifier.testTag("swap_locations_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Intercambiar",
                            tint = EcoGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset Quick Selection Pills
            Text(
                text = "Puntos de interés frecuentes:",
                fontSize = 12.sp,
                color = SlateLight,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availablePoints) { point ->
                    val isSelected = selectedDestination.id == point.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDestinationSelected(point) },
                        label = { Text(point.nombre, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (point.tipo) {
                                    "ENTRADA" -> Icons.Default.DoorFront
                                    "SERVICIOS" -> Icons.Default.Pool
                                    "PARQUE" -> Icons.Default.Park
                                    else -> Icons.Default.Home
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EcoGreenDark,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        ),
                        modifier = Modifier.testTag("preset_chip_${point.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trip Estimate Summary (Distance, Time, Flat Rate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EcoGreenContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "ETA",
                            tint = EcoGreenOnContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "~$estimatedMinutes min",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenOnContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Distance",
                            tint = EcoGreenOnContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${distanceKm} km",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenOnContainer
                        )
                    }
                    Text(text = "Tarifa plana de mantenimiento", fontSize = 11.sp, color = EcoGreenOnContainer.copy(alpha = 0.8f))
                }

                Text(
                    text = "S/. ${String.format("%.2f", fare)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EcoGreenDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Request Trimoto Action Button
            Button(
                onClick = onRequestRide,
                enabled = !isRequesting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("request_ride_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGreenDark,
                    contentColor = Color.White
                )
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Buscando trimoto disponible...", fontSize = 16.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.ElectricRickshaw,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SOLICITAR TRIMOTO AHORA",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTripStatusCard(
    trip: Trip,
    onCancelTrip: () -> Unit,
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
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // State Badge & Animated Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (trip.estado) {
                        TripStatus.SOLICITADO -> AmberContainer
                        TripStatus.ACEPTADO, TripStatus.CONDUCTOR_EN_CAMINO -> EcoGreenContainer
                        TripStatus.CONDUCTOR_LLEGO -> Color(0xFFE0F2FE)
                        TripStatus.EN_CURSO -> EcoGreenContainer
                        else -> RoadGray
                    }
                ) {
                    Text(
                        text = when (trip.estado) {
                            TripStatus.SOLICITADO -> "BUSCANDO CONDUCTOR..."
                            TripStatus.ACEPTADO -> "SOLICITUD ACEPTADA"
                            TripStatus.CONDUCTOR_EN_CAMINO -> "CONDUCTOR EN CAMINO"
                            TripStatus.CONDUCTOR_LLEGO -> "¡TU TRIMOTO HA LLEGADO!"
                            TripStatus.EN_CURSO -> "VIAJE EN CURSO"
                            else -> trip.estado.name
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (trip.estado) {
                            TripStatus.SOLICITADO -> AmberAccent
                            TripStatus.ACEPTADO, TripStatus.CONDUCTOR_EN_CAMINO -> EcoGreenDark
                            TripStatus.CONDUCTOR_LLEGO -> Color(0xFF0284C7)
                            TripStatus.EN_CURSO -> EcoGreenDark
                            else -> SlateDark
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "S/. ${String.format("%.2f", trip.precioEstimado)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EcoGreenDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Driver & Vehicle Details (if accepted)
            if (trip.conductorNombre != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceLight, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EcoGreenDark,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = trip.conductorNombre,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trip.conductorNombre ?: "Conductor Asignado",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = trip.vehiculoInfo ?: "Trimoto Eléctrica Verde #04",
                            fontSize = 12.sp,
                            color = SlateLight
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = AmberAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "4.9 (Conductor Oficial Urbanización)", fontSize = 11.sp, color = SlateLight)
                        }
                    }
                    IconButton(
                        onClick = { /* Call driver */ },
                        modifier = Modifier.background(EcoGreenContainer, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Llamar",
                            tint = EcoGreenDark
                        )
                    }
                }
            } else {
                // Solicitado pulse banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberContainer, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = AmberAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Notificando a conductores de la urbanización...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SlateDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trip Route Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = EcoGreenDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${trip.origenTexto} ➔ ${trip.destinoTexto}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel Button if not yet started
            if (trip.estado == TripStatus.SOLICITADO || trip.estado == TripStatus.ACEPTADO || trip.estado == TripStatus.CONDUCTOR_EN_CAMINO) {
                OutlinedButton(
                    onClick = onCancelTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cancel_trip_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "CANCELAR SOLICITUD DE VIAJE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TripReceiptDialog(
    trip: Trip,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EcoGreenDark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "¡Viaje Finalizado!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Gracias por movilizarte con el servicio de trimotos eléctricas de la urbanización.", fontSize = 13.sp, color = SlateLight)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Recorrido:", fontSize = 12.sp, color = SlateLight)
                            Text(text = "${trip.origenTexto} ➔ ${trip.destinoTexto}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Distancia:", fontSize = 12.sp, color = SlateLight)
                            Text(text = "${trip.distancia} km", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Conductor:", fontSize = 12.sp, color = SlateLight)
                            Text(text = trip.conductorNombre ?: "Conductor Community", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = RoadGray)
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Total a pagar:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "S/. ${String.format("%.2f", trip.precioFinal ?: trip.precioEstimado)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EcoGreenDark
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenDark),
                modifier = Modifier.testTag("dismiss_receipt_button")
            ) {
                Text(text = "ACEPTAR Y CALIFICAR", fontWeight = FontWeight.Bold)
            }
        }
    )
}
