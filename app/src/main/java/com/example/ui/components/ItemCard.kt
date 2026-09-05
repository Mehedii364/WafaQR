package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.StockStatus
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
import com.example.ui.theme.StatusAmber
import java.util.Locale

@Composable
fun ItemCard(
    item: InventoryItem,
    onQuickAdjust: (delta: Int) -> Unit,
    onShowQuickStockDialog: () -> Unit,
    onShowWafaQr: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onShowQuickStockDialog() }
            .testTag("item_card_${item.sku}"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CleanMinimalBorder),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category + Barcode SKU + Actions Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CleanMinimalPrimaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CleanMinimalOnPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.sku,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = CleanMinimalTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onShowWafaQr,
                        modifier = Modifier.size(32.dp).testTag("qr_button_${item.sku}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "View Wafa QR",
                            tint = CleanMinimalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp).testTag("menu_button_${item.sku}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = CleanMinimalTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Quick Stock In/Out") },
                                onClick = {
                                    showMenu = false
                                    onShowQuickStockDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Generate Wafa QR") },
                                onClick = {
                                    showMenu = false
                                    onShowWafaQr()
                                },
                                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = CleanMinimalPrimary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Details") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Item", color = CleanMinimalRed) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CleanMinimalRed) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Item Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CleanMinimalTextPrimary
            )

            // Location & Price
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📍 ${item.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleanMinimalTextSecondary
                )
                Text(
                    text = String.format(Locale.US, "$%.2f/unit", item.unitPrice),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CleanMinimalTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: Stock Status Badge + Stepper (+1 / -1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Alert Badge
                val (badgeBg, badgeTextColor, statusLabel) = when (item.stockStatus) {
                    StockStatus.OUT_OF_STOCK -> Triple(CleanMinimalRedContainer, CleanMinimalRed, "OUT OF STOCK")
                    StockStatus.LOW_STOCK -> Triple(CleanMinimalRedContainer, CleanMinimalRed, "LOW STOCK (${item.quantity}/${item.minStockLevel})")
                    StockStatus.IN_STOCK -> Triple(CleanMinimalGreenContainer, CleanMinimalGreen, "${item.quantity} IN STOCK")
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.stockStatus != StockStatus.IN_STOCK) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = badgeTextColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = badgeTextColor
                    )
                }

                // Quick Increment/Decrement Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3EDF7))
                            .clickable { onQuickAdjust(-1) }
                            .testTag("item_minus_${item.sku}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease Stock",
                            tint = if (item.quantity > 0) CleanMinimalTextPrimary else CleanMinimalTextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinimalTextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CleanMinimalPrimaryContainer)
                            .clickable { onQuickAdjust(1) }
                            .testTag("item_plus_${item.sku}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase Stock",
                            tint = CleanMinimalOnPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
