package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CleanMinimalBg
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@Composable
fun BackendGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Node.js / REST", "Firebase Setup", "SQL Schema", "GitHub / APK")

    val nodeJsCode = """
// server.js - Wafa Inventory Cloud Sync Backend
const express = require('express');
const cors = require('cors');
const app = express();

app.use(cors());
app.use(express.json());

// In-Memory or PostgreSQL/MongoDB store
let inventoryStore = new Map();
let auditLogs = [];

// 1. Health check & latency ping
app.get('/api/v1/health', (req, res) => {
    res.json({ status: 'ONLINE', timestamp: Date.now() });
});

// 2. Real-time incremental sync endpoint
app.post('/api/v1/sync', (req, res) => {
    const { deviceId, items, transactions } = req.body;
    console.log("Sync request from " + deviceId + ": " + items.length + " items");

    items.forEach(item => {
        inventoryStore.set(item.sku, { ...item, serverUpdatedAt: Date.now() });
    });

    if (transactions) {
        auditLogs.push(...transactions);
    }

    res.json({
        success: true,
        syncedCount: items.length,
        serverTime: Date.now(),
        message: 'Sync completed successfully'
    });
});

// 3. Web Dashboard inventory feed
app.get('/api/v1/inventory', (req, res) => {
    res.json(Array.from(inventoryStore.values()));
});

app.listen(3000, () => console.log('Wafa Backend running on port 3000'));
""".trimIndent()

    val firebaseInstructions = """
1. Open Google Firebase Console (firebase.google.com).
2. Create or select your project.
3. Enable 'Cloud Firestore Database' in production mode.
4. Security Rules for Inventory:
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /inventory/{itemSku} {
      allow read, write: if request.auth != null;
    }
    match /transactions/{txId} {
      allow read, create: if request.auth != null;
    }
  }
}
5. Download 'google-services.json' into the app/ directory.
6. The app automatically caches data locally and streams real-time updates!
""".trimIndent()

    val sqlSchema = """
-- PostgreSQL / Supabase Schema for Inventory Tracking
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) DEFAULT 'General',
    quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 5,
    unit_price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    cost_price NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    location VARCHAR(128),
    supplier VARCHAR(128),
    notes TEXT,
    last_updated BIGINT NOT NULL
);

CREATE TABLE stock_transactions (
    id BIGSERIAL PRIMARY KEY,
    item_sku VARCHAR(64) REFERENCES inventory_items(sku) ON DELETE CASCADE,
    type VARCHAR(32) NOT NULL, -- STOCK_IN, STOCK_OUT
    quantity_change INT NOT NULL,
    new_quantity INT NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_sku ON inventory_items(sku);
CREATE INDEX idx_inventory_category ON inventory_items(category);
""".trimIndent()

    val githubApkGuide = """
# Pushing to GitHub and Building Release APK:

1. Push code to GitHub:
   git init
   git add .
   git commit -m "feat: complete Wafa inventory scanner app"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/wafa-inventory.git
   git push -u origin main

2. Build APK locally with Gradle:
   gradle :app:assembleRelease
   # Output APK located at: app/build/outputs/apk/release/app-release.apk

3. GitHub Actions Automatic APK Build Workflow (.github/workflows/build-apk.yml):
   - Triggers on every push to main
   - Sets up Java 17, builds release APK, and publishes the APK as a GitHub Release artifact!
""".trimIndent()

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("backend_guide_dialog"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CleanMinimalBorder),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = CleanMinimalPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cloud Backend Setup",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = CleanMinimalTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("backend_guide_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CleanMinimalTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CleanMinimalBg,
                    contentColor = CleanMinimalPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CleanMinimalPrimary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) CleanMinimalPrimary else CleanMinimalTextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val currentContent = when (selectedTab) {
                    0 -> Pair(nodeJsCode, "Node.js Express Server Code")
                    1 -> Pair(firebaseInstructions, "Firebase Firestore Guide")
                    2 -> Pair(sqlSchema, "PostgreSQL SQL Schema")
                    else -> Pair(githubApkGuide, "GitHub & APK Instructions")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CleanMinimalBg, RoundedCornerShape(12.dp))
                        .border(1.dp, CleanMinimalBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = currentContent.first,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CleanMinimalTextPrimary,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { copyToClipboard(currentContent.first, currentContent.second) },
                    colors = ButtonDefaults.buttonColors(containerColor = CleanMinimalPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("copy_backend_code_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy ${currentContent.second}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
