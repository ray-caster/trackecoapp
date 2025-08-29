package com.trackeco.trackeco.data.models

data class WasteDisposalRequest(
    val user_id: String,
    val video_data: String? = null,
    val image_data: String? = null,
    val waste_type: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class DisposalResponse(
    val success: Boolean,
    val message: String,
    val disposal: DisposalDetails?,
    val user_stats: UserStats?
)

data class DisposalDetails(
    val id: String,
    val waste_type: String,
    val confidence: Float,
    val points_earned: Int,
    val xp_earned: Int,
    val reasoning: String
)

data class UserStats(
    val points: Int,
    val xp: Int,
    val streak: Int,
    val tier: String,
    val new_badges: List<String>
)