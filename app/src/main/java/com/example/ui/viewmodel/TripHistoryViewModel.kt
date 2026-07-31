package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.RideRepository
import com.example.domain.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripHistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false
)

class TripHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RideRepository = RideRepository(AppDatabase.getInstance(application))

    private val _uiState = MutableStateFlow(TripHistoryUiState())
    val uiState: StateFlow<TripHistoryUiState> = _uiState.asStateFlow()

    fun loadHistoryForUser(userId: String, isDriver: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val flow = if (isDriver) repository.getDriverTripHistory(userId) else repository.getTripHistory(userId)
            flow.collect { list ->
                _uiState.value = _uiState.value.copy(
                    trips = list,
                    isLoading = false
                )
            }
        }
    }
}
