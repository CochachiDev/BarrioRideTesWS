package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CommunityLocations
import com.example.domain.model.CommunityPoint
import com.example.domain.model.Trip
import com.example.domain.model.TripStatus
import com.example.provider.LocationTracker
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun InteractiveCommunityMap(
    selectedOrigin: CommunityPoint?,
    selectedDestination: CommunityPoint?,
    activeTrip: Trip?,
    driverLocation: Pair<Double, Double>?,
    availableDriverCount: Int,
    onPointSelected: (CommunityPoint) -> Unit,
    modifier: Modifier = Modifier,
    onRealLocationUpdated: ((Double, Double, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationTracker = remember(context) { LocationTracker(context) }
    var hasLocationPermission by remember { mutableStateOf(locationTracker.hasLocationPermission()) }
    var realUserLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var realUserAddress by remember { mutableStateOf<String?>(null) }
    var hasCenteredInitialLocation by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                      permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        hasLocationPermission = granted
    }

    var useGoogleMaps by remember { mutableStateOf(true) }

    // Google Map Camera State centered initially at urbanization or real user location
    val centerLatLng = realUserLocation?.let { LatLng(it.first, it.second) }
        ?: LatLng(CommunityLocations.CENTER_LAT, CommunityLocations.CENTER_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerLatLng, 16.5f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationTracker.getLocationUpdates().collect { location ->
                val lat = location.latitude
                val lng = location.longitude
                realUserLocation = Pair(lat, lng)
                val addr = locationTracker.getAddressFromCoordinates(lat, lng)
                realUserAddress = addr
                onRealLocationUpdated?.invoke(lat, lng, addr)

                if (!hasCenteredInitialLocation) {
                    hasCenteredInitialLocation = true
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16.8f)
                    )
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Pulse animation for custom overlay
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Center camera when selected points change
    LaunchedEffect(selectedOrigin, selectedDestination, driverLocation) {
        if (useGoogleMaps) {
            val target = when {
                driverLocation != null -> LatLng(driverLocation.first, driverLocation.second)
                selectedOrigin != null -> LatLng(selectedOrigin.lat, selectedOrigin.lng)
                selectedDestination != null -> LatLng(selectedDestination.lat, selectedDestination.lng)
                realUserLocation != null -> LatLng(realUserLocation!!.first, realUserLocation!!.second)
                else -> centerLatLng
            }
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 16.8f))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MapLandGreen)
            .testTag("interactive_community_map")
    ) {
        if (useGoogleMaps) {
            // Google Maps Render Engine
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_container"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = false
                )
            ) {
                // Preset Community Points Markers
                CommunityLocations.PRESET_POINTS.forEach { point ->
                    val isOrigin = selectedOrigin?.id == point.id
                    val isDest = selectedDestination?.id == point.id

                    Marker(
                        state = MarkerState(position = LatLng(point.lat, point.lng)),
                        title = point.nombre,
                        snippet = point.descripcion,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            when {
                                isOrigin -> BitmapDescriptorFactory.HUE_GREEN
                                isDest -> BitmapDescriptorFactory.HUE_VIOLET
                                point.tipo == "ENTRADA" -> BitmapDescriptorFactory.HUE_ORANGE
                                point.tipo == "SERVICIOS" -> BitmapDescriptorFactory.HUE_AZURE
                                else -> BitmapDescriptorFactory.HUE_RED
                            }
                        ),
                        onClick = {
                            onPointSelected(point)
                            true
                        }
                    )
                }

                // Origin & Destination Route Polyline
                val origLat = selectedOrigin?.lat ?: activeTrip?.origenLat
                val origLng = selectedOrigin?.lng ?: activeTrip?.origenLng
                val destLat = selectedDestination?.lat ?: activeTrip?.destinoLat
                val destLng = selectedDestination?.lng ?: activeTrip?.destinoLng

                if (origLat != null && origLng != null && destLat != null && destLng != null) {
                    Polyline(
                        points = listOf(
                            LatLng(origLat, origLng),
                            LatLng(destLat, destLng)
                        ),
                        color = PurplePrimary,
                        width = 12f
                    )
                }

                // Live Driver Marker
                val drvLat = driverLocation?.first ?: activeTrip?.let { if (it.conductorId != null) it.origenLat + 0.0008 else null }
                val drvLng = driverLocation?.second ?: activeTrip?.let { if (it.conductorId != null) it.origenLng + 0.0008 else null }

                if (drvLat != null && drvLng != null) {
                    Marker(
                        state = MarkerState(position = LatLng(drvLat, drvLng)),
                        title = "Trimoto Eléctrica en vivo",
                        snippet = "Conductor asignado",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    )
                }
            }
        } else {
            // Vector Custom Diagram Map (Fallback / Schematic Mode)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Draw Green Parks & Landscape
                drawRect(
                    color = Color(0xFFD1FAE5),
                    topLeft = Offset(w * 0.15f, h * 0.20f),
                    size = Size(w * 0.70f, h * 0.65f)
                )

                drawOval(
                    color = Color(0xFFA7F3D0),
                    topLeft = Offset(w * 0.35f, h * 0.38f),
                    size = Size(w * 0.30f, h * 0.22f)
                )

                // 2. Main Avenue Network & Internal Roads
                val roadPaintColor = RoadGray
                val roadWidth = 28f

                drawRoundRect(
                    color = roadPaintColor,
                    topLeft = Offset(w * 0.12f, h * 0.15f),
                    size = Size(w * 0.76f, h * 0.72f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(40f, 40f),
                    style = Stroke(width = roadWidth)
                )

                drawLine(
                    color = roadPaintColor,
                    start = Offset(w * 0.12f, h * 0.50f),
                    end = Offset(w * 0.88f, h * 0.50f),
                    strokeWidth = roadWidth + 6f
                )
                drawLine(
                    color = roadPaintColor,
                    start = Offset(w * 0.50f, h * 0.15f),
                    end = Offset(w * 0.50f, h * 0.87f),
                    strokeWidth = roadWidth + 6f
                )

                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)

                // 3. Draw Route Path if Origen and Destino are set
                if (selectedOrigin != null && selectedDestination != null) {
                    val origPos = mapGeoToCanvas(selectedOrigin.lat, selectedOrigin.lng, w, h)
                    val destPos = mapGeoToCanvas(selectedDestination.lat, selectedDestination.lng, w, h)

                    val routePath = Path().apply {
                        moveTo(origPos.x, origPos.y)
                        lineTo(origPos.x, destPos.y)
                        lineTo(destPos.x, destPos.y)
                    }

                    drawPath(
                        path = routePath,
                        color = PurplePrimary.copy(alpha = 0.3f),
                        style = Stroke(width = 24f)
                    )
                    drawPath(
                        path = routePath,
                        color = PurplePrimary,
                        style = Stroke(width = 8f, pathEffect = dashPathEffect)
                    )
                }

                if (activeTrip != null && activeTrip.estado == TripStatus.SOLICITADO) {
                    val origPos = mapGeoToCanvas(activeTrip.origenLat, activeTrip.origenLng, w, h)
                    drawCircle(
                        color = PurplePrimary.copy(alpha = pulseAlpha),
                        radius = pulseRadius * 2.5f,
                        center = origPos
                    )
                }
            }

            // Schematic Map Overlay Landmarks
            CommunityLocations.PRESET_POINTS.forEach { point ->
                val isOrigin = selectedOrigin?.id == point.id
                val isDest = selectedDestination?.id == point.id

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                        .offset(
                            x = (getPointOffsetFractionX(point.lat, point.lng) * 320).dp,
                            y = (getPointOffsetFractionY(point.lat, point.lng) * 580).dp
                        )
                ) {
                    Surface(
                        onClick = { onPointSelected(point) },
                        shape = RoundedCornerShape(16.dp),
                        color = when {
                            isOrigin -> PurplePrimary
                            isDest -> SlateDark
                            else -> CardSurface
                        },
                        contentColor = when {
                            isOrigin || isDest -> Color.White
                            else -> SlateDark
                        },
                        shadowElevation = if (isOrigin || isDest) 8.dp else 3.dp,
                        modifier = Modifier.testTag("landmark_node_${point.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (point.tipo) {
                                    "ENTRADA" -> Icons.Default.DoorFront
                                    "SERVICIOS" -> Icons.Default.Pool
                                    "PARQUE" -> Icons.Default.Park
                                    else -> Icons.Default.HomeWork
                                },
                                contentDescription = point.nombre,
                                modifier = Modifier.size(16.dp),
                                tint = when {
                                    isOrigin || isDest -> Color.White
                                    point.tipo == "ENTRADA" -> AmberAccent
                                    point.tipo == "SERVICIOS" -> PurplePrimary
                                    else -> SlateMedium
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = point.nombre.take(16) + if (point.nombre.length > 16) "..." else "",
                                fontSize = 11.sp,
                                fontWeight = if (isOrigin || isDest) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Top Status Header pill with Mode Toggle & Available Drivers Count
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 12.dp, end = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = SlateDark.copy(alpha = 0.92f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Google Maps",
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (realUserAddress != null) "GPS: $realUserAddress" else "Google Maps • Urbanización El Retiro",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasLocationPermission) "$availableDriverCount Trimotos activas • GPS Real Activo" else "$availableDriverCount Trimotos activas • Permiso GPS Requerido",
                            color = if (hasLocationPermission) AmberAccent else AmberAccent,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Toggle Button for Mode Switch
                IconButton(
                    onClick = { useGoogleMaps = !useGoogleMaps },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("toggle_map_mode_button")
                ) {
                    Icon(
                        imageVector = if (useGoogleMaps) Icons.Default.Layers else Icons.Default.LayersClear,
                        contentDescription = "Cambiar vista mapa",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Floating Action Button for Real GPS Location Centering
        FloatingActionButton(
            onClick = {
                if (!hasLocationPermission) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    realUserLocation?.let { (lat, lng) ->
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 17.5f)
                            )
                        }
                        realUserAddress?.let { addr ->
                            onRealLocationUpdated?.invoke(lat, lng, addr)
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 240.dp, end = 16.dp)
                .testTag("my_location_gps_fab"),
            containerColor = EcoGreenDark,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Centrar en mi ubicación GPS real"
            )
        }
    }
}

private fun mapGeoToCanvas(lat: Double, lng: Double, width: Float, height: Float): Offset {
    val fractionX = getPointOffsetFractionX(lat, lng)
    val fractionY = getPointOffsetFractionY(lat, lng)
    return Offset(width * fractionX, height * fractionY)
}

private fun getPointOffsetFractionX(lat: Double, lng: Double): Float {
    val minLng = -77.0375
    val maxLng = -77.0320
    val norm = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.0, 1.0)
    return (0.15f + norm * 0.70f).toFloat()
}

private fun getPointOffsetFractionY(lat: Double, lng: Double): Float {
    val maxLat = -12.0835
    val minLat = -12.0885
    val norm = ((maxLat - lat) / (maxLat - minLat)).coerceIn(0.0, 1.0)
    return (0.18f + norm * 0.65f).toFloat()
}

