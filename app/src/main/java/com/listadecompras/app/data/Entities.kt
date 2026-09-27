package com.listadecompras.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("listId")],
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listId: Long,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "un",
    /** Preço unitário (por un, por kg, por L…). Nulo = sem preço informado. */
    val price: Double? = null,
    val category: String = Category.OUTROS.name,
    val note: String = "",
    val checked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val checkedAt: Long? = null,
)

// Extensões fora da entidade para o Room não tentar gravá-las como colunas.
fun Item.categoryEnum(): Category = Category.fromName(category)

/** Valor total da linha (quantidade × preço), ou nulo se não há preço. */
fun Item.lineTotal(): Double? = price?.let { it * quantity * Units.priceFactor(unit) }

/**
 * Memória de tudo que o usuário já adicionou: alimenta as sugestões ao digitar
 * e lembra categoria, unidade e último preço pago.
 */
@Entity(tableName = "item_history")
data class ItemHistory(
    /** Nome normalizado (minúsculo, sem acento) usado como chave. */
    @PrimaryKey val nameKey: String,
    val name: String,
    val category: String,
    val unit: String,
    val lastPrice: Double?,
    val useCount: Int,
    val lastUsed: Long,
)
