package com.example

import com.example.database.UserSession
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import org.jetbrains.exposed.sql.Database


fun main() {

    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val database = Database.connect(
        "jdbc:postgresql://localhost:5432/cooking-database",
        user = "postgres",
        password = "Skausmas226454!!"
    )

    install(Sessions) {
        cookie<UserSession>("SESSION")
    }

    configureSerialization()
    configureAuthRouting(database = database)
    configureRecipes(database = database)

}

