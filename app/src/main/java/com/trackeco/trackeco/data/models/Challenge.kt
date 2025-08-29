package com.trackeco.trackeco.data.models

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val xp: Int,
    val type: String,
    val target: Int,
    val waste_type: String,
    val progress: Int = 0,
    val completed: Boolean = false
)

data class CommunityEvent(
    val id: String,
    val title: String,
    val description: String,
    val start_date: String,
    val end_date: String,
    val participants: Int,
    val goal: Int,
    val reward_points: Int,
    val location: String,
    val progress: Float
)