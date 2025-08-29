package com.trackeco.trackeco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.api.RegisterRequest
import com.trackeco.trackeco.api.RetrofitClient
import com.trackeco.trackeco.network.AuthInterceptor
import com.trackeco.trackeco.network.NetworkModule
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    onRegisterSuccess: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logo
        Icon(
            Icons.Filled.Recycling,
            contentDescription = "TrackEco Logo",
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )
        
        Text(
            "Join TrackEco",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            "Start Your Environmental Journey",
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
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        when {
                            username.isBlank() -> {
                                Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
                            }
                            email.isBlank() -> {
                                Toast.makeText(context, "Please enter an email", Toast.LENGTH_SHORT).show()
                            }
                            password.isBlank() -> {
                                Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show()
                            }
                            password != confirmPassword -> {
                                Toast.makeText(context, "Passwords don't match", Toast.LENGTH_SHORT).show()
                            }
                            password.length < 6 -> {
                                Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                isLoading = true
                                scope.launch {
                                    var retryCount = 0
                                    val maxRetries = 3
                                    
                                    while (retryCount < maxRetries) {
                                        try {
                                            val request = mapOf(
                                                "username" to username,
                                                "email" to email,
                                                "password" to password
                                            )
                                            val response = NetworkModule.api.register(request)
                                            val success = response["success"] as? Boolean ?: false
                                            if (success) {
                                                val token = response["token"] as? String
                                                token?.let { AuthInterceptor.authToken = it }
                                                val user = response["user"] as? Map<String, Any>
                                                user?.let {
                                                    val userId = it["user_id"] as? String ?: ""
                                                    val username = it["username"] as? String ?: ""
                                                    onRegisterSuccess(userId, username)
                                                }
                                                navController.navigate("home")
                                                break // Success, exit retry loop
                                            } else {
                                                val message = response["message"] as? String ?: "Registration failed"
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                break // Don't retry on validation errors
                                            }
                                        } catch (e: java.net.SocketTimeoutException) {
                                            retryCount++
                                            if (retryCount < maxRetries) {
                                                Toast.makeText(context, "Connection timeout. Retrying... ($retryCount/$maxRetries)", Toast.LENGTH_SHORT).show()
                                                kotlinx.coroutines.delay(2000)
                                            } else {
                                                Toast.makeText(context, "Connection timeout. Please check your internet connection.", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: java.io.IOException) {
                                            retryCount++
                                            if (retryCount < maxRetries) {
                                                Toast.makeText(context, "Network error. Retrying... ($retryCount/$maxRetries)", Toast.LENGTH_SHORT).show()
                                                kotlinx.coroutines.delay(2000)
                                            } else {
                                                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: retrofit2.HttpException) {
                                            when (e.code()) {
                                                400 -> {
                                                    Toast.makeText(context, "Invalid registration data. Please check all fields.", Toast.LENGTH_LONG).show()
                                                    break // Don't retry on validation errors
                                                }
                                                409 -> {
                                                    Toast.makeText(context, "Email already exists. Please use a different email.", Toast.LENGTH_LONG).show()
                                                    break // Don't retry on conflict errors
                                                }
                                                else -> {
                                                    Toast.makeText(context, "Server error (${e.code()}). Please try again later.", Toast.LENGTH_LONG).show()
                                                    break // Don't retry on server errors
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Registration error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            break // Don't retry on other errors
                                        }
                                    }
                                    isLoading = false
                                }
                            }
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
                        Text("Create Account")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? Login")
                }
            }
        }
    }
}