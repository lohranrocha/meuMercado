package com.listadecompras.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    // ---------- Listas ----------

    @Query("SELECT * FROM shopping_lists ORDER BY updatedAt DESC")
    fun observeLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observeList(id: Long): Flow<ShoppingList?>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getList(id: Long): ShoppingList?

    @Query("SELECT COUNT(*) FROM shopping_lists")
    suspend fun countLists(): Int

    @Insert
    suspend fun insertList(list: ShoppingList): Long

    @Query("UPDATE shopping_lists SET name = :name, updatedAt = :now WHERE id = :id")
    suspend fun renameList(id: Long, name: String, now: Long)

    @Query("UPDATE shopping_lists SET updatedAt = :now WHERE id = :id")
    suspend fun touchList(id: Long, now: Long)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteList(id: Long)

    // ---------- Itens ----------

    @Query("SELECT * FROM items")
    fun observeAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE listId = :listId ORDER BY createdAt ASC")
    fun observeItems(listId: Long): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE listId = :listId ORDER BY createdAt ASC")
    suspend fun getItems(listId: Long): List<Item>

    @Insert
    suspend fun insertItem(item: Item): Long

    @Insert
    suspend fun insertItems(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items WHERE listId = :listId")
    suspend fun deleteItemsOfList(listId: Long)

    @Query("UPDATE items SET checked = :checked, checkedAt = :checkedAt WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean, checkedAt: Long?)

    @Query("UPDATE items SET checked = 0, checkedAt = NULL WHERE listId = :listId")
    suspend fun uncheckAll(listId: Long)

    @Query("DELETE FROM items WHERE listId = :listId AND checked = 1")
    suspend fun deleteChecked(listId: Long)

    // ---------- Histórico / sugestões ----------

    @Query("SELECT * FROM item_history ORDER BY useCount DESC, lastUsed DESC")
    fun observeHistory(): Flow<List<ItemHistory>>

    @Query("SELECT * FROM item_history WHERE nameKey = :key")
    suspend fun getHistory(key: String): ItemHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHistory(history: ItemHistory)
}
