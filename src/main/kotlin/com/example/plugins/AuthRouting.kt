package com.example.plugins

import com.example.database.LoginInfo
import com.example.database.RegInfo
import com.example.database.UserService
import com.example.responses.AuthResponse
import io.ktor.server.application.Application
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*


fun Application.configureAuthRouting(database: Database) {

    val userService = UserService(database)

    routing {

        post("/register") {
            val user = call.receive<RegInfo>()
            val id = userService.register(user)
            call.respond(
                AuthResponse(
                    success = true,
                    userId = id
                )
            )
        }


        get("/login") {
            val user = call.receive<LoginInfo>()
            val id = userService.login(user)
            if (id == -1) {
                call.respond(HttpStatusCode.NotFound)
            }
            else {
                call.respond(AuthResponse(
                    success = true,
                    userId = id
                ))
            }
        }


//        // Delete user
//        delete("deleteUser") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            userService.delete(id)
//            call.respond(HttpStatusCode.OK)
//        }
    }
}