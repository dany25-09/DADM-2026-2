package com.example.reto_9.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reto_9.data.model.PoiCategory
import com.example.reto_9.data.model.PoiItem
import com.example.reto_9.data.network.NominatimResult
import com.example.reto_9.data.preferences.PreferencesManager
import com.example.reto_9.data.repository.PoiRepository
import com.example.reto_9.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class MapUiState(
    val centerLocation: GeoPoint = GeoPoint(4.6097102, -74.081749), // Default Colombia coordinates (Bogotá)
    val userGpsLocation: GeoPoint? = null,
    val radiusKm: Float = PreferencesManager.DEFAULT_RADIUS_KM,
    val selectedCategory: PoiCategory = PoiCategory.ALL,
    val poiList: List<PoiItem> = emptyList(),
    val selectedPoi: PoiItem? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSettingsDialog: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<NominatimResult> = emptyList(),
    val isSearchingAddress: Boolean = false
)

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val poiRepository = PoiRepository()
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow(
        MapUiState(
            radiusKm = preferencesManager.radiusKm,
            selectedCategory = preferencesManager.selectedCategory
        )
    )
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Initial fetch using default/saved location
        refreshPois()
    }

    fun requestGpsLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val gpsPoint = locationHelper.getCurrentLocation()
            if (gpsPoint != null) {
                _uiState.update {
                    it.copy(
                        centerLocation = gpsPoint,
                        userGpsLocation = gpsPoint,
                        isLoading = false
                    )
                }
                refreshPois()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo obtener la ubicación GPS actual. Verifique que el GPS esté activado."
                    )
                }
            }
        }
    }

    fun setCenterLocation(location: GeoPoint) {
        _uiState.update { it.copy(centerLocation = location, selectedPoi = null) }
        refreshPois()
    }

    fun setRadiusKm(radius: Float) {
        preferencesManager.radiusKm = radius
        _uiState.update { it.copy(radiusKm = radius) }
        refreshPois()
    }

    fun setSelectedCategory(category: PoiCategory) {
        preferencesManager.selectedCategory = category
        _uiState.update { it.copy(selectedCategory = category) }
        refreshPois()
    }

    fun selectPoi(poi: PoiItem?) {
        _uiState.update { it.copy(selectedPoi = poi) }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.trim().length >= 3) {
            searchAddress(query.trim())
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun selectSearchResult(result: NominatimResult) {
        try {
            val lat = result.lat.toDouble()
            val lon = result.lon.toDouble()
            val newPoint = GeoPoint(lat, lon)
            _uiState.update {
                it.copy(
                    centerLocation = newPoint,
                    searchQuery = result.displayName,
                    searchResults = emptyList()
                )
            }
            refreshPois()
        } catch (_: Exception) {
            _uiState.update { it.copy(errorMessage = "Error al procesar ubicación seleccionada") }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    private fun searchAddress(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingAddress = true) }
            val results = poiRepository.searchLocation(query)
            _uiState.update { it.copy(searchResults = results, isSearchingAddress = false) }
        }
    }

    fun refreshPois() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val pois = poiRepository.fetchNearbyPois(
                latitude = currentState.centerLocation.latitude,
                longitude = currentState.centerLocation.longitude,
                radiusKm = currentState.radiusKm,
                category = currentState.selectedCategory
            )

            _uiState.update {
                it.copy(
                    poiList = pois,
                    isLoading = false,
                    errorMessage = if (pois.isEmpty()) "No se encontraron puntos de interés en un radio de ${currentState.radiusKm} km." else null
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
