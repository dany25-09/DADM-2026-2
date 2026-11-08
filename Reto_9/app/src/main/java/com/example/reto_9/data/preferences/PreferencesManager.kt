package com.example.reto_9.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.reto_9.data.model.PoiCategory

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var radiusKm: Float
        get() = prefs.getFloat(KEY_RADIUS_KM, DEFAULT_RADIUS_KM)
        set(value) = prefs.edit().putFloat(KEY_RADIUS_KM, value).apply()

    var selectedCategory: PoiCategory
        get() {
            val name = prefs.getString(KEY_CATEGORY, PoiCategory.ALL.name) ?: PoiCategory.ALL.name
            return PoiCategory.fromName(name)
        }
        set(value) = prefs.edit().putString(KEY_CATEGORY, value.name).apply()

    companion object {
        private const val PREFS_NAME = "reto9_gps_prefs"
        private const val KEY_RADIUS_KM = "search_radius_km"
        private const val KEY_CATEGORY = "selected_poi_category"
        const val DEFAULT_RADIUS_KM = 2.0f
    }
}
