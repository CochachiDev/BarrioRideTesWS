package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.RideRepository
import com.example.data.signalr.SignalREvent
import com.example.data.signalr.SignalRService
import com.example.domain.model.Driver
import com.example.domain.model.DriverStatus
import com.example.domain.model.Trip
import com.example.domain.model.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverConsoleUiState(
    val currentDriver: Driver? = null,
    val driverStatus: DriverStatus = DriverStatus.DISPONIBLE,
    val pendingRequestedTrips: List<Trip> = emptyList(),
    val incomingTripRequest: Trip? = null,
    val activeDriverTrip: Trip? = null,
    val completedTripsCount: Int = 0,
    val totalEarnings: Double = 0.0,
    val isLoading: Boolean = false,
    val message: String? = null
)

class DriverConsoleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RideRepository
    private val signalR: SignalRService = SignalRService.instance

    private val _uiState = MutableStateFlow(DriverConsoleUiState())
    val uiState: StateFlow<DriverConsoleUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = RideRepository(db)

        viewModelScope.launch {
            repository.getPendingRequestedTrips().collect { trips ->
                _uiState.value = _uiState.value.copy(
                    pendingRequestedTrips = trips,
                    incomingTripRequest = if (_uiState.value.activeDriverTrip == null && trips.isNotEmpty()) trips.first() else null
                )
            }
        }

        viewModelScope.launch {
            signalR.events.collect { event ->
                when (event) {
                    is SignalREvent.TripRequested -> {
                        if (_uiState.value.activeDriverTrip == null && _uiState.value.driverStatus == DriverStatus.DISPONIBLE) {
                            _uiState.value = _uiState.value.copy(incomingTripRequest = event.trip)
                        }
                    }
                    is SignalREvent.TripCancelled -> {
                        if (_uiState.value.incomingTripRequest?.id == event.tripId) {
                            _uiState.value = _uiState.value.copy(incomingTripRequest = null)
                        }
                        if (_uiState.value.activeDriverTrip?.id == event.tripId) {
                            _uiState.value = _uiState.value.copy(activeDriverTrip = null)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun initializeDriver(driverUserId: String) {
        viewModelScope.launch {
            val driver = repository.getDriverByUserId(driverUserId)
            if (driver != null) {
                _uiState.value = _uiState.value.copy(
                    currentDriver = driver,
                    driverStatus = driver.status
                )
                repository.setDriverStatus(driver.id, driver.status)
                observeDriverTrips(driver.id)
            }
        }
    }

    private fun observeDriverTrips(driverId: String) {
        viewModelScope.launch {
            repository.getDriverTripHistory(driverId).collect { trips ->
                val completed = trips.filter { it.estado == TripStatus.FINALIZADO }
                val earnings = completed.sumOf { it.precioFinal ?: it.precioEstimado }
                val active = trips.firstOrNull { it.estado != TripStatus.FINALIZADO && it.estado != TripStatus.CANCELADO }
                _uiState.value = _uiState.value.copy(
                    completedTripsCount = completed.size,
                    totalEarnings = earnings,
                    activeDriverTrip = active
                )
            }
        }
    }

    fun setStatus(status: DriverStatus) {
        val driver = _uiState.value.currentDriver ?: return
        viewModelScope.launch {
            repository.setDriverStatus(driver.id, status)
            _uiState.value = _uiState.value.copy(
                driverStatus = status,
                currentDriver = driver.copy(status = status)
            )
        }
    }

    fun updateDriverLocation(lat: Double, lng: Double) {
        val driver = _uiState.value.currentDriver ?: return
        val updatedDriver = driver.copy(latitudActual = lat, longitudActual = lng)
        _uiState.value = _uiState.value.copy(currentDriver = updatedDriver)
        signalR.updateDriverLocation(driver.id, lat, lng)
    }

    fun acceptIncomingTrip() {
        val trip = _uiState.value.incomingTripRequest ?: return
        val driver = _uiState.value.currentDriver ?: return

        viewModelScope.launch {
            val success = repository.acceptTripByDriver(trip.id, driver)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    incomingTripRequest = null,
                    driverStatus = DriverStatus.OCUPADO,
                    message = "¡Viaje aceptado! Dirígete al punto de encuentro."
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    message = "No pudiste aceptar el viaje. Ya tienes un viaje activo."
                )
            }
        }
    }

    fun rejectIncomingTrip() {
        _uiState.value = _uiState.value.copy(incomingTripRequest = null)
    }

    fun updateTripProgress(nextState: TripStatus) {
        val trip = _uiState.value.activeDriverTrip ?: return
        viewModelScope.launch {
            repository.updateTripStatus(trip.id, nextState)
            if (nextState == TripStatus.FINALIZADO) {
                setStatus(DriverStatus.DISPONIBLE)
                _uiState.value = _uiState.value.copy(
                    activeDriverTrip = null,
                    message = "¡Viaje finalizado exitosamente! Ganancia añadida."
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
