package com.example.plugins

import com.example.database.ExposedRecipe
import com.example.database.ExposedUser
import com.example.database.RecipesService
import com.example.database.UpdateRecipe
import io.ktor.server.application.Application
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*


fun Application.configureRecipes(database: Database) {

    val recipeService = RecipesService(database)

    routing {

    post("/recipe") {
        val recipe = call.receive<ExposedRecipe>()
        val id = recipeService.create(recipe)
        call.respond(HttpStatusCode.Created, id)
    }
        get("/recipe") {
            val recipe = recipeService.readAll()
            if (recipe != null) {
                call.respond(HttpStatusCode.OK, recipe)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/recipe/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val recipe = recipeService.read(id)
            if (recipe != null) {
                call.respond(HttpStatusCode.OK, recipe)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/recipe/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val recipe = call.receive<UpdateRecipe>()
            recipeService.update(id, recipe)
            call.respond(HttpStatusCode.OK)
        }

        delete("/recipe/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            recipeService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
}
}