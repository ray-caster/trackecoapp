package com.trackeco.trackeco.data.models

data class ProductInfo(
    val name: String,
    val type: String,
    val recyclable: Boolean,
    val points: Int
)

data class BarcodeRequest(
    val barcode: String
)

data class BarcodeResponse(
    val success: Boolean,
    val barcode: String,
    val product: ProductInfo?
)

data class CarbonFootprint(
    val user_id: String,
    val waste_type: String,
    val weight_kg: Float,
    val co2_saved_kg: Float,
    val calculation_date: String
)