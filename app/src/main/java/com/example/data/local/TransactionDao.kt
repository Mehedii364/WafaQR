package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StockTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE itemSku = :sku ORDER BY timestamp DESC")
    fun getTransactionsForSku(sku: String): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<StockTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransaction): Long

    @Query("UPDATE stock_transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markTransactionsSynced(ids: List<Long>)

    @Query("SELECT * FROM stock_transactions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getTransactionsSince(sinceTimestamp: Long): List<StockTransaction>
}
