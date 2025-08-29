package com.trackeco.trackeco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.data.models.Challenge
import com.trackeco.trackeco.network.NetworkModule
import com.trackeco.trackeco.ui.components.BottomNavigationBar
import kotlinx.coroutines.launch

@Composable
fun ChallengesScreen(navController: NavController) {
    var challenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Get user ID from shared preferences
                val sharedPref = navController.context.getSharedPreferences("TrackEcoPrefs", android.content.Context.MODE_PRIVATE)
                val userId = sharedPref.getString("user_id", null)
                
                val response = if (userId != null) {
                    NetworkModule.api.getChallenges(userId)
                } else {
                    NetworkModule.api.getDailyChallenges()
                }
                val challengesList = response["challenges"] as? List<Map<String, Any>>
                challengesList?.let { list ->
                    challenges = list.map { c ->
                        Challenge(
                            id = c["id"] as? String ?: "",
                            title = c["title"] as? String ?: "",
                            description = c["description"] as? String ?: "",
                            points = (c["points"] as? Double)?.toInt() ?: 0,
                            xp = (c["xp"] as? Double)?.toInt() ?: 0,
                            type = c["type"] as? String ?: "",
                            target = (c["target"] as? Double)?.toInt() ?: 0,
                            waste_type = c["waste_type"] as? String ?: "",
                            progress = (c["progress"] as? Double)?.toInt() ?: 0,
                            completed = c["completed"] as? Boolean ?: false
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
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
                "Daily Challenges",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(challenges) { challenge ->
                    ChallengeCard(challenge)
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge) {
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
                    challenge.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFFD700)
                    )
                    Text(
                        "${challenge.points} pts",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            
            Text(
                challenge.description,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray
            )
            
            LinearProgressIndicator(
                progress = { if (challenge.target > 0) challenge.progress.toFloat() / challenge.target.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Progress: ${challenge.progress}/${challenge.target}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (challenge.completed) {
                    Text(
                        "✓ Completed",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}