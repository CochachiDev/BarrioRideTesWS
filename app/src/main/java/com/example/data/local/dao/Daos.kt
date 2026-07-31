package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DriverEntity
import com.example.data.local.entity.TripEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
}

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getDriverById(id: String): DriverEntity?

    @Query("SELECT * FROM drivers WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun getDriverByUsuarioId(usuarioId: String): DriverEntity?

    @Query("SELECT * FROM drivers WHERE status = 'DISPONIBLE'")
    fun getAvailableDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Query("UPDATE drivers SET status = :status WHERE id = :id")
    suspend fun updateDriverStatus(id: String, status: String)

    @Query("UPDATE drivers SET latitudActual = :lat, longitudActual = :lng WHERE id = :id")
    suspend fun updateDriverLocation(id: String, lat: Double, lng: Double)
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE id = :id LIMIT 1")
    suspend fun getVehicleById(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: String): TripEntity?

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    fun getTripByIdFlow(id: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE clienteId = :clienteId ORDER BY fechaSolicitud DESC")
    fun getTripsByClient(clienteId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE conductorId = :conductorId ORDER BY fechaSolicitud DESC")
    fun getTripsByDriver(conductorId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE (clienteId = :userId OR conductorId = :userId) AND estado NOT IN ('FINALIZADO', 'CANCELADO') LIMIT 1")
    fun getActiveTripForUser(userId: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE estado = 'SOLICITADO' ORDER BY fechaSolicitud DESC")
    fun getPendingRequestedTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY fechaSolicitud DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("UPDATE trips SET estado = :estado WHERE id = :id")
    suspend fun updateTripState(id: String, estado: String)

    @Query("UPDATE trips SET conductorId = :conductorId, conductorNombre = :conductorNombre, conductorTelefono = :conductorTelefono, vehiculoInfo = :vehiculoInfo, estado = 'ACEPTADO' WHERE id = :id")
    suspend fun acceptTrip(id: String, conductorId: String, conductorNombre: String, conductorTelefono: String, vehiculoInfo: String)
}
