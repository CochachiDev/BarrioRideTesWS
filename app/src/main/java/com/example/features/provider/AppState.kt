package com.example.features.provider

import androidx.compose.runtime.Composable
import com.example.domain.model.CommunityPoint
import com.example.domain.model.User
import com.example.domain.model.UserRole

typealias AppState = com.example.provider.AppState
typealias UserLocation = com.example.provider.UserLocation
typealias AuthState = com.example.provider.AuthState

val LocalAppState = com.example.provider.LocalAppState

@Composable
fun AppStateProvider(
    appState: com.example.provider.AppState = com.example.provider.rememberAppState(),
    content: @Composable () -> Unit
) {
    com.example.provider.AppStateProvider(appState = appState, content = content)
}

@Composable
fun rememberAppState(
    initialUser: User? = null,
    initialRole: UserRole = UserRole.CLIENTE,
    initialLocation: com.example.provider.UserLocation = com.example.provider.UserLocation()
): com.example.provider.AppState {
    return com.example.provider.rememberAppState(
        initialUser = initialUser,
        initialRole = initialRole,
        initialLocation = initialLocation
    )
}
