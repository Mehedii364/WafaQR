package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    STOCK_IN,
    STOCK_OUT,
    AUDIT_ADJUSTMENT
}

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemSku: String,
    val itemName: String,
    val type: TransactionType,
    val quantityChange: Int, // positive for in, negative for out
    val previousQuantity: Int,
    val newQuantity: Int,
    val reason: String = "Manual Scan",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
