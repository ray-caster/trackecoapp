package com.trackeco.trackeco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.trackeco.trackeco.data.models.EnvironmentalStats
import com.trackeco.trackeco.data.models.User
import com.trackeco.trackeco.network.NetworkModule
import com.trackeco.trackeco.ui.components.BottomNavigationBar
import com.trackeco.trackeco.ui.components.TrackEcoMapView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    currentUser: User?
) {
    var stats by remember { mutableStateOf<EnvironmentalStats?>(null) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = NetworkModule.api.getEnvironmentalStats()
                val statsData = response["stats"] as? Map<*, *>
                statsData?.let {
                    stats = EnvironmentalStats(
                        total_users = (it["total_users"] as? Double)?.toInt() ?: 0,
                        total_disposals = (it["total_disposals"] as? Double)?.toInt() ?: 0,
                        waste_prevented_kg = (it["waste_prevented_kg"] as? Double)?.toFloat() ?: 0f,
                        co2_saved_kg = (it["co2_saved_kg"] as? Double)?.toFloat() ?: 0f,
                        trees_saved = (it["trees_saved"] as? Double)?.toInt() ?: 0,
                        active_today = (it["active_today"] as? Double)?.toInt() ?: 0
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TrackEco") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Stats Card
            item {
                currentUser?.let { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Welcome, ${user.username}!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem("Points", user.points.toString())
                                StatItem("Streak", "${user.streak} days")
                                StatItem("Tier", user.tier)
                            }
                        }
                    }
                }
            }
            
            // Map View replacing Community Impact and Quick Actions
            item {
                Text(
                    "Waste Hotspots & Events",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    TrackEcoMapView(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
            Text(text, color = Color.White)
        }
    }
}

@Composable
fun ImpactRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}