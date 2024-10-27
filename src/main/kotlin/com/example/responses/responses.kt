package com.example.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val success: Boolean,
    val userId: Int
)

@Serializable
data class GetRecipesResponse(
    val success: Boolean,
    val recipes: List<RecipeWithUser>
)

@Serializable
data class RecipeResponse(
    val success: Boolean,
    val recipe: RecipeWithUser
)


@Serializable
data class RecipeWithUser(
    val id: Int,
    val name: String,
    val category: String,
    val ingredients: String,
    val text: String,
    val userId: Int,
    val userName: String
)