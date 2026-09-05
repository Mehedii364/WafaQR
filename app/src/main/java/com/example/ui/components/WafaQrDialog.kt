package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InventoryItem
import com.example.scanner.QrGenerator
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary
import java.util.Locale

@Composable
fun WafaQrDialog(
    item: InventoryItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Generate Wafa QR Bitmap with dark minimalist ink
    val qrBitmap: Bitmap? = remember(item.sku) {
        QrGenerator.generateQrBitmap(
            content = item.sku,
            width = 400,
            height = 400,
            darkColor = android.graphics.Color.parseColor("#1D1B20"),
            lightColor = android.graphics.Color.WHITE
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("wafa_qr_dialog"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CleanMinimalBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CleanMinimalPrimaryContainer, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = CleanMinimalPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Wafa QR Label",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CleanMinimalTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_qr_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CleanMinimalTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item info label
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = CleanMinimalTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${item.category} • Loc: ${item.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleanMinimalTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // High-Contrast QR Code container
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, CleanMinimalBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Wafa QR Code for ${item.name}",
                            modifier = Modifier.size(196.dp)
                        )
                    } else {
                        Text(
                            text = "Unable to render QR",
                            color = CleanMinimalTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SKU Code Display
                Text(
                    text = item.sku,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = CleanMinimalPrimary
                )

                Text(
                    text = String.format(Locale.US, "$%.2f | Current Stock: %d", item.unitPrice, item.quantity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CleanMinimalTextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Copy SKU and Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Barcode SKU", item.sku)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "SKU copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CleanMinimalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_sku_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CleanMinimalTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy SKU", color = CleanMinimalTextPrimary)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Wafa QR Inventory Tag: ${item.name}"
                                )
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Wafa Inventory Item Tag:\n" +
                                            "Item: ${item.name}\n" +
                                            "SKU / Barcode: ${item.sku}\n" +
                                            "Category: ${item.category}\n" +
                                            "Unit Price: $${item.unitPrice}\n" +
                                            "Location: ${item.location}\n" +
                                            "In-Stock: ${item.quantity} units"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Wafa QR Tag"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_qr_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Tag", color = Color.White)
                    }
                }
            }
        }
    }
}
