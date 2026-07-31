package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.User
import com.example.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val email: String,
    val residencia: String,
    val rol: String,
    val fechaRegistro: Long,
    val activo: Boolean
) {
    fun toDomain(): User = User(
        id = id,
        nombre = nombre,
        apellido = apellido,
        telefono = telefono,
        email = email,
        residencia = residencia,
        rol = if (rol == UserRole.CONDUCTOR.name) UserRole.CONDUCTOR else UserRole.CLIENTE,
        fechaRegistro = fechaRegistro,
        activo = activo
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            nombre = user.nombre,
            apellido = user.apellido,
            telefono = user.telefono,
            email = user.email,
            residencia = user.residencia,
            rol = user.rol.name,
            fechaRegistro = user.fechaRegistro,
            activo = user.activo
        )
    }
}
