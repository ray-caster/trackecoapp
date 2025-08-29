package com.trackeco.trackeco.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.trackeco.trackeco.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun DisposeScreen(
    navController: NavController,
    userId: String
) {
    var actionDescription by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Record Environmental Action",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Action Description Input
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Describe Your Environmental Action",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = actionDescription,
                    onValueChange = { actionDescription = it },
                    label = { Text("What did you do?") },
                    placeholder = { Text("e.g., Recycled plastic bottle, picked up litter, composted food scraps") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit Button
        Button(
            onClick = {
                if (actionDescription.isBlank()) {
                    Toast.makeText(context, "Please describe your action", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                
                isProcessing = true
                scope.launch {
                    try {
                        val request = mapOf(
                            "user_id" to userId,
                            "description" to actionDescription,
                            "latitude" to 40.7128, // Mock location
                            "longitude" to -74.0060
                        )
                        val response = NetworkModule.api.submitAction(request)
                        if (response["success"] == true) {
                            val pointsEarned = response["points_earned"] ?: 0
                            Toast.makeText(
                                context, 
                                "Action recorded! +$pointsEarned points", 
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("home")
                        } else {
                            val message = response["message"] as? String ?: "Action not approved"
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing && actionDescription.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Evaluating...")
            } else {
                Text("Submit Action")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "💡 Tips for Valid Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("• Be specific about what you recycled/disposed")
                Text("• Mention proper disposal methods (recycling bin, compost, etc.)")
                Text("• Include location context if relevant")
                Text("• AI will evaluate environmental impact")
                Text("• Invalid actions will be rejected")
            }
        }
    }
}