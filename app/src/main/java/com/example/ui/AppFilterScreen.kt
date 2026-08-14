package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.filter.AppFilterMode
import com.example.data.filter.AppItem
import com.example.ui.components.AppSelectionDialog
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalRose
import com.example.ui.theme.MinimalRoseText
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@Composable
fun AppFilterScreen(
    filterMode: AppFilterMode,
    blacklist: Set<String>,
    whitelist: Set<String>,
    onSetFilterMode: (AppFilterMode) -> Unit,
    onAddToBlacklist: (String) -> Unit,
    onRemoveFromBlacklist: (String) -> Unit,
    onAddToWhitelist: (String) -> Unit,
    onRemoveFromWhitelist: (String) -> Unit,
    onResetFilterDefaults: () -> Unit,
    onGetInstalledApps: () -> List<AppItem>,
    onGetAppName: (String) -> String,
    modifier: Modifier = Modifier
) {
    var showAppSelectionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MinimalLavenderPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
                                    contentDescription = null,
                                    tint = MinimalLavenderPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Filter Aplikasi & Efisiensi Baterai",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Pilih aplikasi yang ingin dipantau atau diblokir",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "💡 Tips Baterai Awet: Gunakan mode Whitelist untuk hanya merekam aplikasi penting (misal: WhatsApp, Telegram, Bank, SMS) agar CPU HP anak tidak bekerja terus-menerus memproses spam notifikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Mode Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODE PENYARINGAN NOTIFIKASI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option: Blacklist
                    ModeSelectionRow(
                        title = "Mode Blacklist (Blokir Aplikasi Terpilih)",
                        description = "Merekam semua notifikasi, KECUALI aplikasi di daftar blacklist (misal: YouTube, Game, Iklan).",
                        isSelected = filterMode == AppFilterMode.BLACKLIST,
                        onClick = { onSetFilterMode(AppFilterMode.BLACKLIST) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option: Whitelist
                    ModeSelectionRow(
                        title = "Mode Whitelist (Hanya Rekam Aplikasi Terpilih)",
                        description = "HANYA merekam aplikasi di daftar whitelist. Sangat hemat baterai dan memori.",
                        isSelected = filterMode == AppFilterMode.WHITELIST,
                        onClick = { onSetFilterMode(AppFilterMode.WHITELIST) }
                    )
                }
            }
        }

        // Active List Card
        item {
            val isBlacklistMode = filterMode == AppFilterMode.BLACKLIST
            val currentList = if (isBlacklistMode) blacklist else whitelist

            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBlacklistMode) "DAFTAR BLACKLIST (${blacklist.size})" else "DAFTAR WHITELIST (${whitelist.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isBlacklistMode) MinimalRoseText else MinimalLavenderPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isBlacklistMode) "Aplikasi yang diabaikan" else "Aplikasi yang dipantau",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }

                        Button(
                            onClick = { showAppSelectionDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalLavenderPrimary,
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalSurfaceElevated, RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBlacklistMode) "Belum ada aplikasi yang di-blacklist." else "Belum ada aplikasi dalam whitelist. Tambahkan aplikasi agar rekaman berjalan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextMuted
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentList.forEach { pkg ->
                                val appName = onGetAppName(pkg)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MinimalSurfaceElevated, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = appName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MinimalTextPrimary
                                        )
                                        Text(
                                            text = pkg,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            color = MinimalTextMuted
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (isBlacklistMode) onRemoveFromBlacklist(pkg) else onRemoveFromWhitelist(pkg)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus",
                                            tint = MinimalRoseText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onResetFilterDefaults,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextSecondary),
                        border = BorderStroke(1.dp, MinimalBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset ke Rekomendasi Bawaan", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showAppSelectionDialog) {
        val installedApps = remember { onGetInstalledApps() }
        val currentSelected = if (filterMode == AppFilterMode.BLACKLIST) blacklist else whitelist

        AppSelectionDialog(
            targetMode = filterMode,
            existingPackages = currentSelected,
            installedApps = installedApps,
            onAddApp = { pkg ->
                if (filterMode == AppFilterMode.BLACKLIST) {
                    onAddToBlacklist(pkg)
                } else {
                    onAddToWhitelist(pkg)
                }
            },
            onDismiss = { showAppSelectionDialog = false }
        )
    }
}

@Composable
private fun ModeSelectionRow(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MinimalLavenderPrimary.copy(alpha = 0.15f) else MinimalSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (isSelected) MinimalLavenderPrimary else MinimalBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MinimalLavenderPrimary else Color.Transparent)
                    .then(
                        if (!isSelected) Modifier.background(Color.Transparent, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF381E72))
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MinimalLavenderPrimary else MinimalTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MinimalTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
