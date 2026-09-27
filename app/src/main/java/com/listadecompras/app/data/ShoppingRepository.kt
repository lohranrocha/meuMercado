package com.listadecompras.app.data

import androidx.room.withTransaction
import com.listadecompras.app.util.QuickAdd
import com.listadecompras.app.util.Text
import kotlinx.coroutines.flow.Flow

/** Uma sugestão exibida enquanto o usuário digita. */
data class Suggestion(
    val name: String,
    val category: Category,
    val unit: String,
    val lastPrice: Double?,
    val fromHistory: Boolean,
)

class ShoppingRepository(private val db: AppDatabase) {
    private val dao = db.dao()

    // ---------- Leitura ----------

    fun observeLists(): Flow<List<ShoppingList>> = dao.observeLists()
    fun observeAllItems(): Flow<List<Item>> = dao.observeAllItems()
    fun observeList(id: Long): Flow<ShoppingList?> = dao.observeList(id)
    fun observeItems(listId: Long): Flow<List<Item>> = dao.observeItems(listId)
    fun observeHistory(): Flow<List<ItemHistory>> = dao.observeHistory()

    // ---------- Listas ----------

    suspend fun createList(name: String): Long =
        dao.insertList(ShoppingList(name = Text.capitalizeFirst(name)))

    suspend fun renameList(id: Long, name: String) =
        dao.renameList(id, Text.capitalizeFirst(name), now())

    suspend fun deleteList(id: Long) = db.withTransaction {
        dao.deleteItemsOfList(id)
        dao.deleteList(id)
    }

    /** Cria uma cópia da lista com todos os itens desmarcados. */
    suspend fun duplicateList(id: Long): Long = db.withTransaction {
        val original = dao.getList(id) ?: return@withTransaction -1L
        val newId = dao.insertList(ShoppingList(name = "${original.name} (cópia)"))
        val base = now()
        val copies = dao.getItems(id).mapIndexed { index, item ->
            item.copy(
                id = 0,
                listId = newId,
                checked = false,
                checkedAt = null,
                createdAt = base + index,
            )
        }
        dao.insertItems(copies)
        newId
    }

    /** Na primeira abertura do app cria uma lista de exemplo. */
    suspend fun seedIfEmpty() {
        if (dao.countLists() > 0) return
        val id = dao.insertList(ShoppingList(name = "Mercado"))
        val base = now()
        listOf(
            Item(listId = id, name = "Leite", quantity = 2.0, category = Category.FRIOS_LATICINIOS.name, createdAt = base),
            Item(listId = id, name = "Pão francês", quantity = 6.0, category = Category.PADARIA.name, createdAt = base + 1),
            Item(listId = id, name = "Banana", quantity = 1.0, unit = "dz", category = Category.HORTIFRUTI.name, createdAt = base + 2),
            Item(listId = id, name = "Café", quantity = 1.0, price = 18.90, category = Category.MATINAIS.name, createdAt = base + 3),
            Item(listId = id, name = "Detergente", quantity = 2.0, price = 2.49, category = Category.LIMPEZA.name, createdAt = base + 4),
        ).forEach { dao.insertItem(it) }
    }

    // ---------- Itens ----------

    /**
     * Adiciona um item a partir do texto digitado ou de uma sugestão.
     * Se o item já está na lista: soma a quantidade (ou volta a desmarcá-lo),
     * em vez de criar uma linha duplicada.
     */
    suspend fun addItem(
        listId: Long,
        parsed: QuickAdd,
        suggestion: Suggestion? = null,
    ) = db.withTransaction {
        val name = Text.capitalizeFirst(suggestion?.name ?: parsed.name)
        if (name.isBlank()) return@withTransaction
        val key = Text.normalize(name)

        val existing = dao.getItems(listId).firstOrNull { Text.normalize(it.name) == key }
        if (existing != null) {
            val updated = when {
                existing.checked -> existing.copy(
                    checked = false,
                    checkedAt = null,
                    quantity = parsed.quantity ?: existing.quantity,
                    unit = parsed.unit ?: existing.unit,
                )
                parsed.quantity != null -> existing.copy(
                    quantity = parsed.quantity,
                    unit = parsed.unit ?: existing.unit,
                )
                Units.isCountable(existing.unit) -> existing.copy(quantity = existing.quantity + 1)
                else -> existing
            }
            dao.updateItem(updated)
            dao.touchList(listId, now())
            return@withTransaction
        }

        val history = dao.getHistory(key)
        val catalog = Catalog.find(name)
        val category = suggestion?.category
            ?: history?.let { Category.fromName(it.category) }
            ?: catalog?.category
            ?: Catalog.guessCategory(name)
            ?: Category.OUTROS
        val unit = parsed.unit ?: suggestion?.unit ?: history?.unit ?: catalog?.unit ?: "un"
        val price = suggestion?.lastPrice ?: history?.lastPrice

        dao.insertItem(
            Item(
                listId = listId,
                name = name,
                quantity = parsed.quantity ?: 1.0,
                unit = unit,
                price = price,
                category = category.name,
            )
        )
        saveHistory(name, category, unit, price)
        dao.touchList(listId, now())
    }

    suspend fun updateItem(item: Item) = db.withTransaction {
        val clean = item.copy(name = Text.capitalizeFirst(item.name), note = item.note.trim())
        dao.updateItem(clean)
        saveHistory(clean.name, Category.fromName(clean.category), clean.unit, clean.price, countUse = false)
        dao.touchList(clean.listId, now())
    }

    suspend fun restoreItem(item: Item) {
        dao.insertItem(item.copy(id = 0))
        dao.touchList(item.listId, now())
    }

    suspend fun deleteItem(item: Item) {
        dao.deleteItem(item)
        dao.touchList(item.listId, now())
    }

    suspend fun setChecked(item: Item, checked: Boolean) {
        dao.setChecked(item.id, checked, if (checked) now() else null)
        dao.touchList(item.listId, now())
    }

    suspend fun uncheckAll(listId: Long) {
        dao.uncheckAll(listId)
        dao.touchList(listId, now())
    }

    suspend fun deleteChecked(listId: Long) {
        dao.deleteChecked(listId)
        dao.touchList(listId, now())
    }

    // ---------- Histórico ----------

    private suspend fun saveHistory(
        name: String,
        category: Category,
        unit: String,
        price: Double?,
        countUse: Boolean = true,
    ) {
        val key = Text.normalize(name)
        val old = dao.getHistory(key)
        dao.upsertHistory(
            ItemHistory(
                nameKey = key,
                name = name,
                category = category.name,
                unit = unit,
                lastPrice = price ?: old?.lastPrice,
                useCount = (old?.useCount ?: 0) + if (countUse) 1 else 0,
                lastUsed = now(),
            )
        )
    }

    private fun now() = System.currentTimeMillis()
}
