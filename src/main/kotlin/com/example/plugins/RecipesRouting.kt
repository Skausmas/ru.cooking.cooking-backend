package com.example.plugins

import com.example.database.*
import com.example.responses.GetRecipesResponse
import com.example.responses.RecipeResponse
import com.example.responses.RecipeWithUser
import io.ktor.server.application.Application
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*


fun Application.configureRecipes(database: Database) {

    val recipeService = RecipesService(database)
    val userService = UserService(database)

    routing {


        get("recipes") {
            val recipes = recipeService.readAll()
            call.respond(GetRecipesResponse(
                success = true,
                recipes = recipes.map {
                    RecipeWithUser(
                        id = it.id,
                        name = it.name,
                        category = it.category,
                        ingredients = it.ingredients,
                        text = it.text,
                        userId = it.userId,
                        userName = userService.getUserName(it.userId) ?: "unknown"
                    )
                }
            ))
        }

        post("editRecipe") {
            val updateInfo = call.receive<UpdateRecipeInfo>()
            val recipe = recipeService.updateRecipe(updateInfo)
            if (recipe != null)
                call.respond(
                    RecipeResponse(
                        success = true,
                        recipe = RecipeWithUser(
                            id = recipe.id,
                            name = recipe.name,
                            category = recipe.category,
                            ingredients = recipe.ingredients,
                            text = recipe.text,
                            userId = recipe.userId,
                            userName = userService.getUserName(recipe.userId) ?: "unknown"
                        )
                    )
                )
            else
                call.respond(HttpStatusCode.NotFound)
        }

        post("createRecipe") {
            val createRecipeInfo = call.receive<AddRecipeInfo>()
            val recipe = recipeService.create(createRecipeInfo)
            if (recipe != null)
                call.respond(
                    RecipeResponse(
                        success = true,
                        recipe = RecipeWithUser(
                            id = recipe.id,
                            name = recipe.name,
                            category = recipe.category,
                            ingredients = recipe.ingredients,
                            text = recipe.text,
                            userId = recipe.userId,
                            userName = userService.getUserName(recipe.userId) ?: "unknown"
                        )
                    )
                )
            else
                call.respond(HttpStatusCode.NotFound)
        }



    }
}