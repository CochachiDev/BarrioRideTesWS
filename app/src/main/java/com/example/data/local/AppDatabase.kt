package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DriverDao
import com.example.data.local.dao.TripDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.VehicleDao
import com.example.data.local.entity.DriverEntity
import com.example.data.local.entity.TripEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VehicleEntity
import com.example.domain.model.DriverStatus
import com.example.domain.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DriverEntity::class,
        VehicleEntity::class,
        TripEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun driverDao(): DriverDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "barrioride_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default community data
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            // Demo Resident User
            val resident = UserEntity(
                id = "usr_resident_1",
                nombre = "Carlos",
                apellido = "Mendoza",
                telefono = "+51 987 654 321",
                email = "carlos.vecino@inmobiliaria.com",
                residencia = "Manzana C - Lote 12 (Las Palmeras)",
                rol = UserRole.CLIENTE.name,
                fechaRegistro = System.currentTimeMillis() - 86400000L * 15,
                activo = true
            )

            // Demo Driver User
            val driverUser = UserEntity(
                id = "usr_driver_1",
                nombre = "Mateo",
                apellido = "Quispe",
                telefono = "+51 912 345 678",
                email = "mateo.conductor@inmobiliaria.com",
                residencia = "Servicios Generales Urbanización",
                rol = UserRole.CONDUCTOR.name,
                fechaRegistro = System.currentTimeMillis() - 86400000L * 30,
                activo = true
            )

            val vehicle = VehicleEntity(
                id = "veh_01",
                marca = "GreenE-Motion",
                modelo = "E-Trike 300 Eco",
                color = "Verde Esmeralda",
                placa = "TM-8821",
                numeroUnidad = "Trimoto #04",
                tipo = "Trimoto Eléctrica"
            )

            val driver = DriverEntity(
                id = "drv_01",
                usuarioId = driverUser.id,
                nombre = "Mateo Quispe",
                telefono = driverUser.telefono,
                status = DriverStatus.DISPONIBLE.name,
                latitudActual = -12.0864,
                longitudActual = -77.0345,
                vehiculoId = vehicle.id,
                calificacion = 4.95
            )

            db.userDao().insertUser(resident)
            db.userDao().insertUser(driverUser)
            db.vehicleDao().insertVehicle(vehicle)
            db.driverDao().insertDriver(driver)
        }
    }
}
