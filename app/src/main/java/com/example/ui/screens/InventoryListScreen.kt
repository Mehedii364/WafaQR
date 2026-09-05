package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItem
import com.example.ui.components.AddEditItemDialog
import com.example.ui.components.ItemCard
import com.example.ui.components.QuickStockDialog
import com.example.ui.components.WafaQrDialog
import com.example.ui.theme.CleanMinimalBg
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalOnPrimaryContainer
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalRed
import com.example.ui.theme.CleanMinimalRedContainer
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun InventoryListScreen(
    items: List<InventoryItem>,
    allItems: List<InventoryItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    filterLowStockOnly: Boolean,
    onToggleLowStockFilter: () -> Unit,
    onQuickAdjust: (sku: String, delta: Int) -> Unit,
    onSaveItem: (InventoryItem) -> Unit,
    onDeleteItem: (InventoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEditDialogForItem by remember { mutableStateOf<InventoryItem?>(null) }
    var isAddingNewItem by remember { mutableStateOf(false) }
    var showQuickStockForItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showWafaQrForItem by remember { mutableStateOf<InventoryItem?>(null) }

    val lowStockCount = remember(allItems) {
        allItems.count { it.quantity <= it.minStockLevel }
    }

    val categories = listOf("All", "Electronics", "Packaging", "Warehouse", "Safety")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Section: Search & Filter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Search Input Field (Clean Minimalism White Pill)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_field"),
                    placeholder = { Text("Search by name, SKU, location...", color = CleanMinimalTextSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CleanMinimalPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = CleanMinimalTextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = CleanMinimalPrimary,
                        unfocusedBorderColor = CleanMinimalBorder,
                        focusedTextColor = CleanMinimalTextPrimary,
                        unfocusedTextColor = CleanMinimalTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Automated Low-Stock Alert Banner (Clean Minimalism Red container)
                AnimatedVisibility(visible = lowStockCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onToggleLowStockFilter() }
                            .testTag("low_stock_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanMinimalRedContainer),
                        border = BorderStroke(1.dp, CleanMinimalRed.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = CleanMinimalRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$lowStockCount item${if (lowStockCount > 1) "s" else ""} below minimum safety threshold",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = CleanMinimalRed
                                )
                            }
                            Text(
                                text = if (filterLowStockOnly) "Show All" else "Filter",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CleanMinimalRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal Category filter chips (Clean Minimalism)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category && !filterLowStockOnly
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onCategorySelected(category)
                            },
                            label = {
                                Text(
                                    category,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = CleanMinimalTextSecondary,
                                selectedContainerColor = CleanMinimalPrimaryContainer,
                                selectedLabelColor = CleanMinimalOnPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CleanMinimalBorder,
                                selectedBorderColor = CleanMinimalPrimary
                            ),
                            modifier = Modifier.testTag("chip_category_$category")
                        )
                    }

                    FilterChip(
                        selected = filterLowStockOnly,
                        onClick = onToggleLowStockFilter,
                        label = {
                            Text(
                                "⚠️ Low Stock",
                                fontSize = 12.sp,
                                fontWeight = if (filterLowStockOnly) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = CleanMinimalTextSecondary,
                            selectedContainerColor = CleanMinimalRedContainer,
                            selectedLabelColor = CleanMinimalRed
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filterLowStockOnly,
                            borderColor = CleanMinimalBorder,
                            selectedBorderColor = CleanMinimalRed
                        ),
                        modifier = Modifier.testTag("chip_low_stock")
                    )
                }
            }

            // Inventory Items List
            if (items.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(CleanMinimalPrimaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = CleanMinimalOnPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || filterLowStockOnly) "No matching inventory items" else "No Inventory Items",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinimalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try adjusting your filter or tap the '+' button below to add stock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinimalTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.sku }) { item ->
                        ItemCard(
                            item = item,
                            onQuickAdjust = { delta -> onQuickAdjust(item.sku, delta) },
                            onShowQuickStockDialog = { showQuickStockForItem = item },
                            onShowWafaQr = { showWafaQrForItem = item },
                            onEdit = { showAddEditDialogForItem = item },
                            onDelete = { onDeleteItem(item) }
                        )
                    }
                }
            }
        }

        // Floating Action Button (+ New Item)
        FloatingActionButton(
            onClick = { isAddingNewItem = true },
            containerColor = CleanMinimalPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_item_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Inventory Item")
        }
    }

    // Modal Dialogs
    if (isAddingNewItem) {
        AddEditItemDialog(
            onSave = { newItem ->
                onSaveItem(newItem)
                isAddingNewItem = false
            },
            onDismiss = { isAddingNewItem = false }
        )
    }

    showAddEditDialogForItem?.let { item ->
        AddEditItemDialog(
            initialItem = item,
            onSave = { updated ->
                onSaveItem(updated)
                showAddEditDialogForItem = null
            },
            onDismiss = { showAddEditDialogForItem = null }
        )
    }

    showQuickStockForItem?.let { item ->
        QuickStockDialog(
            item = item,
            onConfirm = { delta, reason ->
                onQuickAdjust(item.sku, delta)
                showQuickStockForItem = null
            },
            onDismiss = { showQuickStockForItem = null }
        )
    }

    showWafaQrForItem?.let { item ->
        WafaQrDialog(
            item = item,
            onDismiss = { showWafaQrForItem = null }
        )
    }
}
