package com.trackeco.trackeco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.data.models.CommunityEvent
import com.trackeco.trackeco.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun CommunityEventsScreen(navController: NavController) {
    var events by remember { mutableStateOf<List<CommunityEvent>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = NetworkModule.api.getCommunityEvents()
                val eventsList = response["events"] as? List<Map<String, Any>>
                eventsList?.let { list ->
                    events = list.map { e ->
                        CommunityEvent(
                            id = e["id"] as? String ?: "",
                            title = e["title"] as? String ?: "",
                            description = e["description"] as? String ?: "",
                            start_date = e["start_date"] as? String ?: "",
                            end_date = e["end_date"] as? String ?: "",
                            participants = (e["participants"] as? Double)?.toInt() ?: 0,
                            goal = (e["goal"] as? Double)?.toInt() ?: 0,
                            reward_points = (e["reward_points"] as? Double)?.toInt() ?: 0,
                            location = e["location"] as? String ?: "",
                            progress = (e["progress"] as? Double)?.toFloat() ?: 0f
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
            "Community Events",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(events) { event ->
                EventCard(event)
            }
        }
    }
}

@Composable
fun EventCard(event: CommunityEvent) {
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
                    event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFF4CAF50)
                ) {
                    Text(
                        "+${event.reward_points} pts",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
            
            Text(
                event.description,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray
            )
            
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Text(
                    event.location,
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Group,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                Text(
                    "${event.participants}/${event.goal} participants",
                    modifier = Modifier.padding(start = 4.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            LinearProgressIndicator(
                progress = { event.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            Text(
                "${event.progress.toInt()}% complete",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}