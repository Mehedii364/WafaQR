package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String, // Unique barcode or QR code
    val name: String,
    val category: String = "General",
    val quantity: Int = 0,
    val minStockLevel: Int = 5, // Automated low-stock alert threshold
    val unitPrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val location: String = "Main Warehouse",
    val supplier: String = "Standard Supplier",
    val barcodeFormat: String = "QR_CODE",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
) {
    val stockStatus: StockStatus
        get() = when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity <= minStockLevel -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }

    val totalValuation: Double
        get() = quantity * unitPrice
}
