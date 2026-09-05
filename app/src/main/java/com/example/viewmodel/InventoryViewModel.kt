package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.InventoryItem
import com.example.data.model.StockTransaction
import com.example.data.repository.InventoryRepository
import com.example.data.sync.CloudSyncManager
import com.example.data.sync.CloudSyncState
import com.example.scanner.BarcodeImageDecoder
import com.example.scanner.BarcodeResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val cloudSyncManager = CloudSyncManager()
    private val repository = InventoryRepository(
        inventoryDao = database.inventoryDao(),
        transactionDao = database.transactionDao(),
        cloudSyncManager = cloudSyncManager
    )

    val allItems: StateFlow<List<InventoryItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItem>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<StockTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cloudSyncState: StateFlow<CloudSyncState> = cloudSyncManager.syncState

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val filterLowStockOnly = MutableStateFlow(false)

    val filteredItems: StateFlow<List<InventoryItem>> = combine(
        allItems,
        searchQuery,
        selectedCategory,
        filterLowStockOnly
    ) { items, query, category, lowOnly ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.sku.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true)

            val matchesLowStock = !lowOnly || item.quantity <= item.minStockLevel

            matchesQuery && matchesCategory && matchesLowStock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scanned Barcode State
    private val _scannedBarcode = MutableStateFlow<BarcodeResult?>(null)
    val scannedBarcode: StateFlow<BarcodeResult?> = _scannedBarcode.asStateFlow()

    // Transient UI alerts / toasts
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            repository.updatePendingQueueCount()
        }
    }

    fun onBarcodeDetected(result: BarcodeResult) {
        if (_scannedBarcode.value?.text != result.text) {
            _scannedBarcode.value = result
            viewModelScope.launch {
                val matchedItem = repository.getItemBySku(result.text)
                if (matchedItem != null) {
                    _toastMessage.emit("Detected: ${matchedItem.name} (${matchedItem.quantity} in stock)")
                } else {
                    _toastMessage.emit("Scanned New Barcode: ${result.text}")
                }
            }
        }
    }

    fun analyzeImageBitmap(bitmap: Bitmap): BarcodeResult? {
        val decoded = BarcodeImageDecoder.decodeBitmap(bitmap)
        if (decoded != null) {
            onBarcodeDetected(decoded)
        } else {
            viewModelScope.launch {
                _toastMessage.emit("No barcode or QR code detected in the selected image")
            }
        }
        return decoded
    }

    fun clearScannedBarcode() {
        _scannedBarcode.value = null
    }

    fun quickAdjustStock(sku: String, delta: Int, reason: String = "Manual Scan") {
        viewModelScope.launch {
            val result = repository.quickAdjustStock(sku, delta, reason)
            result.onSuccess { updatedItem ->
                _toastMessage.emit("Updated: ${updatedItem.name} → ${updatedItem.quantity} units")
            }.onFailure { err ->
                _toastMessage.emit("Error: ${err.localizedMessage}")
            }
        }
    }

    fun saveItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.saveItem(item)
            _toastMessage.emit("Saved item: ${item.name}")
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _toastMessage.emit("Deleted ${item.name}")
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            val result = repository.syncWithCloud()
            _toastMessage.emit(result.message)
        }
    }

    fun toggleOnlineStatus() {
        val current = cloudSyncState.value.isOnline
        cloudSyncManager.updateOnlineStatus(!current)
    }

    fun updateEndpointUrl(url: String) {
        cloudSyncManager.updateEndpoint(url)
    }

    suspend fun getExportableCsvReport(category: String?, lowStockOnly: Boolean): String {
        return repository.generateCsvReport(category, lowStockOnly)
    }
}
