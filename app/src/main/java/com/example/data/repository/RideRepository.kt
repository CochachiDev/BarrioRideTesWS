package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.DriverEntity
import com.example.data.local.entity.TripEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VehicleEntity
import com.example.data.signalr.SignalRService
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

class RideRepository(
    private val db: AppDatabase,
    private val signalR: SignalRService = SignalRService.instance
) {

    // --- User & Auth Operations ---

    suspend fun getUserById(userId: String): User? {
        return db.userDao().getUserById(userId)?.toDomain()
    }

    suspend fun loginOrRegisterUser(
        nombre: String,
        apellido: String,
        telefono: String,
        email: String,
        residencia: String,
        rol: UserRole,
        vehicleInfo: Vehicle? = null
    ): User {
        val existing = db.userDao().getUserByEmail(email)
        if (existing != null) {
            val updated = existing.copy(
                nombre = nombre,
                apellido = apellido,
                telefono = telefono,
                residencia = residencia,
                rol = rol.name
            )
            db.userDao().insertUser(updated)
            return updated.toDomain()
        }

        val userId = "usr_" + UUID.randomUUID().toString().take(8)
        val newUser = User(
            id = userId,
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
            email = email,
            residencia = residencia,
            rol = rol,
            fechaRegistro = System.currentTimeMillis(),
            activo = true
        )
        db.userDao().insertUser(UserEntity.fromDomain(newUser))

        if (rol == UserRole.CONDUCTOR) {
            val vehId = "veh_" + UUID.randomUUID().toString().take(8)
            val veh = vehicleInfo ?: Vehicle(
                id = vehId,
                marca = "GreenE-Motion",
                modelo = "E-Trike 300",
                color = "Verde Esmeralda",
                placa = "TM-" + (1000..9999).random(),
                numeroUnidad = "Trimoto #" + (1..10).random()
            )
            db.vehicleDao().insertVehicle(VehicleEntity.fromDomain(veh.copy(id = vehId)))

            val driver = Driver(
                id = "drv_" + UUID.randomUUID().toString().take(8),
                usuarioId = userId,
                nombre = "$nombre $apellido",
                telefono = telefono,
                status = DriverStatus.DISPONIBLE,
                latitudActual = -12.0864,
                longitudActual = -77.0345,
                vehiculoId = vehId,
                vehiculoInfo = veh,
                calificacion = 5.0
            )
            db.driverDao().insertDriver(DriverEntity.fromDomain(driver))
        }

        return newUser
    }

    // --- Driver Operations ---

    fun getAvailableDrivers(): Flow<List<Driver>> {
        return db.driverDao().getAvailableDrivers().map { entities ->
            entities.map { entity ->
                val vehicle = db.vehicleDao().getVehicleById(entity.vehiculoId)?.toDomain()
                entity.toDomain(vehicle)
            }
        }
    }

    suspend fun getDriverByUserId(userId: String): Driver? {
        val entity = db.driverDao().getDriverByUsuarioId(userId) ?: return null
        val vehicle = db.vehicleDao().getVehicleById(entity.vehiculoId)?.toDomain()
        return entity.toDomain(vehicle)
    }

    suspend fun setDriverStatus(driverId: String, status: DriverStatus) {
        db.driverDao().updateDriverStatus(driverId, status.name)
        if (status == DriverStatus.DISPONIBLE) {
            val d = db.driverDao().getDriverById(driverId)
            if (d != null) {
                signalR.startDriverLocationBroadcaster(driverId, d.latitudActual, d.longitudActual) { lat, lng ->
                    db.driverDao().updateDriverLocation(driverId, lat, lng)
                }
            }
        } else if (status == DriverStatus.DESCONECTADO) {
            signalR.stopDriverLocationBroadcaster()
        }
    }

    suspend fun updateDriverLocation(driverId: String, lat: Double, lng: Double) {
        db.driverDao().updateDriverLocation(driverId, lat, lng)
    }

    // --- Trip Operations ---

    fun getActiveTripForUser(userId: String): Flow<Trip?> {
        return db.tripDao().getActiveTripForUser(userId).map { it?.toDomain() }
    }

    fun getPendingRequestedTrips(): Flow<List<Trip>> {
        return db.tripDao().getPendingRequestedTrips().map { list -> list.map { it.toDomain() } }
    }

    fun getTripHistory(userId: String): Flow<List<Trip>> {
        return db.tripDao().getTripsByClient(userId).map { list -> list.map { it.toDomain() } }
    }

    fun getDriverTripHistory(driverId: String): Flow<List<Trip>> {
        return db.tripDao().getTripsByDriver(driverId).map { list -> list.map { it.toDomain() } }
    }

    suspend fun requestTrip(
        cliente: User,
        origen: CommunityPoint,
        destino: CommunityPoint
    ): Trip {
        // Business Rule: One active trip per client
        val existingActive = db.tripDao().getActiveTripForUser(cliente.id).firstOrNull()
        if (existingActive != null) {
            return existingActive.toDomain()
        }

        val dist = CommunityLocations.calculateDistanceKm(origen.lat, origen.lng, destino.lat, destino.lng)
        val price = CommunityLocations.calculateFare(dist)
        val timeEst = CommunityLocations.calculateEstimateMinutes(dist)

        val tripId = "trip_" + UUID.randomUUID().toString().take(8)
        val newTrip = Trip(
            id = tripId,
            clienteId = cliente.id,
            clienteNombre = "${cliente.nombre} ${cliente.apellido}",
            clienteTelefono = cliente.telefono,
            clienteResidencia = cliente.residencia,
            conductorId = null,
            conductorNombre = null,
            conductorTelefono = null,
            vehiculoInfo = null,
            origenLat = origen.lat,
            origenLng = origen.lng,
            destinoLat = destino.lat,
            destinoLng = destino.lng,
            origenTexto = origen.nombre,
            destinoTexto = destino.nombre,
            estado = TripStatus.SOLICITADO,
            fechaSolicitud = System.currentTimeMillis(),
            precioEstimado = price,
            distancia = dist,
            tiempoEstimado = timeEst
        )

        db.tripDao().insertTrip(TripEntity.fromDomain(newTrip))
        signalR.emitTripRequested(newTrip)

        // Find default available driver if available to auto-dispatch demo simulation
        val drivers = db.driverDao().getAllDrivers().firstOrNull()
        val defaultDriver = drivers?.firstOrNull()?.let { d ->
            val veh = db.vehicleDao().getVehicleById(d.vehiculoId)?.toDomain()
            d.toDomain(veh)
        }

        if (defaultDriver != null) {
            signalR.simulateDriverResponseAndTripProgress(
                trip = newTrip,
                driver = defaultDriver,
                onUpdateTripState = { tId, status, finalPrice, _ ->
                    updateTripStatus(tId, status, finalPrice)
                },
                onUpdateDriverLocation = { drvId, lat, lng ->
                    db.driverDao().updateDriverLocation(drvId, lat, lng)
                }
            )
        }

        return newTrip
    }

    suspend fun acceptTripByDriver(tripId: String, driver: Driver): Boolean {
        // Business Rule: One active trip per driver
        val activeDriverTrip = db.tripDao().getActiveTripForUser(driver.usuarioId).firstOrNull()
        if (activeDriverTrip != null) {
            return false
        }

        val vehStr = driver.vehiculoInfo?.let { "${it.marca} ${it.modelo} (${it.numeroUnidad} - Placa ${it.placa})" }
            ?: "Trimoto Eléctrica #04 (Placa TM-8821)"

        db.tripDao().acceptTrip(
            id = tripId,
            conductorId = driver.id,
            conductorNombre = driver.nombre,
            conductorTelefono = driver.telefono,
            vehiculoInfo = vehStr
        )
        db.driverDao().updateDriverStatus(driver.id, DriverStatus.OCUPADO.name)

        val updated = db.tripDao().getTripById(tripId)?.toDomain()
        if (updated != null) {
            signalR.emitTripAccepted(updated)
        }
        return true
    }

    suspend fun updateTripStatus(tripId: String, status: TripStatus, finalPrice: Double? = null) {
        val tripEntity = db.tripDao().getTripById(tripId) ?: return
        val currentTrip = tripEntity.toDomain()

        val updated = currentTrip.copy(
            estado = status,
            fechaInicio = if (status == TripStatus.EN_CURSO) System.currentTimeMillis() else currentTrip.fechaInicio,
            fechaFin = if (status == TripStatus.FINALIZADO || status == TripStatus.CANCELADO) System.currentTimeMillis() else currentTrip.fechaFin,
            precioFinal = finalPrice ?: if (status == TripStatus.FINALIZADO) currentTrip.precioEstimado else currentTrip.precioFinal
        )

        db.tripDao().insertTrip(TripEntity.fromDomain(updated))

        if (status == TripStatus.FINALIZADO || status == TripStatus.CANCELADO) {
            signalR.cancelTripSimulation()
            currentTrip.conductorId?.let { drvId ->
                db.driverDao().updateDriverStatus(drvId, DriverStatus.DISPONIBLE.name)
            }
        }

        signalR.emitTripStatusChanged(tripId, status)
    }

    suspend fun cancelTrip(tripId: String) {
        updateTripStatus(tripId, TripStatus.CANCELADO)
        signalR.emitTripCancelled(tripId)
    }
}
