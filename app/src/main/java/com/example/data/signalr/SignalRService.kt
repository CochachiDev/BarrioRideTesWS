package com.example.data.signalr

import com.example.domain.model.Driver
import com.example.domain.model.DriverStatus
import com.example.domain.model.Trip
import com.example.domain.model.TripStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SignalREvent {
    data class TripRequested(val trip: Trip) : SignalREvent()
    data class TripAccepted(val trip: Trip) : SignalREvent()
    data class TripStatusChanged(val tripId: String, val newStatus: TripStatus) : SignalREvent()
    data class DriverLocationUpdated(val driverId: String, val lat: Double, val lng: Double) : SignalREvent()
    data class TripCancelled(val tripId: String) : SignalREvent()
}

/**
 * Real-time Communication Hub simulating ASP.NET Core SignalR WebSockets.
 * Handles location broadcast loops every 5s, trip updates, and automated driver dispatching.
 */
class SignalRService private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _events = MutableSharedFlow<SignalREvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SignalREvent> = _events.asSharedFlow()

    private val _activeDriverLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val activeDriverLocation: StateFlow<Pair<Double, Double>?> = _activeDriverLocation.asStateFlow()

    private var driverPulseJob: Job? = null
    private var tripSimulationJob: Job? = null

    fun startDriverLocationBroadcaster(
        driverId: String,
        initialLat: Double,
        initialLng: Double,
        onLocationUpdate: suspend (Double, Double) -> Unit
    ) {
        driverPulseJob?.cancel()
        var currentLat = initialLat
        var currentLng = initialLng

        driverPulseJob = scope.launch {
            while (isActive) {
                // Slightly simulate subtle trimoto idling / movement pulse
                currentLat += (Math.random() - 0.5) * 0.0001
                currentLng += (Math.random() - 0.5) * 0.0001
                _activeDriverLocation.value = Pair(currentLat, currentLng)
                _events.emit(SignalREvent.DriverLocationUpdated(driverId, currentLat, currentLng))
                onLocationUpdate(currentLat, currentLng)
                delay(5000L) // 5 seconds GPS pulse as requested
            }
        }
    }

    fun stopDriverLocationBroadcaster() {
        driverPulseJob?.cancel()
        driverPulseJob = null
    }

    fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        _activeDriverLocation.value = Pair(lat, lng)
        scope.launch {
            _events.emit(SignalREvent.DriverLocationUpdated(driverId, lat, lng))
        }
    }

    suspend fun emitTripRequested(trip: Trip) {
        _events.emit(SignalREvent.TripRequested(trip))
    }

    suspend fun emitTripAccepted(trip: Trip) {
        _events.emit(SignalREvent.TripAccepted(trip))
    }

    suspend fun emitTripStatusChanged(tripId: String, status: TripStatus) {
        _events.emit(SignalREvent.TripStatusChanged(tripId, status))
    }

    suspend fun emitTripCancelled(tripId: String) {
        _events.emit(SignalREvent.TripCancelled(tripId))
    }

    /**
     * Simulates automated driver behavior when requested if testing in resident mode without second manual driver input.
     */
    fun simulateDriverResponseAndTripProgress(
        trip: Trip,
        driver: Driver,
        onUpdateTripState: suspend (String, TripStatus, Double?, Double?) -> Unit,
        onUpdateDriverLocation: suspend (String, Double, Double) -> Unit
    ) {
        tripSimulationJob?.cancel()
        tripSimulationJob = scope.launch {
            // Step 1: Wait 3s then Driver Accepts
            delay(3000L)
            onUpdateTripState(trip.id, TripStatus.ACEPTADO, null, null)
            _events.emit(SignalREvent.TripStatusChanged(trip.id, TripStatus.ACEPTADO))

            // Step 2: Driver on the way to Pickup
            delay(2000L)
            onUpdateTripState(trip.id, TripStatus.CONDUCTOR_EN_CAMINO, null, null)
            _events.emit(SignalREvent.TripStatusChanged(trip.id, TripStatus.CONDUCTOR_EN_CAMINO))

            // Smoothly move driver from current position to pickup (Origen)
            var steps = 10
            for (i in 1..steps) {
                delay(1200L)
                val progress = i / steps.toDouble()
                val curLat = driver.latitudActual + (trip.origenLat - driver.latitudActual) * progress
                val curLng = driver.longitudActual + (trip.origenLng - driver.longitudActual) * progress
                _activeDriverLocation.value = Pair(curLat, curLng)
                _events.emit(SignalREvent.DriverLocationUpdated(driver.id, curLat, curLng))
                onUpdateDriverLocation(driver.id, curLat, curLng)
            }

            // Step 3: Driver Arrived at Pickup
            delay(1500L)
            onUpdateTripState(trip.id, TripStatus.CONDUCTOR_LLEGO, null, null)
            _events.emit(SignalREvent.TripStatusChanged(trip.id, TripStatus.CONDUCTOR_LLEGO))

            // Step 4: Start Trip after resident boards
            delay(3000L)
            onUpdateTripState(trip.id, TripStatus.EN_CURSO, null, null)
            _events.emit(SignalREvent.TripStatusChanged(trip.id, TripStatus.EN_CURSO))

            // Move driver from Origen to Destino
            steps = 15
            for (i in 1..steps) {
                delay(1200L)
                val progress = i / steps.toDouble()
                val curLat = trip.origenLat + (trip.destinoLat - trip.origenLat) * progress
                val curLng = trip.origenLng + (trip.destinoLng - trip.origenLng) * progress
                _activeDriverLocation.value = Pair(curLat, curLng)
                _events.emit(SignalREvent.DriverLocationUpdated(driver.id, curLat, curLng))
                onUpdateDriverLocation(driver.id, curLat, curLng)
            }

            // Step 5: Finish Trip upon arrival
            delay(1500L)
            onUpdateTripState(trip.id, TripStatus.FINALIZADO, trip.precioEstimado, null)
            _events.emit(SignalREvent.TripStatusChanged(trip.id, TripStatus.FINALIZADO))
        }
    }

    fun cancelTripSimulation() {
        tripSimulationJob?.cancel()
        tripSimulationJob = null
    }

    companion object {
        val instance: SignalRService by lazy { SignalRService() }
    }
}
