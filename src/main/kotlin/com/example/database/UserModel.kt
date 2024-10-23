package com.example.database

import kotlinx.coroutines.Dispatchers
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
import org.mindrot.jbcrypt.BCrypt


@Serializable
data class ExposedUser(val login: String, val email: String, val password: String) {
}


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

    fun hashedPassword(password:String):String{
        return BCrypt.hashpw(password,BCrypt.gensalt())
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }


    suspend fun create(user: ExposedUser): Int = dbQuery {
        Users.insert {
            it[login] = user.login
            it[email] = user.email
            it[password] = user.password
        }[Users.id]
    }

    suspend fun read(id: Int): ExposedUser? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { ExposedUser(it[Users.login], it[Users.email], it[Users.password])}
                .singleOrNull()
        }
    }

    suspend fun find(login: String): ExposedUser? {
        return dbQuery {
            Users.select { Users.login eq login }
                .map { ExposedUser(it[Users.login], it[Users.email], it[Users.password])}
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[login] = user.login
                it[email] = user.email
                it[password] = user.password
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

}