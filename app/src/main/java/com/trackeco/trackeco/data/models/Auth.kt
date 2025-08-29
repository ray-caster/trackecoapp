package com.trackeco.trackeco.data.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: User?
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)