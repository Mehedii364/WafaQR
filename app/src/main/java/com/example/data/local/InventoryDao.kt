package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryItem
import com.example.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY lastUpdated DESC")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE sku = :sku LIMIT 1")
    suspend fun getItemBySku(sku: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE quantity <= minStockLevel ORDER BY quantity ASC")
    fun getLowStockItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE syncStatus != :synced")
    suspend fun getPendingSyncItems(synced: SyncStatus = SyncStatus.SYNCED): List<InventoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET quantity = :newQuantity, lastUpdated = :timestamp, syncStatus = :syncStatus WHERE sku = :sku")
    suspend fun updateQuantity(
        sku: String,
        newQuantity: Int,
        timestamp: Long = System.currentTimeMillis(),
        syncStatus: SyncStatus = SyncStatus.PENDING_UPDATE
    )

    @Query("UPDATE inventory_items SET syncStatus = :status WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<Long>, status: SyncStatus)

    @Query("DELETE FROM inventory_items WHERE sku = :sku")
    suspend fun deleteItemBySku(sku: String)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun getItemCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE quantity <= minStockLevel")
    fun getLowStockCount(): Flow<Int>
}
