package com.trackeco.trackeco.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface TrackEcoApi {
    // Authentication
    @POST("api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Map<String, Any>
    
    @POST("api/auth/register")
    suspend fun register(@Body request: Map<String, String>): Map<String, Any>
    
    // Environmental Action Recording
    @POST("api/waste/dispose")
    suspend fun submitAction(@Body request: Map<String, Any>): Map<String, Any>
    
    @Multipart
    @POST("api/video/upload")
    suspend fun uploadVideo(
        @Part("user_id") userId: RequestBody,
        @Part("latitude") latitude: RequestBody?,
        @Part("longitude") longitude: RequestBody?,
        @Part video: MultipartBody.Part
    ): Map<String, Any>
    
    @GET("api/waste/history/{userId}")
    suspend fun getWasteHistory(@Path("userId") userId: String): Map<String, Any>
    
    // User Profile
    @GET("api/user/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): Map<String, Any>
    
    @POST("api/user/update-profile")
    suspend fun updateProfile(@Body request: Map<String, Any>): Map<String, Any>
    
    // Challenges & Events
    @GET("api/challenges/daily")
    suspend fun getDailyChallenges(): Map<String, Any>
    
    @GET("api/challenges")
    suspend fun getChallenges(@Query("user_id") userId: String): Map<String, Any>
    
    @GET("api/community/events")
    suspend fun getCommunityEvents(): Map<String, Any>
    
    @POST("api/challenges/complete")
    suspend fun completeChallenge(@Body request: Map<String, String>): Map<String, Any>
    
    // Stats & Leaderboard
    @GET("api/stats/environmental")
    suspend fun getEnvironmentalStats(): Map<String, Any>
    
    @GET("api/leaderboard")
    suspend fun getLeaderboard(): Map<String, Any>
    
    // Recycling Centers
    @GET("api/recycling-centers")
    suspend fun getRecyclingCenters(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Map<String, Any>
    
    // Educational Content
    @GET("api/education/guides")
    suspend fun getEducationalGuides(): Map<String, Any>
    
    // Reports
    @GET("api/report/weekly")
    suspend fun getWeeklyReport(@Query("user_id") userId: String): Map<String, Any>
    
    // Carbon Calculation
    @POST("api/carbon/calculate")
    suspend fun calculateCarbon(@Body request: Map<String, String>): Map<String, Any>
    
    // Sync
    @POST("api/sync/upload")
    suspend fun syncUpload(@Body request: Map<String, Any>): Map<String, Any>
    
    @GET("api/sync/download/{userId}")
    suspend fun syncDownload(@Path("userId") userId: String): Map<String, Any>
    
    // Health Check
    @GET("api/health")
    suspend fun healthCheck(): Map<String, Any>
    
    // Map Hotspots
    @GET("api/map/hotspots")
    suspend fun getMapHotspots(): Map<String, Any>
}