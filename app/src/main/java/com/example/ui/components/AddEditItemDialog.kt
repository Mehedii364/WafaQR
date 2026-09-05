package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InventoryItem
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary
import kotlin.random.Random

@Composable
fun AddEditItemDialog(
    initialItem: InventoryItem? = null,
    initialBarcode: String? = null,
    onSave: (InventoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    var sku by remember {
        mutableStateOf(
            initialItem?.sku
                ?: initialBarcode
                ?: "WAFA-${Random.nextInt(100000, 999999)}"
        )
    }
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Electronics") }
    var quantityText by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "10") }
    var minStockText by remember { mutableStateOf(initialItem?.minStockLevel?.toString() ?: "5") }
    var unitPriceText by remember { mutableStateOf(initialItem?.unitPrice?.toString() ?: "29.99") }
    var costPriceText by remember { mutableStateOf(initialItem?.costPrice?.toString() ?: "15.00") }
    var location by remember { mutableStateOf(initialItem?.location ?: "Warehouse A - Bay 1") }
    var supplier by remember { mutableStateOf(initialItem?.supplier ?: "Standard Supplier") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CleanMinimalPrimary,
        unfocusedBorderColor = CleanMinimalBorder,
        focusedTextColor = CleanMinimalTextPrimary,
        unfocusedTextColor = CleanMinimalTextPrimary
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("add_edit_item_dialog"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CleanMinimalBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem != null) "Edit Inventory Item" else "New Inventory Item",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinimalTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_close_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CleanMinimalTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SKU / Barcode field with generate button
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("Barcode / SKU Number", color = CleanMinimalTextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("input_sku"),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    trailingIcon = {
                        IconButton(onClick = { sku = "WAFA-${Random.nextInt(100000, 999999)}" }) {
                            Icon(Icons.Default.Autorenew, contentDescription = "Generate Random SKU", tint = CleanMinimalPrimary)
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    label = { Text("Item Name *", color = CleanMinimalTextSecondary) },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name cannot be empty") } } else null,
                    modifier = Modifier.fillMaxWidth().testTag("input_name"),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category", color = CleanMinimalTextSecondary) },
                        modifier = Modifier.weight(1f).testTag("input_category"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Storage Loc", color = CleanMinimalTextSecondary) },
                        modifier = Modifier.weight(1f).testTag("input_location"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity & Min Alert Threshold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Stock Quantity", color = CleanMinimalTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_quantity"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockText,
                        onValueChange = { minStockText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Low-Stock Alert", color = CleanMinimalTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("input_min_stock"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unit Price & Cost Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Retail Price ($)", color = CleanMinimalTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_unit_price"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Cost Price ($)", color = CleanMinimalTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("input_cost_price"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Supplier
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name", color = CleanMinimalTextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("input_supplier"),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Specifications / Notes", color = CleanMinimalTextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("input_notes"),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CleanMinimalBorder),
                        modifier = Modifier.weight(1f).testTag("dialog_cancel_button")
                    ) {
                        Text("Cancel", color = CleanMinimalTextPrimary)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val qty = quantityText.toIntOrNull() ?: 0
                            val minStock = minStockText.toIntOrNull() ?: 5
                            val price = unitPriceText.toDoubleOrNull() ?: 0.0
                            val cost = costPriceText.toDoubleOrNull() ?: 0.0

                            val item = InventoryItem(
                                id = initialItem?.id ?: 0,
                                sku = sku.trim().ifBlank { "WAFA-${Random.nextInt(100000, 999999)}" },
                                name = name.trim(),
                                category = category.trim().ifBlank { "General" },
                                quantity = qty,
                                minStockLevel = minStock,
                                unitPrice = price,
                                costPrice = cost,
                                location = location.trim(),
                                supplier = supplier.trim(),
                                notes = notes.trim(),
                                lastUpdated = System.currentTimeMillis()
                            )
                            onSave(item)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("dialog_save_button")
                    ) {
                        Text("Save Item", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
