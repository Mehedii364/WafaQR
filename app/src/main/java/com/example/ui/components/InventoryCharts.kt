package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItem
import com.example.data.model.StockTransaction
import com.example.data.model.TransactionType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

/**
 * 4 Key Performance Indicator (KPI) Metric Summary Cards (Clean Minimalism)
 */
@Composable
fun InventoryKpiCards(
    items: List<InventoryItem>,
    modifier: Modifier = Modifier
) {
    val totalItems = items.size
    val totalUnits = items.sumOf { it.quantity }
    val lowStockCount = items.count { it.quantity <= it.minStockLevel }
    val totalValuation = items.sumOf { it.totalValuation }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                title = "Total SKU",
                value = "$totalItems",
                subtitle = "Active products",
                containerColor = CleanMinimalPrimaryContainer,
                contentColor = CleanMinimalOnPrimaryContainer,
                icon = Icons.Default.Inventory2,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "Total Units",
                value = "$totalUnits",
                subtitle = "In warehouse",
                containerColor = CleanMinimalGreenContainer,
                contentColor = CleanMinimalGreen,
                icon = Icons.Default.ArrowUpward,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                title = "Low Stock Alerts",
                value = "$lowStockCount",
                subtitle = if (lowStockCount > 0) "Needs reorder!" else "Healthy",
                containerColor = if (lowStockCount > 0) CleanMinimalRedContainer else CleanMinimalGreenContainer,
                contentColor = if (lowStockCount > 0) CleanMinimalRed else CleanMinimalGreen,
                icon = Icons.Default.Warning,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "Inventory Value",
                value = String.format(Locale.US, "$%,.0f", totalValuation),
                subtitle = "Net retail",
                containerColor = Color(0xFFF3EDF7),
                contentColor = CleanMinimalPrimary,
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor.copy(alpha = 0.85f)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 7-Day Stock Movement Trend Chart (Clean Minimalism Canvas Line/Area Chart)
 */
@Composable
fun StockMovementTrendChart(
    transactions: List<StockTransaction>,
    modifier: Modifier = Modifier
) {
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedRatio by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800),
        label = "trend_anim"
    )

    LaunchedEffect(Unit) {
        animationProgress = 1f
    }

    // Group last 7 days of transactions into daily IN and OUT sums
    val now = System.currentTimeMillis()
    val dayMs = 86400000L
    val days = (6 downTo 0).map { dayOffset ->
        val startDay = now - (dayOffset * dayMs)
        val endDay = startDay + dayMs
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(startDay))
        val dayIn = transactions.filter { it.timestamp in startDay until endDay && it.type == TransactionType.STOCK_IN }
            .sumOf { it.quantityChange }
        val dayOut = transactions.filter { it.timestamp in startDay until endDay && it.type == TransactionType.STOCK_OUT }
            .sumOf { kotlin.math.abs(it.quantityChange) }
        Triple(dayFormat, max(dayIn, 2), max(dayOut, 1))
    }

    val maxVal = max(days.maxOf { max(it.second, it.third) }, 10).toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CleanMinimalBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock Movement Trends",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = CleanMinimalTextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CleanMinimalGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("In", style = MaterialTheme.typography.labelSmall, color = CleanMinimalTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CleanMinimalPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Out", style = MaterialTheme.typography.labelSmall, color = CleanMinimalTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height - 20f
                    val stepX = w / (days.size - 1)

                    // Draw subtle grid lines
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val y = h * (i / gridSteps.toFloat())
                        drawLine(
                            color = CleanMinimalBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Build Paths for IN (Green) and OUT (Primary Purple)
                    val inPath = Path()
                    val outPath = Path()

                    days.forEachIndexed { index, triple ->
                        val x = index * stepX
                        val inY = h - (triple.second / maxVal * h * animatedRatio)
                        val outY = h - (triple.third / maxVal * h * animatedRatio)

                        if (index == 0) {
                            inPath.moveTo(x, inY)
                            outPath.moveTo(x, outY)
                        } else {
                            inPath.lineTo(x, inY)
                            outPath.lineTo(x, outY)
                        }
                    }

                    // Stroke lines
                    drawPath(
                        path = inPath,
                        color = CleanMinimalGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = outPath,
                        color = CleanMinimalPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw points on data coordinates
                    days.forEachIndexed { index, triple ->
                        val x = index * stepX
                        val inY = h - (triple.second / maxVal * h * animatedRatio)
                        val outY = h - (triple.third / maxVal * h * animatedRatio)

                        drawCircle(color = CleanMinimalGreen, radius = 4.dp.toPx(), center = Offset(x, inY))
                        drawCircle(color = CleanMinimalPrimary, radius = 4.dp.toPx(), center = Offset(x, outY))
                    }
                }
            }

            // X-axis Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach {
                    Text(
                        text = it.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = CleanMinimalTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Category Stock Breakdown (Custom Donut/Distribution Chart in Clean Minimalism)
 */
@Composable
fun CategoryDistributionChart(
    items: List<InventoryItem>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = items.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.quantity } }
        .filter { it.value > 0 }

    val totalStock = categoryTotals.values.sum().coerceAtLeast(1)
    val colors = listOf(
        CleanMinimalPrimary,
        CleanMinimalGreen,
        CleanMinimalOnPrimaryContainer,
        Color(0xFFE91E63),
        Color(0xFF00897B),
        Color(0xFFE65100)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, CleanMinimalBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Stock Distribution by Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CleanMinimalTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        var startAngle = -90f
                        val strokeWidth = 18.dp.toPx()

                        categoryTotals.entries.forEachIndexed { index, entry ->
                            val sweepAngle = (entry.value.toFloat() / totalStock) * 360f
                            val sliceColor = colors[index % colors.size]

                            drawArc(
                                color = sliceColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                size = Size(size.width, size.height)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalStock",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CleanMinimalTextPrimary
                        )
                        Text(
                            text = "Units",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanMinimalTextSecondary
                        )
                    }
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryTotals.entries.take(4).forEachIndexed { index, entry ->
                        val percent = (entry.value * 100) / totalStock
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(colors[index % colors.size], CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${entry.key} ($percent%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanMinimalTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
