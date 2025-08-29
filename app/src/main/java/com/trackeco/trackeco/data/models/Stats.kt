package com.trackeco.trackeco.data.models

data class EnvironmentalStats(
    val total_users: Int,
    val total_disposals: Int,
    val waste_prevented_kg: Float,
    val co2_saved_kg: Float,
    val trees_saved: Int,
    val active_today: Int
)

data class RecyclingCenter(
    val id: String,
    val name: String,
    val address: String,
    val distance: Float,
    val types: List<String>,
    val hours: String,
    val lat: Double,
    val lng: Double
)

data class WeeklyReport(
    val user_id: String,
    val week_ending: String,
    val disposals_count: Int,
    val points_earned: Int,
    val co2_saved: Float,
    val rank_change: String,
    val streak_status: String,
    val top_waste_type: String,
    val comparison: String
)