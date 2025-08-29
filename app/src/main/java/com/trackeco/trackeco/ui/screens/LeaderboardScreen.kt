package com.trackeco.trackeco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.network.NetworkModule
import com.trackeco.trackeco.ui.components.BottomNavigationBar
import kotlinx.coroutines.launch

data class LeaderboardUser(
    val rank: Int,
    val username: String,
    val points: Int,
    val tier: String
)

@Composable
fun LeaderboardScreen(navController: NavController) {
    var leaderboardData by remember { mutableStateOf<List<LeaderboardUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = NetworkModule.api.getLeaderboard()
                val leaderboard = response["leaderboard"] as? List<Map<String, Any>>
                leaderboard?.let { list ->
                    leaderboardData = list.map { user ->
                        LeaderboardUser(
                            rank = (user["rank"] as? Double)?.toInt() ?: 0,
                            username = user["username"] as? String ?: "",
                            points = (user["points"] as? Double)?.toInt() ?: 0,
                            tier = user["tier"] as? String ?: "BEGINNER"
                        )
                    }
                }
            } catch (e: Exception) {
                // Keep empty list on error
            }
            isLoading = false
        }
    }
    
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        Text(
            "Global Leaderboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (leaderboardData.isNotEmpty()) {
                    items(leaderboardData) { user ->
                        LeaderboardCard(
                            rank = user.rank,
                            username = user.username,
                            points = user.points,
                            tier = user.tier
                        )
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "No users on leaderboard yet",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    username: String,
    points: Int,
    tier: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#$rank",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(50.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(username, fontWeight = FontWeight.Bold)
                Text("$points points • $tier", color = Color.Gray)
            }
            
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = when(rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    3 -> Color(0xFFCD7F32) // Bronze
                    else -> Color.Gray
                }
            )
        }
    }
}