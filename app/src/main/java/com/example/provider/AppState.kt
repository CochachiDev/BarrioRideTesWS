package com.example.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.domain.model.CommunityPoint
import com.example.domain.model.User
import com.example.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Representa el estado actual de la ubicación del usuario en la aplicación.
 */
data class UserLocation(
    val latitude: Double = -12.0856,
    val longitude: Double = -77.0348,
    val address: String = "Casa Club - Urbanización El Retiro",
    val communityPoint: CommunityPoint? = null,
    val isPermissionGranted: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Representa el estado de autenticación de la aplicación.
 */
data class AuthState(
    val currentUser: User? = null,
    val activeRole: UserRole = UserRole.CLIENTE,
    val isAuthenticated: Boolean = currentUser != null,
    val authToken: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Clase central AppState para la gestión del estado de autenticación y ubicación
 * del usuario en la capa provider dentro del patrón de arquitectura limpia.
 */
@Stable
class AppState(
    initialUser: User? = null,
    initialRole: UserRole = UserRole.CLIENTE,
    initialLocation: UserLocation = UserLocation()
) {
    // Estado de Autenticación
    var currentUser by mutableStateOf(initialUser)
        private set

    var activeRole by mutableStateOf(initialRole)
        private set

    var isAuthenticated by mutableStateOf(initialUser != null)
        private set

    var authToken by mutableStateOf<String?>(null)
        private set

    var isAuthLoading by mutableStateOf(false)
        private set

    var authError by mutableStateOf<String?>(null)
        private set

    // Estado de Ubicación del Usuario
    var currentLocation by mutableStateOf(initialLocation)
        private set

    var selectedCommunityPoint by mutableStateOf<CommunityPoint?>(initialLocation.communityPoint)
        private set

    // Flujos reactivos para observadores asíncronos
    private val _authStateFlow = MutableStateFlow(
        AuthState(
            currentUser = initialUser,
            activeRole = initialRole,
            isAuthenticated = initialUser != null
        )
    )
    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    private val _locationStateFlow = MutableStateFlow(initialLocation)
    val locationStateFlow: StateFlow<UserLocation> = _locationStateFlow.asStateFlow()

    /**
     * Establece el usuario autenticado y actualiza el estado de la sesión.
     */
    fun setUser(user: User?, token: String? = null) {
        currentUser = user
        isAuthenticated = user != null
        authToken = token
        authError = null
        isAuthLoading = false
        if (user != null) {
            activeRole = user.rol
        }
        syncAuthState()
    }

    /**
     * Cambia el rol activo del usuario (CLIENTE o CONDUCTOR).
     */
    fun setRole(role: UserRole) {
        activeRole = role
        currentUser = currentUser?.copy(rol = role)
        syncAuthState()
    }

    /**
     * Establece el estado de carga para la autenticación.
     */
    fun updateAuthLoading(loading: Boolean) {
        isAuthLoading = loading
        syncAuthState()
    }

    /**
     * Establece el mensaje de error de autenticación.
     */
    fun updateAuthError(error: String?) {
        authError = error
        isAuthLoading = false
        syncAuthState()
    }

    /**
     * Cierra la sesión activa y limpia los datos de autenticación.
     */
    fun logout() {
        currentUser = null
        isAuthenticated = false
        authToken = null
        authError = null
        isAuthLoading = false
        syncAuthState()
    }

    /**
     * Actualiza las coordenadas y la dirección de ubicación del usuario.
     */
    fun updateLocation(
        latitude: Double,
        longitude: Double,
        address: String = currentLocation.address,
        point: CommunityPoint? = currentLocation.communityPoint
    ) {
        currentLocation = UserLocation(
            latitude = latitude,
            longitude = longitude,
            address = address,
            communityPoint = point,
            isPermissionGranted = currentLocation.isPermissionGranted,
            lastUpdated = System.currentTimeMillis()
        )
        if (point != null) {
            selectedCommunityPoint = point
        }
        _locationStateFlow.value = currentLocation
    }

    /**
     * Actualiza la ubicación del usuario a partir de un punto de la comunidad.
     */
    fun updateLocationFromCommunityPoint(point: CommunityPoint) {
        selectedCommunityPoint = point
        updateLocation(
            latitude = point.lat,
            longitude = point.lng,
            address = point.nombre,
            point = point
        )
    }

    /**
     * Actualiza el permiso de ubicación concedido.
     */
    fun setLocationPermissionGranted(granted: Boolean) {
        currentLocation = currentLocation.copy(isPermissionGranted = granted)
        _locationStateFlow.value = currentLocation
    }

    private fun syncAuthState() {
        _authStateFlow.value = AuthState(
            currentUser = currentUser,
            activeRole = activeRole,
            isAuthenticated = isAuthenticated,
            authToken = authToken,
            isLoading = isAuthLoading,
            errorMessage = authError
        )
    }
}

/**
 * Reference CompositionLocal para acceder a [AppState] en la jerarquía de Jetpack Compose.
 */
val LocalAppState = compositionLocalOf<AppState> {
    error("No se proporcionó AppState. Envuelve la jerarquía en AppStateProvider.")
}

/**
 * Composable Provider que inyecta [AppState] en el árbol de Compose.
 */
@Composable
fun AppStateProvider(
    appState: AppState = rememberAppState(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAppState provides appState) {
        content()
    }
}

/**
 * Función helper para recordar una instancia de [AppState] en un composables.
 */
@Composable
fun rememberAppState(
    initialUser: User? = null,
    initialRole: UserRole = UserRole.CLIENTE,
    initialLocation: UserLocation = UserLocation()
): AppState {
    return remember {
        AppState(
            initialUser = initialUser,
            initialRole = initialRole,
            initialLocation = initialLocation
        )
    }
}
