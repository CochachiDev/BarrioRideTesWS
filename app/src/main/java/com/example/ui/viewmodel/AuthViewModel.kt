package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.RideRepository
import com.example.domain.model.Driver
import com.example.domain.model.User
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val currentDriver: Driver? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeRole: UserRole = UserRole.CLIENTE
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RideRepository

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = RideRepository(db)
        loadDefaultUser()
    }

    private fun loadDefaultUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            var user = repository.getUserById("usr_resident_1")
            if (user == null) {
                user = repository.loginOrRegisterUser(
                    nombre = "Carlos",
                    apellido = "Mendoza",
                    telefono = "+51 987 654 321",
                    email = "carlos.vecino@inmobiliaria.com",
                    residencia = "Manzana C - Lote 12 (Las Palmeras)",
                    rol = UserRole.CLIENTE
                )
            }
            _uiState.value = _uiState.value.copy(
                currentUser = user,
                activeRole = user.rol,
                isLoading = false
            )
        }
    }

    fun loginDemoResident() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = repository.loginOrRegisterUser(
                nombre = "Carlos",
                apellido = "Mendoza",
                telefono = "+51 987 654 321",
                email = "carlos.vecino@inmobiliaria.com",
                residencia = "Manzana C - Lote 12 (Las Palmeras)",
                rol = UserRole.CLIENTE
            )
            _uiState.value = _uiState.value.copy(
                currentUser = user,
                currentDriver = null,
                activeRole = UserRole.CLIENTE,
                isLoading = false
            )
        }
    }

    fun loginDemoDriver() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = repository.loginOrRegisterUser(
                nombre = "Mateo",
                apellido = "Quispe",
                telefono = "+51 912 345 678",
                email = "mateo.conductor@inmobiliaria.com",
                residencia = "Servicios Generales Urbanización",
                rol = UserRole.CONDUCTOR
            )
            val driver = repository.getDriverByUserId(user.id)
            _uiState.value = _uiState.value.copy(
                currentUser = user,
                currentDriver = driver,
                activeRole = UserRole.CONDUCTOR,
                isLoading = false
            )
        }
    }

    fun switchRole(role: UserRole) {
        if (role == UserRole.CLIENTE) {
            loginDemoResident()
        } else {
            loginDemoDriver()
        }
    }

    fun registerOrLoginCustomUser(
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        residencia: String,
        rol: UserRole
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = repository.loginOrRegisterUser(
                    nombre = nombre,
                    apellido = apellido,
                    telefono = telefono,
                    email = email,
                    residencia = residencia,
                    rol = rol
                )
                val driver = if (rol == UserRole.CONDUCTOR) repository.getDriverByUserId(user.id) else null
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    currentDriver = driver,
                    activeRole = rol,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al autenticar"
                )
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
