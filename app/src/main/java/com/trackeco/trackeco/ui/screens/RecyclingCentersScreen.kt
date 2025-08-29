package com.trackeco.trackeco.ui.screens

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.data.models.RecyclingCenter
import com.trackeco.trackeco.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun RecyclingCentersScreen(
    navController: NavController,
    currentLocation: Location?
) {
    var centers by remember { mutableStateOf<List<RecyclingCenter>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(currentLocation) {
        scope.launch {
            try {
                val lat = currentLocation?.latitude ?: 40.7128
                val lng = currentLocation?.longitude ?: -74.0060
                
                val response = NetworkModule.api.getRecyclingCenters(lat, lng)
                val centersList = response["centers"] as? List<Map<String, Any>>
                centersList?.let { list ->
                    centers = list.map { c ->
                        RecyclingCenter(
                            id = c["id"] as? String ?: "",
                            name = c["name"] as? String ?: "",
                            address = c["address"] as? String ?: "",
                            distance = (c["distance"] as? Double)?.toFloat() ?: 0f,
                            types = (c["types"] as? List<String>) ?: emptyList(),
                            hours = c["hours"] as? String ?: "",
                            lat = (c["lat"] as? Double) ?: 0.0,
                            lng = (c["lng"] as? Double) ?: 0.0
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Nearby Recycling Centers",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        if (centers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Text(
                        "No recycling centers found",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(centers) { center ->
                    RecyclingCenterCard(center)
                }
            }
        }
    }
}

@Composable
fun RecyclingCenterCard(center: RecyclingCenter) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    center.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${center.distance} km",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                center.address,
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.Gray
            )
            
            Text(
                "Hours: ${center.hours}",
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                center.types.forEach { type ->
                    Surface(
                        modifier = Modifier.padding(end = 8.dp),
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            type,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}