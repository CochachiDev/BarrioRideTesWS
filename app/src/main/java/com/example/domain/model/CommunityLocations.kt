package com.example.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object CommunityLocations {

    // Reference center of the private urbanisation
    const val CENTER_LAT = -12.0864
    const val CENTER_LNG = -77.0345

    val PRESET_POINTS = listOf(
        CommunityPoint(
            id = "loc_gate",
            nombre = "Portón Principal & Caseta",
            tipo = "ENTRADA",
            lat = -12.0880,
            lng = -77.0370,
            descripcion = "Acceso vehicular y caseta de seguridad"
        ),
        CommunityPoint(
            id = "loc_clubhouse",
            nombre = "Casa Club & Piscina",
            tipo = "SERVICIOS",
            lat = -12.0850,
            lng = -77.0330,
            descripcion = "Zona social, piscina y salón de eventos"
        ),
        CommunityPoint(
            id = "loc_sports",
            nombre = "Canchas Deportivas",
            tipo = "SERVICIOS",
            lat = -12.0840,
            lng = -77.0360,
            descripcion = "Canchas de tenis, futbol y padel"
        ),
        CommunityPoint(
            id = "loc_market",
            nombre = "Minimarket & Plaza",
            tipo = "SERVICIOS",
            lat = -12.0870,
            lng = -77.0340,
            descripcion = "Tienda de conveniencia y cafetería"
        ),
        CommunityPoint(
            id = "loc_park",
            nombre = "Parque Central",
            tipo = "PARQUE",
            lat = -12.0860,
            lng = -77.0345,
            descripcion = "Área verde central y juegos infantiles"
        ),
        CommunityPoint(
            id = "loc_villas",
            nombre = "Manzana C - Villa 12 (Las Palmeras)",
            tipo = "RECIEN",
            lat = -12.0875,
            lng = -77.0325,
            descripcion = "Zona de casas residenciales"
        ),
        CommunityPoint(
            id = "loc_towers",
            nombre = "Torre 2 Apt 501 (Los Fresnos)",
            tipo = "RECIEN",
            lat = -12.0845,
            lng = -77.0350,
            descripcion = "Edificios de departamentos"
        )
    )

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = r * c
        return if (dist < 0.1) 0.3 else Math.round(dist * 100.0) / 100.0
    }

    fun calculateFare(distanceKm: Double): Double {
        // Community flat fare formula: Base S/. 3.00 + S/. 1.00 per km (approx S/. 3.50 standard ride)
        val fare = 3.00 + (distanceKm * 1.0)
        return Math.round(fare * 2) / 2.0 // round to nearest 0.50
    }

    fun calculateEstimateMinutes(distanceKm: Double): Int {
        // Electric trimoto average community speed: 15 km/h
        val minutes = (distanceKm / 15.0 * 60).toInt() + 2 // + 2 min pickup
        return minutes.coerceAtLeast(3)
    }
}
