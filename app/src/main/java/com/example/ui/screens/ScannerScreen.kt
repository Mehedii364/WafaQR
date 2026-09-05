package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.InventoryItem
import com.example.scanner.BarcodeResult
import com.example.scanner.CameraBarcodeAnalyzer
import com.example.scanner.QrGenerator
import com.example.ui.components.AddEditItemDialog
import com.example.ui.components.QuickStockDialog
import com.example.ui.components.WafaQrDialog
import com.example.ui.theme.CleanMinimalBg
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalGreen
import com.example.ui.theme.CleanMinimalLaser
import com.example.ui.theme.CleanMinimalOnPrimaryContainer
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalRed
import com.example.ui.theme.CleanMinimalRedContainer
import com.example.ui.theme.CleanMinimalSurface
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.InputStream
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    items: List<InventoryItem>,
    lastScannedBarcode: BarcodeResult?,
    onBarcodeDetected: (BarcodeResult) -> Unit,
    onAnalyzeImage: (Bitmap) -> BarcodeResult?,
    onQuickStock: (sku: String, delta: Int, reason: String) -> Unit,
    onSaveItem: (InventoryItem) -> Unit,
    onClearScannedBarcode: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onTriggerSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControl: androidx.camera.core.CameraControl? by remember { mutableStateOf(null) }

    // Dialogs state
    var showQuickStockDialogForItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showAddDialogWithBarcode by remember { mutableStateOf<String?>(null) }
    var showWafaQrForItem by remember { mutableStateOf<InventoryItem?>(null) }

    // Item counts for Clean Minimalism Header & Metrics
    val lowStockCount = remember(items) {
        items.count { it.quantity <= it.minStockLevel }
    }
    val totalItemsCount = remember(items) {
        items.size
    }

    // Matching item for current barcode
    val matchedItem = remember(lastScannedBarcode, items) {
        lastScannedBarcode?.let { scanned ->
            items.find { it.sku.equals(scanned.text, ignoreCase = true) }
        }
    }

    // Photo picker for Barcode Image Analysis
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    onAnalyzeImage(bitmap)
                }
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header (Clean Minimalism Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Wafa QR",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = CleanMinimalPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CleanMinimalGreen, CircleShape)
                        )
                        Text(
                            text = "CLOUD SYNCED • OFFLINE READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = CleanMinimalTextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sync Button
                    IconButton(
                        onClick = onTriggerSync,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CleanMinimalPrimaryContainer)
                            .testTag("scanner_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Trigger Sync",
                            tint = CleanMinimalOnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Wafa Avatar Pill
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CleanMinimalPrimaryContainer)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CleanMinimalPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "W",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Top Stat Metric Cards (Clean Minimalism Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Low Stock Alerts Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanMinimalPrimaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Low Stock Alerts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CleanMinimalOnPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$lowStockCount",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = CleanMinimalOnPrimaryContainer
                            )
                            if (lowStockCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CleanMinimalRedContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+$lowStockCount",
                                        color = CleanMinimalRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Total Items Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, CleanMinimalBorder),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CleanMinimalTextSecondary
                        )
                        Text(
                            text = "$totalItemsCount",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleanMinimalTextPrimary
                        )
                    }
                }
            }

            // Viewfinder Container (Clean Minimalism Styled Viewfinder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(4.dp, Color.White),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (cameraPermissionState.status.isGranted) {
                        // Live Camera View
                        CameraPreviewWithScanner(
                            onBarcodeDetected = onBarcodeDetected,
                            torchEnabled = torchEnabled,
                            onCameraReady = { cam ->
                                cameraControl = cam.cameraControl
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Camera Permission Request UI
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(CleanMinimalPrimaryContainer.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = CleanMinimalPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Camera Access Required",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Grant camera permission to scan barcodes and Wafa QR tags.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { cameraPermissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                                modifier = Modifier.testTag("request_camera_permission_button")
                            ) {
                                Text("Enable Camera", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Inner gradient overlay from bottom
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                )
                            )
                    )

                    // Animated Laser Reticle (Center)
                    ScannerReticleOverlay(modifier = Modifier.align(Alignment.Center))

                    // Top Viewfinder Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "AUTO-FOCUS ACTIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (cameraPermissionState.status.isGranted) {
                            IconButton(
                                onClick = {
                                    torchEnabled = !torchEnabled
                                    cameraControl?.enableTorch(torchEnabled)
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(CleanMinimalPrimaryContainer)
                                    .testTag("toggle_torch_button")
                            ) {
                                Icon(
                                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Toggle Torch",
                                    tint = CleanMinimalOnPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Bottom Barcode Image Analysis Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CleanMinimalPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .align(Alignment.BottomCenter)
                            .testTag("analyze_image_button")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyze Barcode Image",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Test Barcodes Row (For testing convenience)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Test:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CleanMinimalTextSecondary,
                    modifier = Modifier.padding(end = 6.dp)
                )
                items.take(3).forEach { testItem ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CleanMinimalPrimaryContainer)
                            .clickable {
                                onBarcodeDetected(
                                    BarcodeResult(
                                        text = testItem.sku,
                                        format = testItem.barcodeFormat
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("sample_scan_${testItem.sku}")
                    ) {
                        Text(
                            text = testItem.sku.takeLast(6),
                            color = CleanMinimalOnPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stock Trend (7D) Card (Clean Minimalism Aesthetic)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, CleanMinimalBorder),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STOCK TREND (7D)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CleanMinimalTextSecondary
                        )
                        Text(
                            text = "View Analytics",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CleanMinimalPrimary,
                            modifier = Modifier.clickable { onNavigateToAnalytics() }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Minimal 7D vertical bars representation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val barHeights = listOf(0.40f, 0.60f, 0.45f, 0.85f, 0.95f, 0.70f, 0.80f)
                        barHeights.forEachIndexed { idx, frac ->
                            val isHighlight = idx == 4
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(frac)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (isHighlight) CleanMinimalPrimary else CleanMinimalPrimaryContainer)
                            )
                        }
                    }
                }
            }

            // Scanned Result Floating Action Card (When barcode detected)
            AnimatedVisibility(visible = lastScannedBarcode != null) {
                if (lastScannedBarcode != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_result_sheet"),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, CleanMinimalBorder),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(CleanMinimalGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Barcode Detected (${lastScannedBarcode.format})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CleanMinimalPrimary
                                    )
                                }
                                IconButton(onClick = onClearScannedBarcode, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                                }
                            }

                            Text(
                                text = lastScannedBarcode.text,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CleanMinimalTextPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (matchedItem != null) {
                                // Item exists in local Inventory
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CleanMinimalPrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = matchedItem.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = CleanMinimalTextPrimary
                                            )
                                            Text(
                                                text = "Stock: ${matchedItem.quantity} units | Loc: ${matchedItem.location}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CleanMinimalTextSecondary
                                            )
                                        }
                                        IconButton(onClick = { showWafaQrForItem = matchedItem }) {
                                            Icon(Icons.Default.QrCode, contentDescription = "Wafa QR", tint = CleanMinimalPrimary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Stock In (+1), Stock Out (-1), or Detailed Stock Action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onQuickStock(matchedItem.sku, 1, "Quick Scanner Scan In") },
                                        border = BorderStroke(1.dp, CleanMinimalGreen.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("scanner_stock_in_quick")
                                    ) {
                                        Text("+1 In", color = CleanMinimalGreen, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onQuickStock(matchedItem.sku, -1, "Quick Scanner Scan Out") },
                                        border = BorderStroke(1.dp, CleanMinimalRed.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f).testTag("scanner_stock_out_quick")
                                    ) {
                                        Text("-1 Out", color = CleanMinimalRed, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showQuickStockDialogForItem = matchedItem },
                                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.2f).testTag("scanner_stock_custom_quick")
                                    ) {
                                        Text("Adjust...", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // New unrecognized barcode
                                Text(
                                    text = "Item not yet registered in local warehouse inventory.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanMinimalTextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showAddDialogWithBarcode = lastScannedBarcode.text },
                                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("register_new_barcode_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Register New Product", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    showQuickStockDialogForItem?.let { item ->
        QuickStockDialog(
            item = item,
            onConfirm = { delta, reason ->
                onQuickStock(item.sku, delta, reason)
                showQuickStockDialogForItem = null
            },
            onDismiss = { showQuickStockDialogForItem = null }
        )
    }

    showAddDialogWithBarcode?.let { barcode ->
        AddEditItemDialog(
            initialBarcode = barcode,
            onSave = { newItem ->
                onSaveItem(newItem)
                showAddDialogWithBarcode = null
            },
            onDismiss = { showAddDialogWithBarcode = null }
        )
    }

    showWafaQrForItem?.let { item ->
        WafaQrDialog(
            item = item,
            onDismiss = { showWafaQrForItem = null }
        )
    }
}

/**
 * CameraX Preview integration with ImageAnalysis Analyzer
 */
@Composable
private fun CameraPreviewWithScanner(
    onBarcodeDetected: (BarcodeResult) -> Unit,
    torchEnabled: Boolean,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor, CameraBarcodeAnalyzer(onBarcodeDetected))
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    onCameraReady(camera)
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

/**
 * Animated Optical Laser Scanner Reticle Overlay (Clean Minimalism red optical laser beam)
 */
@Composable
private fun ScannerReticleOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "reticle_anim")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .border(2.dp, CleanMinimalPrimaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val laserY = h * laserPosition

            // Horizontal optical laser scanline with glow (red-500/80 shadow-[0_0_10px_rgba(239,68,68,0.5)])
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        CleanMinimalLaser.copy(alpha = 0.8f),
                        Color.White,
                        CleanMinimalLaser.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, laserY),
                end = Offset(w, laserY),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

