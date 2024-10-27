package com.example.database

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


@Serializable
data class RegInfo(
    @SerialName("login") val login: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginInfo(
    val login: String,
    val password: String
)



class UserService(private val database: Database) {
    object Users : Table() {

        val id = integer("id").autoIncrement()
        val login = varchar("login", length = 50)
        val email = varchar("email", length = 50)
        val password = varchar("password", length = 250)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }


    suspend fun register(regInfo: RegInfo): Int = dbQuery {
        Users.insert {
            it[login] = regInfo.login
            it[email] = regInfo.email
            it[password] = regInfo.password
        }[Users.id]
    }

    suspend fun login(loginInfo: LoginInfo): Int {
        val usersList = dbQuery {
            Users.select {
                Users.login eq loginInfo.login
                Users.password eq loginInfo.password
            }.map {
                it[Users.id]
            }
        }
        return if (usersList.isEmpty())
            -1
        else
            usersList.single()
    }


    suspend fun getUserName(userId: Int):String?{
        val userList = dbQuery {
            Users.select{
                Users.id eq userId
            }
                .map {
                    it[Users.login]
                }
        }
        return userList.singleOrNull()
    }



    suspend fun delete(id: Int): Boolean {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
        return dbQuery { Users.select{ Users.id eq id }.empty() }
    }

}