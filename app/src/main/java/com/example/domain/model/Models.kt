package com.example.domain.model

enum class UserRole {
    CLIENTE,
    CONDUCTOR
}

enum class DriverStatus {
    DISPONIBLE,
    OCUPADO,
    DESCONECTADO
}

enum class TripStatus {
    SOLICITADO,
    ACEPTADO,
    CONDUCTOR_EN_CAMINO,
    CONDUCTOR_LLEGO,
    EN_CURSO,
    FINALIZADO,
    CANCELADO
}

data class User(
    val id: String,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val residencia: String, // e.g. "Manzana B - Lote 14" or "Torre 2 Apt 501"
    val rol: UserRole,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val activo: Boolean = true
)

data class Vehicle(
    val id: String,
    val marca: String = "GreenE-Motion",
    val modelo: String = "E-Trike 300",
    val color: String = "Verde Esmeralda",
    val placa: String = "TM-2024",
    val numeroUnidad: String = "#04",
    val tipo: String = "Trimoto Eléctrica"
)

data class Driver(
    val id: String,
    val usuarioId: String,
    val nombre: String,
    val telefono: String,
    val status: DriverStatus = DriverStatus.DISPONIBLE,
    val latitudActual: Double,
    val longitudActual: Double,
    val vehiculoId: String,
    val vehiculoInfo: Vehicle? = null,
    val calificacion: Double = 4.9
)

data class Trip(
    val id: String,
    val clienteId: String,
    val clienteNombre: String,
    val clienteTelefono: String,
    val clienteResidencia: String,
    val conductorId: String?,
    val conductorNombre: String?,
    val conductorTelefono: String?,
    val vehiculoInfo: String?,
    val origenLat: Double,
    val origenLng: Double,
    val destinoLat: Double,
    val destinoLng: Double,
    val origenTexto: String,
    val destinoTexto: String,
    val estado: TripStatus,
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val fechaInicio: Long? = null,
    val fechaFin: Long? = null,
    val precioEstimado: Double = 3.50,
    val precioFinal: Double? = null,
    val distancia: Double = 0.8, // in km
    val tiempoEstimado: Int = 4 // in minutes
)

data class CommunityPoint(
    val id: String,
    val nombre: String,
    val tipo: String, // "RECIEN", "SERVICIOS", "ENTRADA", "PARQUE"
    val lat: Double,
    val lng: Double,
    val descripcion: String
)
