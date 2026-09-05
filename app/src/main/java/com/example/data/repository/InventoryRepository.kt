package com.example.data.repository

import com.example.data.local.InventoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.InventoryItem
import com.example.data.model.StockTransaction
import com.example.data.model.SyncStatus
import com.example.data.model.TransactionType
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.SyncResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryRepository(
    private val inventoryDao: InventoryDao,
    private val transactionDao: TransactionDao,
    val cloudSyncManager: CloudSyncManager
) {
    val allItems: Flow<List<InventoryItem>> = inventoryDao.getAllItems()
    val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()
    val allTransactions: Flow<List<StockTransaction>> = transactionDao.getAllTransactions()
    val totalItemCount: Flow<Int> = inventoryDao.getItemCount()
    val lowStockCount: Flow<Int> = inventoryDao.getLowStockCount()

    suspend fun getItemBySku(sku: String): InventoryItem? = withContext(Dispatchers.IO) {
        inventoryDao.getItemBySku(sku)
    }

    suspend fun getItemById(id: Long): InventoryItem? = withContext(Dispatchers.IO) {
        inventoryDao.getItemById(id)
    }

    suspend fun saveItem(item: InventoryItem): Long = withContext(Dispatchers.IO) {
        val existing = inventoryDao.getItemBySku(item.sku)
        val id = if (existing != null) {
            val updated = item.copy(
                id = existing.id,
                lastUpdated = System.currentTimeMillis(),
                syncStatus = SyncStatus.PENDING_UPDATE
            )
            inventoryDao.updateItem(updated)
            existing.id
        } else {
            val newId = inventoryDao.insertItem(
                item.copy(
                    lastUpdated = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_CREATE
                )
            )
            // Log initial stock transaction if quantity > 0
            if (item.quantity > 0) {
                transactionDao.insertTransaction(
                    StockTransaction(
                        itemSku = item.sku,
                        itemName = item.name,
                        type = TransactionType.STOCK_IN,
                        quantityChange = item.quantity,
                        previousQuantity = 0,
                        newQuantity = item.quantity,
                        reason = "Initial Inventory Intake",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            newId
        }

        updatePendingQueueCount()
        id
    }

    suspend fun quickAdjustStock(
        sku: String,
        delta: Int,
        reason: String
    ): Result<InventoryItem> = withContext(Dispatchers.IO) {
        val item = inventoryDao.getItemBySku(sku)
            ?: return@withContext Result.failure(Exception("Item with barcode $sku not found"))

        val newQuantity = (item.quantity + delta).coerceAtLeast(0)
        val actualChange = newQuantity - item.quantity

        if (actualChange == 0 && delta != 0) {
            return@withContext Result.failure(Exception("Item is already at 0 stock"))
        }

        val updated = item.copy(
            quantity = newQuantity,
            lastUpdated = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPDATE
        )
        inventoryDao.updateItem(updated)

        val txType = if (delta > 0) TransactionType.STOCK_IN else TransactionType.STOCK_OUT
        transactionDao.insertTransaction(
            StockTransaction(
                itemSku = item.sku,
                itemName = item.name,
                type = txType,
                quantityChange = actualChange,
                previousQuantity = item.quantity,
                newQuantity = newQuantity,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )

        updatePendingQueueCount()
        Result.success(updated)
    }

    suspend fun deleteItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        inventoryDao.deleteItemById(item.id)
        updatePendingQueueCount()
    }

    suspend fun syncWithCloud(): SyncResult = withContext(Dispatchers.IO) {
        val pendingItems = inventoryDao.getPendingSyncItems()
        val pendingTransactions = transactionDao.getUnsyncedTransactions()

        val result = cloudSyncManager.performSync(pendingItems, pendingTransactions)
        if (result.success) {
            if (result.syncedItemIds.isNotEmpty()) {
                inventoryDao.updateSyncStatus(result.syncedItemIds, SyncStatus.SYNCED)
            }
            if (result.syncedTransactionIds.isNotEmpty()) {
                transactionDao.markTransactionsSynced(result.syncedTransactionIds)
            }
        }
        updatePendingQueueCount()
        result
    }

    suspend fun updatePendingQueueCount() = withContext(Dispatchers.IO) {
        val pendingItems = inventoryDao.getPendingSyncItems()
        cloudSyncManager.updatePendingCount(pendingItems.size)
    }

    suspend fun generateCsvReport(filterCategory: String? = null, lowStockOnly: Boolean = false): String = withContext(Dispatchers.IO) {
        val items = inventoryDao.getAllItems().first().filter { item ->
            (filterCategory == null || filterCategory == "All" || item.category.equals(filterCategory, ignoreCase = true)) &&
                    (!lowStockOnly || item.quantity <= item.minStockLevel)
        }

        val sb = StringBuilder()
        sb.append("SKU,Item Name,Category,Quantity,Min Alert,Unit Price,Total Value,Location,Supplier,Status\n")
        items.forEach { item ->
            val status = item.stockStatus.name
            sb.append("\"${item.sku}\",\"${item.name}\",\"${item.category}\",${item.quantity},${item.minStockLevel},${item.unitPrice},${item.totalValuation},\"${item.location}\",\"${item.supplier}\",\"$status\"\n")
        }
        sb.toString()
    }

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = inventoryDao.getItemCount().first()
        if (count == 0) {
            val sampleItems = listOf(
                InventoryItem(
                    sku = "WAFA-984210",
                    name = "Wireless Barcode Laser Scanner X1",
                    category = "Electronics",
                    quantity = 28,
                    minStockLevel = 10,
                    unitPrice = 85.00,
                    costPrice = 45.00,
                    location = "Aisle 3 - Bay B",
                    supplier = "TechScan Global",
                    barcodeFormat = "QR_CODE",
                    notes = "High precision 2D imager, USB & Bluetooth",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-110482",
                    name = "Thermal Shipping Labels 4x6 (Roll 500)",
                    category = "Packaging",
                    quantity = 3, // LOW STOCK TRIGGER
                    minStockLevel = 15,
                    unitPrice = 14.50,
                    costPrice = 8.20,
                    location = "Shelf 1 - Rack C",
                    supplier = "PackPro Supplies",
                    barcodeFormat = "CODE_128",
                    notes = "BPA free direct thermal paper roll",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-448201",
                    name = "Heavy Duty Packing Tape (6-Pack)",
                    category = "Packaging",
                    quantity = 45,
                    minStockLevel = 12,
                    unitPrice = 19.99,
                    costPrice = 10.50,
                    location = "Shelf 1 - Rack D",
                    supplier = "PackPro Supplies",
                    barcodeFormat = "CODE_128",
                    notes = "2.8 mil commercial grade acrylic tape",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-773192",
                    name = "USB-C Industrial Charging Dock",
                    category = "Electronics",
                    quantity = 4, // LOW STOCK TRIGGER
                    minStockLevel = 8,
                    unitPrice = 64.00,
                    costPrice = 32.00,
                    location = "Aisle 4 - Cabinet 2",
                    supplier = "TechScan Global",
                    barcodeFormat = "QR_CODE",
                    notes = "Multi-bay fast charger for handheld terminals",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-330198",
                    name = "Corrugated Shipping Boxes 12x9x4",
                    category = "Packaging",
                    quantity = 150,
                    minStockLevel = 50,
                    unitPrice = 1.25,
                    costPrice = 0.60,
                    location = "Pallet Staging P-04",
                    supplier = "BoxCraft Logistics",
                    barcodeFormat = "CODE_128",
                    notes = "ECT-32 single wall corrugated cardboard",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-662914",
                    name = "Safety High-Vis Warehouse Vest (L)",
                    category = "Safety",
                    quantity = 0, // OUT OF STOCK
                    minStockLevel = 10,
                    unitPrice = 12.00,
                    costPrice = 6.00,
                    location = "Safety Lockers S-1",
                    supplier = "SafeGuard Gear",
                    barcodeFormat = "QR_CODE",
                    notes = "ANSI Class 2 certified with reflective strips",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-552109",
                    name = "Handheld Mobile Computer Terminal",
                    category = "Electronics",
                    quantity = 12,
                    minStockLevel = 5,
                    unitPrice = 420.00,
                    costPrice = 290.00,
                    location = "Secure Cage C-01",
                    supplier = "MobileData Corp",
                    barcodeFormat = "QR_CODE",
                    notes = "Rugged Android 13 scanner terminal with Gorilla Glass",
                    syncStatus = SyncStatus.SYNCED
                ),
                InventoryItem(
                    sku = "WAFA-229871",
                    name = "Adjustable Heavy Steel Shelving Unit",
                    category = "Warehouse",
                    quantity = 7,
                    minStockLevel = 5,
                    unitPrice = 189.00,
                    costPrice = 110.00,
                    location = "Bulk Storage B-02",
                    supplier = "MetalWorks Rack",
                    barcodeFormat = "CODE_128",
                    notes = "5-tier modular storage rack (2000 lb capacity)",
                    syncStatus = SyncStatus.SYNCED
                )
            )

            inventoryDao.insertItems(sampleItems)

            // Seed historical stock transactions
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            val transactions = listOf(
                StockTransaction(
                    itemSku = "WAFA-984210",
                    itemName = "Wireless Barcode Laser Scanner X1",
                    type = TransactionType.STOCK_IN,
                    quantityChange = 30,
                    previousQuantity = 0,
                    newQuantity = 30,
                    reason = "Purchase Order #PO-8821",
                    timestamp = now - (6 * dayMs),
                    isSynced = true
                ),
                StockTransaction(
                    itemSku = "WAFA-984210",
                    itemName = "Wireless Barcode Laser Scanner X1",
                    type = TransactionType.STOCK_OUT,
                    quantityChange = -2,
                    previousQuantity = 30,
                    newQuantity = 28,
                    reason = "Dispatch to Retail Store 4",
                    timestamp = now - (4 * dayMs),
                    isSynced = true
                ),
                StockTransaction(
                    itemSku = "WAFA-110482",
                    itemName = "Thermal Shipping Labels 4x6 (Roll 500)",
                    type = TransactionType.STOCK_OUT,
                    quantityChange = -12,
                    previousQuantity = 15,
                    newQuantity = 3,
                    reason = "Packing Station Daily Issue",
                    timestamp = now - (1 * dayMs),
                    isSynced = true
                ),
                StockTransaction(
                    itemSku = "WAFA-448201",
                    itemName = "Heavy Duty Packing Tape (6-Pack)",
                    type = TransactionType.STOCK_IN,
                    quantityChange = 50,
                    previousQuantity = 0,
                    newQuantity = 50,
                    reason = "Bulk Supplier Restock",
                    timestamp = now - (3 * dayMs),
                    isSynced = true
                ),
                StockTransaction(
                    itemSku = "WAFA-448201",
                    itemName = "Heavy Duty Packing Tape (6-Pack)",
                    type = TransactionType.STOCK_OUT,
                    quantityChange = -5,
                    previousQuantity = 50,
                    newQuantity = 45,
                    reason = "Fulfillment Center Distribution",
                    timestamp = now - (12 * 3600000L),
                    isSynced = true
                )
            )

            transactions.forEach { tx ->
                transactionDao.insertTransaction(tx)
            }
        }
    }
}
