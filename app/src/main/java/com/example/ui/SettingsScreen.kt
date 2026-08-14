package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalEmerald
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalRose
import com.example.ui.theme.MinimalRoseText
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@Composable
fun SettingsScreen(
    hasNotificationAccess: Boolean,
    hasBackgroundLocationAccess: Boolean = true,
    isIgnoringBatteryOptimizations: Boolean = true,
    onOpenNotificationSettings: () -> Unit,
    onRequestBackgroundPermissions: () -> Unit = {},
    onRequestBatteryOptimization: () -> Unit = {},
    onOpenAutostart: () -> Unit = {},
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
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
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = MinimalLavenderPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sistem & Pemeliharaan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Konfigurasi izin latar belakang dan pemeliharaan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Background Access & Power Management Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IZIN AKSES LATAR BELAKANG (24/7)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Agar sistem dapat merespons perintah Telegram (/lokasi) dan merekam notifikasi saat HP terkunci atau layar mati, aktifkan semua izin latar belakang di bawah ini:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: Background Location Permission
                    Surface(
                        color = MinimalSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (hasBackgroundLocationAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (hasBackgroundLocationAccess) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "GPS Lokasi Latar Belakang",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalTextPrimary
                                    )
                                    Text(
                                        text = if (hasBackgroundLocationAccess) "Diizinkan 24/7 saat layar mati" else "Perlu Izin Akses Latar Belakang",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (hasBackgroundLocationAccess) MinimalEmerald else MinimalRoseText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Battery Optimization Status
                    Surface(
                        color = MinimalSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (isIgnoringBatteryOptimizations) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isIgnoringBatteryOptimizations) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Penghemat Baterai System",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalTextPrimary
                                    )
                                    Text(
                                        text = if (isIgnoringBatteryOptimizations) "Penghemat Baterai Diabaikan (Unrestricted)" else "Masih Dibatasi Penghemat Baterai",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isIgnoringBatteryOptimizations) MinimalEmerald else MinimalRoseText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRequestBackgroundPermissions,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalLavenderPrimary,
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Minta Izin Latar Belakang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onRequestBatteryOptimization,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary),
                            border = BorderStroke(1.dp, MinimalBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Matikan Penghemat Baterai", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenAutostart,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalLavenderPrimary),
                        border = BorderStroke(1.dp, MinimalLavenderPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buka Pengaturan Mulai Otomatis (Autostart OEM)", fontSize = 11.sp)
                    }
                }
            }
        }

        // Notification Access Service Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LAYANAN PENDENGAR NOTIFIKASI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MinimalSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasNotificationAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (hasNotificationAccess) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Akses Notifikasi Android",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalTextPrimary
                                    )
                                    Text(
                                        text = if (hasNotificationAccess) "Layanan Berjalan Aktif" else "Izin Belum Aktif",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (hasNotificationAccess) MinimalEmerald else MinimalRoseText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenNotificationSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalLavenderPrimary,
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasNotificationAccess) "Cek Pengaturan Izin Notifikasi" else "Buka Pengaturan & Izinkan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Database Maintenance & Clear Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANAJEMEN PENYIMPANAN DATA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Data rekaman disimpan secara lokal di memori internal HP anak dalam database Room yang terenkripsi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalRoseText),
                        border = BorderStroke(1.dp, MinimalRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus Semua Riwayat Notifikasi", fontSize = 12.sp)
                    }
                }
            }
        }

        // App Information
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MinimalLavenderPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Famly Parental Control & Monitor",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Versi 1.4.0 • Keamanan & Privasi Terjamin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Hapus Semua Data?",
                    fontWeight = FontWeight.Bold,
                    color = MinimalTextPrimary
                )
            },
            text = {
                Text(
                    text = "Tindakan ini akan menghapus seluruh rekaman notifikasi dari database lokal HP anak secara permanen.",
                    color = MinimalTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalRose, contentColor = Color.White)
                ) {
                    Text("Hapus Semua")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = MinimalTextMuted)
                }
            },
            containerColor = MinimalSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
