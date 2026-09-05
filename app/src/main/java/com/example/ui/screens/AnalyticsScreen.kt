package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.InventoryItem
import com.example.data.model.StockTransaction
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryDistributionChart
import com.example.ui.components.InventoryKpiCards
import com.example.ui.components.StockMovementTrendChart
import com.example.ui.theme.CleanMinimalBg
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalGreen
import com.example.ui.theme.CleanMinimalGreenContainer
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalRed
import com.example.ui.theme.CleanMinimalRedContainer
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    items: List<InventoryItem>,
    transactions: List<StockTransaction>,
    onQuickRestock: (sku: String, amount: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lowStockItems = items.filter { it.quantity <= it.minStockLevel }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBg),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_inventory_hero),
                    contentDescription = "Warehouse Analytics Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Clean overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Warehouse Analytics",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Real-time stock velocity, turnover & threshold monitoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinimalPrimaryContainer
                    )
                }
            }
        }

        // 4 KPI Summary Cards
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                InventoryKpiCards(items = items)
            }
        }

        // 7-Day Movement Trend Chart
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                StockMovementTrendChart(transactions = transactions)
            }
        }

        // Category Distribution Donut Chart
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CategoryDistributionChart(items = items)
            }
        }

        // Automated Low-Stock Alert & Reorder Priority List
        if (lowStockItems.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CleanMinimalRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Low Stock Reorder Priorities",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CleanMinimalTextPrimary
                            )
                        }
                        Text(
                            text = "${lowStockItems.size} items",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CleanMinimalRed
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    lowStockItems.forEach { lowItem ->
                        val deficit = (lowItem.minStockLevel * 2) - lowItem.quantity
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, CleanMinimalBorder),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lowItem.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = CleanMinimalTextPrimary
                                    )
                                    Text(
                                        text = "Current: ${lowItem.quantity} (Safety: ${lowItem.minStockLevel}) • ${lowItem.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CleanMinimalRed
                                    )
                                }

                                Button(
                                    onClick = { onQuickRestock(lowItem.sku, deficit.coerceAtLeast(10)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("restock_btn_${lowItem.sku}")
                                ) {
                                    Text(
                                        text = "+$deficit Restock",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Audit Log / Transactions (Past movements)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Recent Stock Movements",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CleanMinimalTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (transactions.isEmpty()) {
                    Text(
                        text = "No stock transactions recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinimalTextSecondary
                    )
                } else {
                    transactions.take(5).forEach { tx ->
                        val isPositive = tx.type == TransactionType.STOCK_IN
                        val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(tx.timestamp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CleanMinimalBorder),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isPositive) CleanMinimalGreenContainer else CleanMinimalRedContainer,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (isPositive) CleanMinimalGreen else CleanMinimalRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tx.itemName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = CleanMinimalTextPrimary
                                        )
                                        Text(
                                            text = "${tx.reason} • $dateStr",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CleanMinimalTextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (isPositive) "+" else ""}${tx.quantityChange}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPositive) CleanMinimalGreen else CleanMinimalRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
