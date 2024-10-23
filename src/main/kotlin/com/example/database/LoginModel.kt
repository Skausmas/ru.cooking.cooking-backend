package com.example.database

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class LoginModel(
    val login: String,
    val password: String
)

@Suppress("DEPRECATION")
@Serializable
data class UserSession(
    val id : String,
    val login : String
) : Principal