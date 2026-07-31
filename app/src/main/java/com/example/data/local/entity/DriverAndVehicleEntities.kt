package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Driver
import com.example.domain.model.DriverStatus
import com.example.domain.model.Vehicle

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val marca: String,
    val modelo: String,
    val color: String,
    val placa: String,
    val numeroUnidad: String,
    val tipo: String
) {
    fun toDomain(): Vehicle = Vehicle(
        id = id,
        marca = marca,
        modelo = modelo,
        color = color,
        placa = placa,
        numeroUnidad = numeroUnidad,
        tipo = tipo
    )

    companion object {
        fun fromDomain(v: Vehicle): VehicleEntity = VehicleEntity(
            id = v.id,
            marca = v.marca,
            modelo = v.modelo,
            color = v.color,
            placa = v.placa,
            numeroUnidad = v.numeroUnidad,
            tipo = v.tipo
        )
    }
}

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val nombre: String,
    val telefono: String,
    val status: String,
    val latitudActual: Double,
    val longitudActual: Double,
    val vehiculoId: String,
    val calificacion: Double
) {
    fun toDomain(vehicle: Vehicle? = null): Driver = Driver(
        id = id,
        usuarioId = usuarioId,
        nombre = nombre,
        telefono = telefono,
        status = try { DriverStatus.valueOf(status) } catch (e: Exception) { DriverStatus.DISPONIBLE },
        latitudActual = latitudActual,
        longitudActual = longitudActual,
        vehiculoId = vehiculoId,
        vehiculoInfo = vehicle,
        calificacion = calificacion
    )

    companion object {
        fun fromDomain(driver: Driver): DriverEntity = DriverEntity(
            id = driver.id,
            usuarioId = driver.usuarioId,
            nombre = driver.nombre,
            telefono = driver.telefono,
            status = driver.status.name,
            latitudActual = driver.latitudActual,
            longitudActual = driver.longitudActual,
            vehiculoId = driver.vehiculoId,
            calificacion = driver.calificacion
        )
    }
}
