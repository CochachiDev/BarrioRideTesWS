package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.RideRepository
import com.example.data.signalr.SignalREvent
import com.example.data.signalr.SignalRService
import com.example.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClientTripUiState(
    val availablePoints: List<CommunityPoint> = CommunityLocations.PRESET_POINTS,
    val selectedOrigin: CommunityPoint = CommunityLocations.PRESET_POINTS[5], // Villa 12
    val selectedDestination: CommunityPoint = CommunityLocations.PRESET_POINTS[1], // Casa Club
    val estimatedDistanceKm: Double = 0.8,
    val estimatedFare: Double = 3.50,
    val estimatedMinutes: Int = 4,
    val activeTrip: Trip? = null,
    val liveDriverLocation: Pair<Double, Double>? = null,
    val availableDrivers: List<Driver> = emptyList(),
    val isRequesting: Boolean = false,
    val errorMessage: String? = null,
    val showReceiptModal: Boolean = false
)

class ClientTripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RideRepository
    private val signalR: SignalRService = SignalRService.instance

    private val _uiState = MutableStateFlow(ClientTripUiState())
    val uiState: StateFlow<ClientTripUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = RideRepository(db)
        calculateEstimates()

        viewModelScope.launch {
            repository.getAvailableDrivers().collect { drivers ->
                _uiState.value = _uiState.value.copy(availableDrivers = drivers)
            }
        }

        viewModelScope.launch {
            signalR.activeDriverLocation.collect { location ->
                _uiState.value = _uiState.value.copy(liveDriverLocation = location)
            }
        }

        viewModelScope.launch {
            signalR.events.collect { event ->
                when (event) {
                    is SignalREvent.TripStatusChanged -> {
                        if (_uiState.value.activeTrip?.id == event.tripId) {
                            val updatedTrip = _uiState.value.activeTrip?.copy(estado = event.newStatus)
                            _uiState.value = _uiState.value.copy(
                                activeTrip = updatedTrip,
                                showReceiptModal = (event.newStatus == TripStatus.FINALIZADO)
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun observeActiveTripForUser(userId: String) {
        viewModelScope.launch {
            repository.getActiveTripForUser(userId).collect { trip ->
                _uiState.value = _uiState.value.copy(
                    activeTrip = trip,
                    showReceiptModal = (trip?.estado == TripStatus.FINALIZADO)
                )
            }
        }
    }

    fun selectOrigin(point: CommunityPoint) {
        _uiState.value = _uiState.value.copy(selectedOrigin = point)
        calculateEstimates()
    }

    fun updateRealLocation(lat: Double, lng: Double, address: String) {
        val gpsPoint = CommunityPoint(
            id = "gps_origin",
            nombre = "Mi Ubicación ($address)",
            descripcion = "Obtenida por GPS en vivo",
            lat = lat,
            lng = lng,
            tipo = "GPS"
        )
        if (_uiState.value.selectedOrigin.id == "gps_origin" || _uiState.value.selectedOrigin.id == CommunityLocations.PRESET_POINTS[5].id) {
            _uiState.value = _uiState.value.copy(selectedOrigin = gpsPoint)
            calculateEstimates()
        }
    }

    fun selectDestination(point: CommunityPoint) {
        _uiState.value = _uiState.value.copy(selectedDestination = point)
        calculateEstimates()
    }

    fun swapOriginAndDestination() {
        val orig = _uiState.value.selectedOrigin
        val dest = _uiState.value.selectedDestination
        _uiState.value = _uiState.value.copy(selectedOrigin = dest, selectedDestination = orig)
        calculateEstimates()
    }

    private fun calculateEstimates() {
        val orig = _uiState.value.selectedOrigin
        val dest = _uiState.value.selectedDestination
        val dist = CommunityLocations.calculateDistanceKm(orig.lat, orig.lng, dest.lat, dest.lng)
        val fare = CommunityLocations.calculateFare(dist)
        val mins = CommunityLocations.calculateEstimateMinutes(dist)
        _uiState.value = _uiState.value.copy(
            estimatedDistanceKm = dist,
            estimatedFare = fare,
            estimatedMinutes = mins
        )
    }

    fun requestTrip(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRequesting = true, errorMessage = null)
            try {
                val trip = repository.requestTrip(
                    cliente = user,
                    origen = _uiState.value.selectedOrigin,
                    destino = _uiState.value.selectedDestination
                )
                _uiState.value = _uiState.value.copy(
                    activeTrip = trip,
                    isRequesting = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRequesting = false,
                    errorMessage = e.localizedMessage ?: "No se pudo solicitar el viaje"
                )
            }
        }
    }

    fun cancelActiveTrip() {
        val tripId = _uiState.value.activeTrip?.id ?: return
        viewModelScope.launch {
            repository.cancelTrip(tripId)
            _uiState.value = _uiState.value.copy(activeTrip = null)
        }
    }

    fun dismissReceiptModal() {
        _uiState.value = _uiState.value.copy(showReceiptModal = false, activeTrip = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
