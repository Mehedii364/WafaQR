package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InventoryItem
import com.example.ui.theme.CleanMinimalBg
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalGreen
import com.example.ui.theme.CleanMinimalGreenContainer
import com.example.ui.theme.CleanMinimalOnPrimaryContainer
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalRed
import com.example.ui.theme.CleanMinimalRedContainer
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickStockDialog(
    item: InventoryItem,
    onConfirm: (delta: Int, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isStockIn by remember { mutableStateOf(true) }
    var quantityInput by remember { mutableIntStateOf(1) }
    var selectedReason by remember { mutableStateOf("Manual Adjustment") }
    var customNotes by remember { mutableStateOf("") }

    val presetReasonsIn = listOf("Purchase Received", "Customer Return", "Inventory Transfer", "Audit Recount")
    val presetReasonsOut = listOf("Customer Sale", "Store Fulfillment", "Damaged / Defect", "Expired", "Internal Use")

    val finalDelta = if (isStockIn) quantityInput else -quantityInput
    val newQuantity = (item.quantity + finalDelta).coerceAtLeast(0)
    val willBeLowStock = newQuantity <= item.minStockLevel && newQuantity > 0
    val willBeOutOfStock = newQuantity == 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("quick_stock_dialog"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CleanMinimalBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Update Stock Level",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CleanMinimalTextPrimary
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanMinimalTextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("quick_stock_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CleanMinimalTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stock In vs Stock Out Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CleanMinimalBg, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = {
                            isStockIn = true
                            selectedReason = "Purchase Received"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStockIn) CleanMinimalPrimary else Color.Transparent,
                            contentColor = if (isStockIn) Color.White else CleanMinimalTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("tab_stock_in")
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stock In (+)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isStockIn = false
                            selectedReason = "Customer Sale"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isStockIn) CleanMinimalRed else Color.Transparent,
                            contentColor = if (!isStockIn) Color.White else CleanMinimalTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("tab_stock_out")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stock Out (-)", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity stepper & indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (quantityInput > 1) quantityInput-- },
                        modifier = Modifier
                            .size(44.dp)
                            .background(CleanMinimalBg, CircleShape)
                            .testTag("decrement_stock_input")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = CleanMinimalTextPrimary)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "$quantityInput",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isStockIn) CleanMinimalPrimary else CleanMinimalRed
                        )
                        Text(
                            text = "Units to ${if (isStockIn) "add" else "remove"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanMinimalTextSecondary
                        )
                    }

                    IconButton(
                        onClick = { quantityInput++ },
                        modifier = Modifier
                            .size(44.dp)
                            .background(CleanMinimalBg, CircleShape)
                            .testTag("increment_stock_input")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = CleanMinimalTextPrimary)
                    }
                }

                // Quick Increment Pills (+1, +5, +10, +25)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf(1, 5, 10, 25).forEach { amount ->
                        val isSelected = quantityInput == amount
                        FilterChip(
                            selected = isSelected,
                            onClick = { quantityInput = amount },
                            label = { Text("+$amount", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(10.dp),
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
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Before vs After Projected Stock Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CleanMinimalBg, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Stock", style = MaterialTheme.typography.labelSmall, color = CleanMinimalTextSecondary)
                            Text("${item.quantity} units", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CleanMinimalTextPrimary)
                        }

                        Text("➔", style = MaterialTheme.typography.titleLarge, color = CleanMinimalTextSecondary)

                        Column(horizontalAlignment = Alignment.End) {
                            Text("New Projected", style = MaterialTheme.typography.labelSmall, color = CleanMinimalTextSecondary)
                            Text(
                                text = "$newQuantity units",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = when {
                                    willBeOutOfStock -> CleanMinimalRed
                                    willBeLowStock -> CleanMinimalRed
                                    else -> CleanMinimalGreen
                                }
                            )
                        }
                    }
                }

                // Low Stock Warning Alert if triggered
                if (willBeLowStock || willBeOutOfStock) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                CleanMinimalRedContainer,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CleanMinimalRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (willBeOutOfStock) "Warning: Item will be completely out of stock!" else "Low-stock alert: Reaches threshold (${item.minStockLevel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanMinimalRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Preset Reason chips
                Text(
                    text = "Reason / Notes",
                    style = MaterialTheme.typography.labelMedium,
                    color = CleanMinimalTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val reasons = if (isStockIn) presetReasonsIn else presetReasonsOut
                    reasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedReason = reason },
                            label = { Text(reason, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(10.dp),
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
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Action Button
                Button(
                    onClick = {
                        val reasonText = if (customNotes.isNotBlank()) "$selectedReason: $customNotes" else selectedReason
                        onConfirm(finalDelta, reasonText)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStockIn) CleanMinimalPrimary else CleanMinimalRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_quick_stock_button")
                ) {
                    Text(
                        text = if (isStockIn) "Confirm Stock In (+$quantityInput)" else "Confirm Stock Out (-$quantityInput)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
