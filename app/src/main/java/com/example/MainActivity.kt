package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.InventoryListScreen
import com.example.ui.screens.ReportsSyncScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    SCANNER("Scanner", Icons.Default.QrCodeScanner, "nav_tab_scanner"),
    INVENTORY("Inventory", Icons.Default.Inventory2, "nav_tab_inventory"),
    ANALYTICS("Analytics", Icons.Default.Assessment, "nav_tab_analytics"),
    REPORTS_SYNC("Reports & Sync", Icons.Default.CloudSync, "nav_tab_reports_sync")
}

@Composable
fun MainAppScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredItems.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val lastScannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val filterLowStockOnly by viewModel.filterLowStockOnly.collectAsStateWithLifecycle()

    // Listen for transient toast/snackbar messages
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CleanMinimalPrimary else CleanMinimalTextSecondary.copy(alpha = 0.7f)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 10.sp,
                                letterSpacing = 0.3.sp,
                                color = if (isSelected) CleanMinimalPrimary else CleanMinimalTextSecondary.copy(alpha = 0.7f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CleanMinimalPrimaryContainer,
                            selectedIconColor = CleanMinimalPrimary,
                            unselectedIconColor = CleanMinimalTextSecondary,
                            selectedTextColor = CleanMinimalPrimary,
                            unselectedTextColor = CleanMinimalTextSecondary
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (NavigationTab.entries[currentTab]) {
                NavigationTab.SCANNER -> {
                    ScannerScreen(
                        items = allItems,
                        lastScannedBarcode = lastScannedBarcode,
                        onBarcodeDetected = { viewModel.onBarcodeDetected(it) },
                        onAnalyzeImage = { viewModel.analyzeImageBitmap(it) },
                        onQuickStock = { sku, delta, reason ->
                            viewModel.quickAdjustStock(sku, delta, reason)
                        },
                        onSaveItem = { viewModel.saveItem(it) },
                        onClearScannedBarcode = { viewModel.clearScannedBarcode() }
                    )
                }

                NavigationTab.INVENTORY -> {
                    InventoryListScreen(
                        items = filteredItems,
                        allItems = allItems,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectedCategory.value = it },
                        filterLowStockOnly = filterLowStockOnly,
                        onToggleLowStockFilter = {
                            viewModel.filterLowStockOnly.value = !viewModel.filterLowStockOnly.value
                        },
                        onQuickAdjust = { sku, delta ->
                            viewModel.quickAdjustStock(sku, delta, "Quick Counter Adjustment")
                        },
                        onSaveItem = { viewModel.saveItem(it) },
                        onDeleteItem = { viewModel.deleteItem(it) }
                    )
                }

                NavigationTab.ANALYTICS -> {
                    AnalyticsScreen(
                        items = allItems,
                        transactions = allTransactions,
                        onQuickRestock = { sku, amount ->
                            viewModel.quickAdjustStock(sku, amount, "Low Stock Priority Restock")
                        }
                    )
                }

                NavigationTab.REPORTS_SYNC -> {
                    ReportsSyncScreen(
                        items = allItems,
                        cloudSyncState = cloudSyncState,
                        onTriggerSync = { viewModel.triggerCloudSync() },
                        onToggleOnlineStatus = { viewModel.toggleOnlineStatus() },
                        onUpdateEndpoint = { viewModel.updateEndpointUrl(it) },
                        onGenerateReport = { cat, lowOnly ->
                            viewModel.getExportableCsvReport(cat, lowOnly)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

