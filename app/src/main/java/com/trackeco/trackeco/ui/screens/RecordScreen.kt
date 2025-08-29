package com.trackeco.trackeco.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.FallbackStrategy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.trackeco.trackeco.network.NetworkModule
import com.trackeco.trackeco.ui.components.BottomNavigationBar
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var activeRecording: Recording? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    // Check permissions on start
    LaunchedEffect(Unit) {
        val cameraGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val audioGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        hasCameraPermission = cameraGranted
        hasAudioPermission = audioGranted

        // Debug log
        println("RecordScreen: Camera: $cameraGranted, Audio: $audioGranted")

        if (!cameraGranted) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if (!audioGranted) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Upload function
    suspend fun uploadVideoToBackend(context: Context, videoUri: Uri, userId: String) {
        var retryCount = 0
        val maxRetries = 3
        
        try {
            isUploading = true

            // Get file from URI
            val inputStream = context.contentResolver.openInputStream(videoUri)
            val file = File.createTempFile("video", ".mp4", context.cacheDir)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            while (retryCount < maxRetries) {
                try {
                    // Create multipart request
                    val requestFile = file.asRequestBody("video/mp4".toMediaTypeOrNull())
                    val videoPart = MultipartBody.Part.createFormData("video", file.name, requestFile)
                    
                    // Create request body for user ID
                    val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                    // Upload video with proper endpoint
                    val response = NetworkModule.api.uploadVideo(
                        userId = userIdBody,
                        latitude = null,
                        longitude = null,
                        video = videoPart
                    )

                    if (response["success"] == true) {
                        val pointsEarned = response["points_earned"] ?: 0
                        val objectType = response["object_type"] ?: "unknown"
                        Toast.makeText(
                            context, 
                            "Video analyzed! +$pointsEarned points for $objectType", 
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate("home")
                        break // Success
                    } else {
                        val message = response["message"] as? String ?: "Video analysis failed"
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        break // Don't retry on analysis failure
                    }

                } catch (e: retrofit2.HttpException) {
                    // NEVER retry on HTTP errors
                    when (e.code()) {
                        400 -> Toast.makeText(context, "Invalid video format or missing data", Toast.LENGTH_LONG).show()
                        401 -> Toast.makeText(context, "Authentication failed. Please log in again.", Toast.LENGTH_LONG).show()
                        413 -> Toast.makeText(context, "Video file too large. Please use a smaller video.", Toast.LENGTH_LONG).show()
                        else -> Toast.makeText(context, "Upload failed with error ${e.code()}", Toast.LENGTH_LONG).show()
                    }
                    break // NEVER retry HTTP errors
                } catch (e: java.net.SocketTimeoutException) {
                    retryCount++
                    if (retryCount < maxRetries) {
                        Toast.makeText(context, "Upload timeout. Retrying... ($retryCount/$maxRetries)", Toast.LENGTH_SHORT).show()
                        kotlinx.coroutines.delay(3000) // Longer delay for video uploads
                    } else {
                        Toast.makeText(context, "Upload timeout. Please check your internet connection.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: java.io.IOException) {
                    // Only retry on actual connection/stream issues 
                    if (e.message?.contains("unexpected end of stream") == true || 
                        e.message?.contains("Connection reset") == true ||
                        e.message?.contains("Network is unreachable") == true) {
                        retryCount++
                        if (retryCount < maxRetries) {
                            Toast.makeText(context, "Connection interrupted. Retrying upload... ($retryCount/$maxRetries)", Toast.LENGTH_SHORT).show()
                            kotlinx.coroutines.delay(3000)
                        } else {
                            Toast.makeText(context, "Upload failed. Please check your internet connection.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Upload error: ${e.message}", Toast.LENGTH_LONG).show()
                        break
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    break
                }
            }
            
            // Clean up temp file
            file.delete()

        } finally {
            isUploading = false
        }
    }
    
    // Video recording functions
    fun startVideoRecording() {
        val capture = videoCapture ?: return

        // Create video file
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "trackeco_action_${System.currentTimeMillis()}")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TrackEco")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        // Start recording
        activeRecording = capture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == 
                    PermissionChecker.PERMISSION_GRANTED) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        isRecording = true
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            // Upload video to backend
                            scope.launch {
                                uploadVideoToBackend(context, recordEvent.outputResults.outputUri, userId)
                            }
                        } else {
                            Toast.makeText(context, "Video recording failed", Toast.LENGTH_SHORT).show()
                        }
                        isRecording = false
                        activeRecording = null
                    }
                }
            }
    }

    fun stopVideoRecording() {
        activeRecording?.stop()
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                // Camera Preview
                VideoRecordingPreview(
                    modifier = Modifier.fillMaxSize(),
                    lifecycleOwner = lifecycleOwner,
                    onVideoCaptureReady = { capture ->
                        videoCapture = capture
                    }
                )

                // Top overlay with instructions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = if (isRecording) "Recording environmental action..." else "Record your environmental action",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Recording controls
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 120.dp) // Leave space for bottom nav
                ) {
                    if (isUploading) {
                        Card(
                            modifier = Modifier.padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.8f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Analyzing video with AI...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                if (isRecording) {
                                    stopVideoRecording()
                                } else {
                                    startVideoRecording()
                                }
                            },
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape),
                            containerColor = if (isRecording) Color.Red else Color(0xFF4CAF50)
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            } else {
                // Permission denied state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Camera Access Required",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "To record environmental actions",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Permissions: Camera=${hasCameraPermission}, Audio=${hasAudioPermission}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (!hasCameraPermission) {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            if (!hasAudioPermission) {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Grant Permissions")
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoRecordingPreview(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner,
    onVideoCaptureReady: (VideoCapture<Recorder>) -> Unit
) {
    var isCameraReady by remember { mutableStateOf(false) }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                // Set implementation mode to performance
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Build preview use case
                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build()
                    
                    // Select back camera
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    // Set up video capture with proper quality settings
                    val qualitySelector = QualitySelector.fromOrderedList(
                        listOf(Quality.HD, Quality.FHD, Quality.SD),
                        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                    )
                    
                    val recorder = Recorder.Builder()
                        .setExecutor(executor)
                        .setQualitySelector(qualitySelector)
                        .build()
                        
                    val videoCapture = VideoCapture.withOutput(recorder)

                    // Unbind all use cases before rebinding
                    cameraProvider.unbindAll()
                    
                    // Bind preview to surface provider
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    
                    // Bind use cases to camera
                    try {
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            videoCapture
                        )
                        
                        isCameraReady = true
                        onVideoCaptureReady(videoCapture)
                        
                        println("RecordScreen: Camera initialized successfully")
                    } catch (e: Exception) {
                        println("RecordScreen: Failed to bind camera: ${e.message}")
                    }
                } catch (e: Exception) {
                    println("RecordScreen: Camera provider error: ${e.message}")
                    e.printStackTrace()
                }
            }, executor)

            previewView
        },
        modifier = modifier,
        update = { view ->
            // Force layout update when camera is ready
            if (isCameraReady) {
                view.requestLayout()
            }
        }
    )
}