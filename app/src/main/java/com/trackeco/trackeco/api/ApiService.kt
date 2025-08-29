package com.trackeco.trackeco.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// --- DATA CLASSES ---

// For sending login/registration data
data class AuthRequest(val email: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(
    val success: Boolean, 
    val message: String, 
    val token: String, 
    val user: RegisterUser
)
data class RegisterUser(
    val user_id: String,
    val username: String,
    val points: Int,
    val tier: String
)

// For the main user data object
data class DailyChallenge(val description: String, val progress: Int, val goal: Int, val reward: Int)
data class UserData(
    val user_id: String,
    val xp: Int,
    val points: Int,
    val streak: Int,
    val eco_rank: String,
    val has_completed_first_disposal: Boolean,
    val daily_challenge: DailyChallenge
)

// For sending action description and location data
data class ActionRequest(
    val user_id: String,
    val description: String,
    val latitude: Double?,
    val longitude: Double?
)

// For the response after recording an action
data class ActionResult(
    val success: Boolean,
    val points_earned: Int,
    val xp_earned: Int,
    val object_type: String?,
    val message: String,
    val ai_reasoning: String?,
    val new_total_points: Int?,
    val new_total_xp: Int?,
    val new_streak: Int?,
    val eco_rank: String?
)

// --- API INTERFACE ---

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): UserData

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("api/user/{userId}")
    suspend fun getUserData(@Path("userId") userId: String): UserData

    @POST("api/waste/dispose")
    suspend fun submitAction(@Body request: ActionRequest): ActionResult
}

// --- RETROFIT CLIENT ---

object RetrofitClient {
    private const val BASE_URL = "http://157.66.55.198:5000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}