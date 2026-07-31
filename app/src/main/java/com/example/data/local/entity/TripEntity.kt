package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Trip
import com.example.domain.model.TripStatus

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
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
    val estado: String,
    val fechaSolicitud: Long,
    val fechaInicio: Long?,
    val fechaFin: Long?,
    val precioEstimado: Double,
    val precioFinal: Double?,
    val distancia: Double,
    val tiempoEstimado: Int
) {
    fun toDomain(): Trip = Trip(
        id = id,
        clienteId = clienteId,
        clienteNombre = clienteNombre,
        clienteTelefono = clienteTelefono,
        clienteResidencia = clienteResidencia,
        conductorId = conductorId,
        conductorNombre = conductorNombre,
        conductorTelefono = conductorTelefono,
        vehiculoInfo = vehiculoInfo,
        origenLat = origenLat,
        origenLng = origenLng,
        destinoLat = destinoLat,
        destinoLng = destinoLng,
        origenTexto = origenTexto,
        destinoTexto = destinoTexto,
        estado = try { TripStatus.valueOf(estado) } catch (e: Exception) { TripStatus.SOLICITADO },
        fechaSolicitud = fechaSolicitud,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        precioEstimado = precioEstimado,
        precioFinal = precioFinal,
        distancia = distancia,
        tiempoEstimado = tiempoEstimado
    )

    companion object {
        fun fromDomain(trip: Trip): TripEntity = TripEntity(
            id = trip.id,
            clienteId = trip.clienteId,
            clienteNombre = trip.clienteNombre,
            clienteTelefono = trip.clienteTelefono,
            clienteResidencia = trip.clienteResidencia,
            conductorId = trip.conductorId,
            conductorNombre = trip.conductorNombre,
            conductorTelefono = trip.conductorTelefono,
            vehiculoInfo = trip.vehiculoInfo,
            origenLat = trip.origenLat,
            origenLng = trip.origenLng,
            destinoLat = trip.destinoLat,
            destinoLng = trip.destinoLng,
            origenTexto = trip.origenTexto,
            destinoTexto = trip.destinoTexto,
            estado = trip.estado.name,
            fechaSolicitud = trip.fechaSolicitud,
            fechaInicio = trip.fechaInicio,
            fechaFin = trip.fechaFin,
            precioEstimado = trip.precioEstimado,
            precioFinal = trip.precioFinal,
            distancia = trip.distancia,
            tiempoEstimado = trip.tiempoEstimado
        )
    }
}
