package com.trackeco.trackeco.data.models

data class User(
    val user_id: String,
    val username: String,
    val points: Int,
    val xp: Int,
    val streak: Int,
    val tier: String,
    val eco_rank: String,
    val total_disposals: Int,
    val badges: List<String>,
    val impact_score: Int
)