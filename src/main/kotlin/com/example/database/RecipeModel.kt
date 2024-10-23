package com.example.database
import com.example.database.RecipesService.Recipes.category
import com.example.database.RecipesService.Recipes.ingredients
import com.example.database.RecipesService.Recipes.name
import com.example.database.RecipesService.Recipes.userId
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
data class ExposedRecipe(val name: String, val category: String, val ingredients: String, val text: String, val userId: Int) {
}
@Serializable
data class UpdateRecipe(val name: String, val category: String, val ingredients: String, val text: String)

class RecipesService(private val database: Database) {
    object Recipes : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val category = varchar("category", length = 50)
        val ingredients = varchar("ingredients", length = 50)
        val text = varchar("text", length = 50)
        val userId = integer("userId" ).references(Users.id, ReferenceOption.CASCADE)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Recipes)
        }
    }
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }


    suspend fun create(recipe: ExposedRecipe): Int = dbQuery {
        Recipes.insert {
            it[name] = recipe.name
            it[category] = recipe.category
            it[ingredients] = recipe.ingredients
            it[text] = recipe.text
            it[userId] = recipe.userId
        }[Recipes.id]
    }

    suspend fun read(id: Int): ExposedRecipe? {
        return dbQuery {
            Recipes.select { Recipes.id eq id }
                .map { ExposedRecipe(it[Recipes.name], it[Recipes.category], it[Recipes.ingredients], it[Recipes.text], it[Recipes.userId])}
                .singleOrNull()
        }
    }
    suspend fun readAll(): List<ExposedRecipe>? {
        return dbQuery {
            Recipes.selectAll()
        }       .map { ExposedRecipe(
            it[Recipes.name],
            it[Recipes.category],
            it[Recipes.ingredients],
            it[Recipes.text],
            it[Recipes.userId]) }
    }


    suspend fun update(id: Int, recipe: UpdateRecipe) {
        dbQuery {
            Recipes.update({ Recipes.id eq id }) {
                it[name] = recipe.name
                it[category] = recipe.category
                it[ingredients] = recipe.ingredients
                it[text] = recipe.text
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Recipes.deleteWhere { Recipes.id.eq(id) }
        }
    }
}