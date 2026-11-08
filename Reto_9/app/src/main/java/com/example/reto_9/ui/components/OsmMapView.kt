package com.example.reto_9.ui.components

import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.reto_9.data.model.PoiItem
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow

private val OPEN_STREET_MAP_TILE_SOURCE = XYTileSource(
    "OpenStreetMap_FR",
    0, 19, 256, ".png",
    arrayOf(
        "https://a.tile.openstreetmap.fr/osmfr/",
        "https://b.tile.openstreetmap.fr/osmfr/",
        "https://c.tile.openstreetmap.fr/osmfr/"
    ),
    "© Colaboradores de OpenStreetMap"
)

@Composable
fun OsmMapView(
    centerLocation: GeoPoint,
    userGpsLocation: GeoPoint?,
    radiusKm: Float,
    poiList: List<PoiItem>,
    selectedPoi: PoiItem?,
    onPoiSelected: (PoiItem?) -> Unit,
    onMapClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember(context) {
        Configuration.getInstance().apply {
            userAgentValue = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
            load(context, context.getSharedPreferences("osmdroid_prefs", 0))
        }

        MapView(context).apply {
            setTileSource(OPEN_STREET_MAP_TILE_SOURCE)
            setMultiTouchControls(true)
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)
            controller.setZoom(14.5)
            controller.setCenter(centerLocation)
            tileProvider.clearTileCache()
        }
    }

    // Lifecycle handling for osmdroid MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { map ->
            map.overlays.clear()

            // 1. Draw Radius Circle
            val circlePolygon = Polygon(map).apply {
                points = Polygon.pointsAsCircle(centerLocation, (radiusKm * 1000).toDouble())
                fillPaint.color = Color.argb(40, 33, 150, 243) // Semi-transparent blue
                outlinePaint.color = Color.rgb(33, 150, 243)
                outlinePaint.strokeWidth = 4f
                outlinePaint.style = Paint.Style.STROKE
            }
            map.overlays.add(circlePolygon)

            // 2. Center Location / Search Marker
            val centerMarker = Marker(map).apply {
                position = centerLocation
                title = "Centro de búsqueda"
                snippet = "Radio: ${radiusKm} km"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                infoWindow = BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map)
            }
            map.overlays.add(centerMarker)

            // 3. User GPS location marker if distinct
            userGpsLocation?.let { gpsLoc ->
                if (gpsLoc.distanceToAsDouble(centerLocation) > 20) {
                    val gpsMarker = Marker(map).apply {
                        position = gpsLoc
                        title = "Tu ubicación GPS actual"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(gpsMarker)
                }
            }

            // 4. POI Markers
            poiList.forEach { poi ->
                val poiMarker = Marker(map).apply {
                    position = poi.geoPoint
                    title = "${poi.category.iconEmoji} ${poi.name}"
                    snippet = "Categoría: ${poi.category.displayName}\nDistancia: ${poi.distanceKm} km"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    infoWindow = BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, map)
                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        onPoiSelected(poi)
                        true
                    }
                }
                map.overlays.add(poiMarker)

                // Highlight selected POI
                if (selectedPoi?.id == poi.id) {
                    poiMarker.showInfoWindow()
                }
            }

            // Center map smoothly when location or selected POI changes
            if (selectedPoi != null) {
                map.controller.animateTo(selectedPoi.geoPoint)
            }

            map.invalidate()
        }
    )
}
