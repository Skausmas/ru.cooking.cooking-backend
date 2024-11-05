package com.example.database

import com.example.database.UserService.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


@Serializable
data class Recipe(
    val id: Int,
    val name: String,
    val category: String,
    val ingredients: String,
    val text: String,
    val userId: Int,
)

@Serializable
data class AddRecipeInfo(
    val name: String,
    val category: String,
    val ingredients: String,
    val text: String,
    val userId: Int
)


@Serializable
data class UpdateRecipeInfo(
    val id: Int,
    val name: String,
    val category: String,
    val ingredients: String,
    val text: String,
)


class RecipesService(private val database: Database) {
    object Recipes : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val category = varchar("category", length = 50)
        val ingredients = varchar("ingredients", length = 50)
        val text = varchar("text", length = 50)
        val userId = integer("userId").references(Users.id, ReferenceOption.CASCADE)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Recipes)
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun readAll(): List<Recipe> {
        return dbQuery {
            Recipes.selectAll()
                .map {
                    Recipe(
                        id = it[Recipes.id],
                        name = it[Recipes.name],
                        category = it[Recipes.category],
                        ingredients = it[Recipes.ingredients],
                        text = it[Recipes.text],
                        userId = it[Recipes.userId]
                    )
                }
        }
    }

    suspend fun updateRecipe(updateRecipeInfo: UpdateRecipeInfo): Recipe? {
        dbQuery {
            Recipes.update({ Recipes.id eq updateRecipeInfo.id }) {
                it[name] = updateRecipeInfo.name
                it[category] = updateRecipeInfo.category
                it[ingredients] = updateRecipeInfo.ingredients
                it[text] = updateRecipeInfo.text
            }
        }
        return dbQuery {
            Recipes.select { Recipes.id eq updateRecipeInfo.id }
                .map {
                    Recipe(
                        id = it[Recipes.id],
                        name = it[Recipes.name],
                        category = it[Recipes.category],
                        ingredients = it[Recipes.ingredients],
                        text = it[Recipes.text],
                        userId = it[Recipes.userId]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun create(addRecipeInfo: AddRecipeInfo): Recipe? {
        val id = dbQuery {
            Recipes.insert {
                it[name] = addRecipeInfo.name
                it[category] = addRecipeInfo.category
                it[ingredients] = addRecipeInfo.ingredients
                it[text] = addRecipeInfo.text
                it[userId] = addRecipeInfo.userId
            }[Recipes.id]
        }
        return dbQuery {
            Recipes.select { Recipes.id eq id }
                .map {
                    Recipe(
                        id = it[Recipes.id],
                        name = it[Recipes.name],
                        category = it[Recipes.category],
                        ingredients = it[Recipes.ingredients],
                        text = it[Recipes.text],
                        userId = it[Recipes.userId]
                    )
                }
                .singleOrNull()
        }
    }


    suspend fun delete(id: Int): Boolean {
        dbQuery {
            Recipes.deleteWhere { Recipes.id.eq(id) }
        }
        return dbQuery { Recipes.select { Recipes.id eq id }.empty() }
    }

}