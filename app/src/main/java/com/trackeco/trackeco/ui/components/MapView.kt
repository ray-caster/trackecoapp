package com.trackeco.trackeco.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.trackeco.trackeco.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HotspotData(
    val lat: Double,
    val lng: Double,
    val radius: Float,
    val intensity: Float,
    val activityCount: Int,
    val type: String
)

data class EventData(
    val lat: Double,
    val lng: Double,
    val radius: Float,
    val intensity: Float,
    val title: String,
    val participants: Int,
    val type: String
)

@Composable
fun TrackEcoMapView(
    modifier: Modifier = Modifier,
    onMapReady: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var hotspots by remember { mutableStateOf<List<HotspotData>>(emptyList()) }
    var events by remember { mutableStateOf<List<EventData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // NYC coordinates as default center
    val defaultLocation = LatLng(40.7128, -74.0060)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }
    
    // Fetch hotspot data
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = NetworkModule.api.getMapHotspots()
                    
                    val hotspotsList = (response["hotspots"] as? List<*>)?.mapNotNull { item ->
                        val hotspot = item as? Map<*, *>
                        hotspot?.let {
                            HotspotData(
                                lat = (it["lat"] as? Double) ?: 0.0,
                                lng = (it["lng"] as? Double) ?: 0.0,
                                radius = ((it["radius"] as? Double) ?: 50.0).toFloat(),
                                intensity = ((it["intensity"] as? Double) ?: 0.5).toFloat(),
                                activityCount = ((it["activity_count"] as? Double) ?: 1.0).toInt(),
                                type = (it["type"] as? String) ?: "waste_hotspot"
                            )
                        }
                    } ?: emptyList()
                    
                    val eventsList = (response["events"] as? List<*>)?.mapNotNull { item ->
                        val event = item as? Map<*, *>
                        event?.let {
                            EventData(
                                lat = (it["lat"] as? Double) ?: 0.0,
                                lng = (it["lng"] as? Double) ?: 0.0,
                                radius = ((it["radius"] as? Double) ?: 100.0).toFloat(),
                                intensity = ((it["intensity"] as? Double) ?: 0.8).toFloat(),
                                title = (it["title"] as? String) ?: "Event",
                                participants = ((it["participants"] as? Double) ?: 0.0).toInt(),
                                type = (it["type"] as? String) ?: "event"
                            )
                        }
                    } ?: emptyList()
                    
                    hotspots = hotspotsList
                    events = eventsList
                    isLoading = false
                }
            } catch (e: Exception) {
                // Use demo data if API fails
                hotspots = listOf(
                    HotspotData(40.7128, -74.0060, 80f, 0.8f, 15, "waste_hotspot"),
                    HotspotData(40.7580, -73.9855, 60f, 0.6f, 8, "waste_hotspot"),
                    HotspotData(40.7489, -73.9680, 100f, 1.0f, 25, "waste_hotspot")
                )
                events = listOf(
                    EventData(40.7614, -73.9776, 150f, 0.8f, "Ocean Cleanup Initiative", 3456, "event")
                )
                isLoading = false
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,  // Shows buildings, roads, and terrain
                isMyLocationEnabled = false,
                isBuildingEnabled = true,  // Enable 3D buildings
                isIndoorEnabled = true     // Enable indoor maps where available
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                compassEnabled = true,
                myLocationButtonEnabled = false
            )
        ) {
            // Add pulsating red circles for waste hotspots
            hotspots.forEach { hotspot ->
                PulsatingCircle(
                    center = LatLng(hotspot.lat, hotspot.lng),
                    radius = hotspot.radius.toDouble(),
                    fillColor = Color.Red.copy(alpha = 0.3f * hotspot.intensity),
                    strokeColor = Color.Red,
                    strokeWidth = 2f
                )
                
                // Add marker for high activity hotspots
                if (hotspot.activityCount > 10) {
                    Marker(
                        state = MarkerState(position = LatLng(hotspot.lat, hotspot.lng)),
                        title = "High Activity Zone",
                        snippet = "${hotspot.activityCount} disposals"
                    )
                }
            }
            
            // Add pulsating golden circles for events
            events.forEach { event ->
                PulsatingCircle(
                    center = LatLng(event.lat, event.lng),
                    radius = event.radius.toDouble(),
                    fillColor = Color(0xFFFFD700).copy(alpha = 0.4f * event.intensity),
                    strokeColor = Color(0xFFFFD700),
                    strokeWidth = 3f
                )
                
                // Add marker for events
                Marker(
                    state = MarkerState(position = LatLng(event.lat, event.lng)),
                    title = event.title,
                    snippet = "${event.participants} participants"
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF4CAF50)
            )
        }
        
        // Map legend
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "Legend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red.copy(alpha = 0.6f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Waste Hotspot", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFFFFD700).copy(alpha = 0.6f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Community Event", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PulsatingCircle(
    center: LatLng,
    radius: Double,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float
) {
    // Animation for pulsating effect
    val infiniteTransition = rememberInfiniteTransition()
    val animatedRadius by infiniteTransition.animateFloat(
        initialValue = radius.toFloat() * 0.9f,
        targetValue = radius.toFloat() * 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Circle(
        center = center,
        radius = animatedRadius.toDouble(),
        fillColor = fillColor.copy(alpha = animatedAlpha * fillColor.alpha),
        strokeColor = strokeColor,
        strokeWidth = strokeWidth
    )
}