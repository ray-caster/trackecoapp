package com.trackeco.trackeco

import android.Manifest
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.trackeco.trackeco.data.models.User
import com.trackeco.trackeco.ui.screens.*
import com.trackeco.trackeco.network.NetworkModule
import com.trackeco.trackeco.ui.theme.TrackEcoTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var currentLocation: Location? = null
    
    // User state - Fixed to prevent crashes during state transitions
    private var currentUser: User? = null
    private var isLoggedIn = false
    private var userId = ""
    private var username = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            requestLocationPermission()
            
            setContent {
                TrackEcoTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TrackEcoNavigation()
                    }
                }
            }
        } catch (e: Exception) {
            println("MainActivity onCreate error: ${e.message}")
            e.printStackTrace()
            // Prevent app crash by finishing activity gracefully
            finish()
        }
    }
    
    @Composable
    fun TrackEcoNavigation() {
        val navController = rememberNavController()
        
        // Use remember for state management to prevent crashes
        var isLoggedInState by remember { mutableStateOf(isLoggedIn) }
        var currentUserState by remember { mutableStateOf(currentUser) }
        var userIdState by remember { mutableStateOf(userId) }
        var usernameState by remember { mutableStateOf(username) }
        
        NavHost(
            navController = navController,
            startDestination = if (isLoggedInState) "home" else "login"
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    onLoginSuccess = { uid, uname ->
                        userIdState = uid
                        usernameState = uname
                        isLoggedInState = true
                        // Create user object
                        currentUserState = User(
                            user_id = uid,
                            username = uname,
                            points = 0,
                            xp = 0,
                            streak = 0,
                            tier = "STARTER",
                            eco_rank = "ECO BEGINNER",
                            total_disposals = 0,
                            badges = emptyList(),
                            impact_score = 0
                        )
                        // Update class variables
                        userId = uid
                        username = uname
                        isLoggedIn = true
                        currentUser = currentUserState
                    }
                )
            }
            
            composable("register") {
                RegisterScreen(
                    navController = navController,
                    onRegisterSuccess = { uid, uname ->
                        userIdState = uid
                        usernameState = uname
                        isLoggedInState = true
                        // Create user object
                        currentUserState = User(
                            user_id = uid,
                            username = uname,
                            points = 0,
                            xp = 0,
                            streak = 0,
                            tier = "STARTER",
                            eco_rank = "ECO BEGINNER",
                            total_disposals = 0,
                            badges = emptyList(),
                            impact_score = 0
                        )
                        // Update class variables
                        userId = uid
                        username = uname
                        isLoggedIn = true
                        currentUser = currentUserState
                    }
                )
            }
            
            composable("home") {
                HomeScreen(
                    navController = navController,
                    currentUser = currentUserState
                )
            }
            
            composable("dispose") {
                RecordScreen(
                    navController = navController,
                    userId = userIdState
                )
            }
            
            composable("dispose/{userId}?description={description}") { backStackEntry ->
                val userIdArg = backStackEntry.arguments?.getString("userId") ?: userIdState
                // val description = backStackEntry.arguments?.getString("description")
                DisposeScreen(
                    navController = navController,
                    userId = userIdArg
                )
            }
            
            composable("challenges") {
                ChallengesScreen(navController)
            }
            
            composable("leaderboard") {
                LeaderboardScreen(navController)
            }
            
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    currentUser = currentUserState,
                    onLogout = {
                        isLoggedInState = false
                        currentUserState = null
                        userIdState = ""
                        usernameState = ""
                        // Update class variables
                        isLoggedIn = false
                        currentUser = null
                        userId = ""
                        username = ""
                    }
                )
            }
            
            composable("centers") {
                RecyclingCentersScreen(
                    navController = navController,
                    currentLocation = currentLocation
                )
            }
            
            composable("events") {
                CommunityEventsScreen(navController)
            }
            
            composable("record") {
                RecordScreen(
                    navController = navController,
                    userId = userIdState
                )
            }
        }
    }
    
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            getCurrentLocation()
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
            }
        }
    }
}

