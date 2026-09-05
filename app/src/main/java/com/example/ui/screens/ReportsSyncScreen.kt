package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItem
import com.example.data.sync.CloudSyncState
import com.example.ui.components.BackendGuideDialog
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
import kotlinx.coroutines.launch

@Composable
fun ReportsSyncScreen(
    items: List<InventoryItem>,
    cloudSyncState: CloudSyncState,
    onTriggerSync: () -> Unit,
    onToggleOnlineStatus: () -> Unit,
    onUpdateEndpoint: (String) -> Unit,
    onGenerateReport: suspend (category: String?, lowStockOnly: Boolean) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showBackendDialog by remember { mutableStateOf(false) }
    var selectedReportCategory by remember { mutableStateOf("All") }
    var reportLowStockOnly by remember { mutableStateOf(false) }
    var showPayloadInspector by remember { mutableStateOf(false) }

    var endpointInput by remember { mutableStateOf(cloudSyncState.endpointUrl) }
    var generatedPreviewCsv by remember { mutableStateOf("") }

    val categories = listOf("All", "Electronics", "Packaging", "Warehouse", "Safety")

    LaunchedEffect(items, selectedReportCategory, reportLowStockOnly) {
        generatedPreviewCsv = onGenerateReport(
            if (selectedReportCategory == "All") null else selectedReportCategory,
            reportLowStockOnly
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Real-Time Cloud Sync Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cloud_sync_hub_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CleanMinimalBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with Online/Offline Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (cloudSyncState.isOnline) CleanMinimalGreenContainer else CleanMinimalRedContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (cloudSyncState.isOnline) Icons.Default.CloudSync else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (cloudSyncState.isOnline) CleanMinimalGreen else CleanMinimalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Cloud Dashboard Sync",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CleanMinimalTextPrimary
                                )
                                Text(
                                    text = if (cloudSyncState.isOnline) "Real-time bidirectional link" else "Offline Mode (Local Storage Active)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (cloudSyncState.isOnline) CleanMinimalGreen else CleanMinimalRed
                                )
                            }
                        }

                        // Offline Mode Switch Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (cloudSyncState.isOnline) "Online" else "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanMinimalTextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = cloudSyncState.isOnline,
                                onCheckedChange = { onToggleOnlineStatus() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CleanMinimalPrimary,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = CleanMinimalBorder
                                ),
                                modifier = Modifier.testTag("offline_mode_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync Status message & Pending count
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CleanMinimalBg, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Local Queue:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanMinimalTextSecondary
                                )
                                Text(
                                    text = "${cloudSyncState.pendingCount} pending updates",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (cloudSyncState.pendingCount > 0) CleanMinimalRed else CleanMinimalGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cloudSyncState.lastSyncMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = CleanMinimalTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cloud Endpoint URL input
                    OutlinedTextField(
                        value = endpointInput,
                        onValueChange = {
                            endpointInput = it
                            onUpdateEndpoint(it)
                        },
                        label = { Text("Cloud Endpoint URL", color = CleanMinimalTextSecondary) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CleanMinimalPrimary,
                            unfocusedBorderColor = CleanMinimalBorder,
                            focusedTextColor = CleanMinimalTextPrimary,
                            unfocusedTextColor = CleanMinimalTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("endpoint_url_field"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sync Now Button & Payload Inspector toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTriggerSync,
                            enabled = !cloudSyncState.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_now_button")
                        ) {
                            if (cloudSyncState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showPayloadInspector = !showPayloadInspector },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CleanMinimalBorder),
                            modifier = Modifier.testTag("toggle_payload_inspector")
                        ) {
                            Icon(
                                imageVector = if (showPayloadInspector) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = CleanMinimalTextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inspect JSON", color = CleanMinimalTextPrimary)
                        }
                    }

                    // Cloud Payload Inspector view
                    AnimatedVisibility(visible = showPayloadInspector) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "Real-time Transmitted JSON Payload:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = CleanMinimalPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CleanMinimalBg, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = cloudSyncState.lastSyncPayloadJson.ifBlank {
                                        "{\n  \"status\": \"idle\",\n  \"info\": \"Tap 'Sync Now' to transmit pending items and stream payload\"\n}"
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = CleanMinimalTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Customizable Exportable Reports
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_reports_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CleanMinimalBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Customizable Inventory Reports",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinimalTextPrimary
                    )
                    Text(
                        text = "Export and share real-time inventory spreadsheets, audits, and low-stock alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinimalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filters: Category
                    Text("Filter by Category:", style = MaterialTheme.typography.labelMedium, color = CleanMinimalTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.take(3).forEach { category ->
                            val isSelected = selectedReportCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedReportCategory = category },
                                label = { Text(category, fontSize = 11.sp) },
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
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter: Low Stock Only Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Export Low Stock & Reorder Items Only",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanMinimalTextPrimary
                        )
                        Switch(
                            checked = reportLowStockOnly,
                            onCheckedChange = { reportLowStockOnly = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CleanMinimalPrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = CleanMinimalBorder
                            ),
                            modifier = Modifier.testTag("report_low_stock_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CSV Preview snippet
                    Text(
                        text = "Report Preview (CSV):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = CleanMinimalPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(CleanMinimalBg, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = generatedPreviewCsv.take(400) + if (generatedPreviewCsv.length > 400) "\n..." else "",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = CleanMinimalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (Share, Copy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Inventory Report CSV", generatedPreviewCsv)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CleanMinimalBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_csv_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CleanMinimalTextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy CSV", color = CleanMinimalTextPrimary)
                        }

                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Wafa Inventory Report (${selectedReportCategory})")
                                    putExtra(Intent.EXTRA_TEXT, generatedPreviewCsv)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Inventory Report"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_csv_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export / Share", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: Developer & Cloud Guides
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CleanMinimalBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Deployment & Backend Instructions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinimalTextPrimary
                    )
                    Text(
                        text = "Step-by-step documentation for Node.js, Firebase, PostgreSQL, and GitHub APK generation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CleanMinimalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showBackendDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimaryContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_backend_guide_button")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = CleanMinimalOnPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Backend Database & GitHub Guide",
                            color = CleanMinimalOnPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showBackendDialog) {
        BackendGuideDialog(onDismiss = { showBackendDialog = false })
    }
}
