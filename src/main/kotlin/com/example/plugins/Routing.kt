package com.example.plugins

import com.example.database.LoginModel
import com.example.database.UserService
import com.example.database.UserSession
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.get
import io.ktor.server.sessions.set
import io.ktor.server.sessions.sessions
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(database: Database) {
    routing {
        route("/login") {
            post {
                val user = call.receive<LoginModel>()
                val username = user.login
                val validUser = UserService(database).find(username ?: "")
                if (validUser  != null) {
                    call.sessions.set(UserSession(validUser.login,validUser.password))
                    call.respondText("Login successful!", status = HttpStatusCode.OK)
                } else {
                    call.respondText("Invalid credentials.", status = HttpStatusCode.Unauthorized)
                }
            }
        }

        get("/protected") {
            val session = call.sessions.get<UserSession>()
            if (session != null) {
                call.respondText("Welcome, ${session.login}!", status = HttpStatusCode.OK)
            } else {
                call.respondText("You are not logged in.", status = HttpStatusCode.Unauthorized)
            }
        }

        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondText("Logged out successfully.", status = HttpStatusCode.OK)
        }

        get("/") {
             call.respondText("Hello from Ktor!", ContentType.Text.Plain)
        }
    }
}
