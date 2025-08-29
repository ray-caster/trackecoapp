package com.trackeco.trackeco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.data.models.LoginRequest
import com.trackeco.trackeco.network.AuthInterceptor
import com.trackeco.trackeco.network.NetworkModule
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("test@trackeco.app") }
    var password by remember { mutableStateOf("test123") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Icon(
            Icons.Filled.Recycling,
            contentDescription = "TrackEco Logo",
            modifier = Modifier.size(100.dp),
            tint = Color.White
        )
        
        Text(
            "TrackEco",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            "Save the Planet, One Disposal at a Time",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            var retryCount = 0
                            val maxRetries = 3
                            
                            while (retryCount < maxRetries) {
                                try {
                                    val request = mapOf(
                                        "email" to email,
                                        "password" to password
                                    )
                                    val response = NetworkModule.api.login(request)
                                    val success = response["success"] as? Boolean ?: false
                                    if (success) {
                                        val token = response["token"] as? String
                                        token?.let { AuthInterceptor.authToken = it }
                                        val user = response["user"] as? Map<String, Any>
                                        user?.let {
                                            val userId = it["user_id"] as? String ?: ""
                                            val username = it["username"] as? String ?: ""
                                            onLoginSuccess(userId, username)
                                        }
                                        navController.navigate("home")
                                        break // Success, exit retry loop
                                    } else {
                                        val message = response["message"] as? String ?: "Login failed"
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        break // Don't retry on authentication failure
                                    }
                                } catch (e: retrofit2.HttpException) {
                                    // NEVER retry on HTTP errors - these are server responses, not connection issues
                                    when (e.code()) {
                                        401 -> {
                                            Toast.makeText(context, "Invalid email or password. Please try again.", Toast.LENGTH_LONG).show()
                                        }
                                        400 -> {
                                            Toast.makeText(context, "Invalid input format. Please check your data.", Toast.LENGTH_LONG).show()
                                        }
                                        else -> {
                                            Toast.makeText(context, "Server error (${e.code()}). Please try again later.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    break // NEVER retry on HTTP errors
                                } catch (e: java.net.SocketTimeoutException) {
                                    retryCount++
                                    if (retryCount < maxRetries) {
                                        Toast.makeText(context, "Connection timeout. Retrying... ($retryCount/$maxRetries)", Toast.LENGTH_SHORT).show()
                                        kotlinx.coroutines.delay(2000) // Wait 2 seconds before retry
                                    } else {
                                        Toast.makeText(context, "Connection timeout. Please check your internet connection.", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: java.io.IOException) {
                                    // Only retry on actual connection/stream issues 
                                    if (e.message?.contains("unexpected end of stream") == true || 
                                        e.message?.contains("Connection reset") == true ||
                                        e.message?.contains("Network is unreachable") == true) {
                                        retryCount++
                                        if (retryCount < maxRetries) {
                                            Toast.makeText(context, "Connection interrupted. Retrying... ($retryCount/$maxRetries)", Toast.LENGTH_SHORT).show()
                                            kotlinx.coroutines.delay(2000)
                                        } else {
                                            Toast.makeText(context, "Connection failed. Please check your internet connection.", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        // Other IO errors - don't retry
                                        Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                                        break
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    break // Don't retry on other errors
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Login")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? Register")
                }
            }
        }
    }
}