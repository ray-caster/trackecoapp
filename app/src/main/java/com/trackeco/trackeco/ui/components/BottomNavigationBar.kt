package com.trackeco.trackeco.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute == "dispose",
            onClick = { navController.navigate("dispose") },
            icon = { Icon(Icons.Filled.CameraAlt, contentDescription = "Dispose") },
            label = { Text("Dispose") }
        )
        NavigationBarItem(
            selected = currentRoute == "challenges",
            onClick = { navController.navigate("challenges") },
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Challenges") },
            label = { Text("Challenges") }
        )
        NavigationBarItem(
            selected = currentRoute == "leaderboard",
            onClick = { navController.navigate("leaderboard") },
            icon = { Icon(Icons.Filled.Leaderboard, contentDescription = "Leaderboard") },
            label = { Text("Leaders") }
        )
    }
}