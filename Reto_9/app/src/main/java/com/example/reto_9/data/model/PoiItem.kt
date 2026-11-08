package com.example.reto_9.data.model

import org.osmdroid.util.GeoPoint

enum class PoiCategory(
    val displayName: String,
    val overpassTagKey: String,
    val overpassTagValue: String,
    val iconEmoji: String
) {
    ALL("Todos", "", "", "📍"),
    HOSPITAL("Hospitales", "amenity", "hospital|clinic|doctors", "🏥"),
    TOURISM("Turismo", "tourism", "attraction|museum|artwork|viewpoint|hotel", "🏛️"),
    RESTAURANT("Restaurantes", "amenity", "restaurant|fast_food|cafe", "🍽️"),
    SCHOOL("Escuelas", "amenity", "school|university|college", "🏫"),
    GAS_STATION("Gasolineras", "amenity", "fuel", "⛽"),
    BANK("Bancos", "amenity", "bank|atm", "🏦"),
    PARK("Parques", "leisure", "park|garden", "🌳");

    companion object {
        fun fromName(name: String): PoiCategory {
            return entries.find { it.name == name } ?: ALL
        }
    }
}

data class PoiItem(
    val id: Long,
    val name: String,
    val category: PoiCategory,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val rawCategory: String? = null
) {
    val geoPoint: GeoPoint
        get() = GeoPoint(latitude, longitude)
}
