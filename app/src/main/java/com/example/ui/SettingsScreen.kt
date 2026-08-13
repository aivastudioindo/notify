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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    hasNotificationAccess: Boolean,
    isPinProtectionEnabled: Boolean,
    isVaultUnlocked: Boolean,
    themeMode: ThemeMode,
    onOpenNotificationSettings: () -> Unit,
    onOpenSetPinDialog: () -> Unit,
    onDisablePin: () -> Unit,
    onLockVault: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSendTestNotification: (String, String, String) -> Unit,
    onInsertSampleData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Notification Service
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MinimalCardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Layanan Perekam Notifikasi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Status izin listener sistem Android",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasNotificationAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasNotificationAccess) Color(0xFF25D366) else MinimalLavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasNotificationAccess) "Izin Aktif Berjalan" else "Izin Belum Diberikan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalTextPrimary
                            )
                        }

                        Button(
                            onClick = onOpenNotificationSettings,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasNotificationAccess) MinimalCardBackground else MinimalLavenderPrimary,
                                contentColor = if (hasNotificationAccess) MinimalTextPrimary else Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (hasNotificationAccess) "Pengaturan" else "Izinkan",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Section: Data Privacy & Encryption
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MinimalCardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Enkripsi & Keamanan Privasi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Standar keamanan database tingkat militer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = MinimalCardBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "• Enkripsi Kolom: AES-256 GCM authenticated",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                            Text(
                                text = "• Kunci Enkripsi: Android KeyStore hardware-backed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                            Text(
                                text = "• Zero Cloud Upload: 100% tersimpan offline lokal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MinimalBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Protection Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kunci PIN Brankas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = if (isPinProtectionEnabled) "PIN 4-digit aktif melindungi pesan sensitif" else "Proteksi PIN dinonaktifkan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }

                        Switch(
                            checked = isPinProtectionEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    onOpenSetPinDialog()
                                } else {
                                    onDisablePin()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MinimalDarkBackground,
                                checkedTrackColor = MinimalLavenderPrimary,
                                uncheckedThumbColor = MinimalTextMuted,
                                uncheckedTrackColor = MinimalCardBackground
                            )
                        )
                    }

                    if (isPinProtectionEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenSetPinDialog,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MinimalBorder)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MinimalTextPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ubah PIN", style = MaterialTheme.typography.labelMedium, color = MinimalTextPrimary)
                            }

                            if (isVaultUnlocked) {
                                OutlinedButton(
                                    onClick = onLockVault,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MinimalBorder)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MinimalLavenderPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kunci Sekarang", style = MaterialTheme.typography.labelMedium, color = MinimalLavenderPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Theme & Appearance
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MinimalCardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tema & Tampilan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Clean Minimalism Charcoal Theme",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetThemeMode(mode) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MinimalLavenderPrimary,
                                    unselectedColor = MinimalTextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (mode) {
                                    ThemeMode.DARK -> "Mode Gelap (Minimal Charcoal)"
                                    ThemeMode.LIGHT -> "Mode Terang (Clean Lavender)"
                                    ThemeMode.SYSTEM -> "Mengikuti Sistem Perangkat"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MinimalTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section: Notification Simulator & Test Data
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MinimalCardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Uji Coba & Generator Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Kirim notifikasi tiruan untuk mencoba perekaman",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSendTestNotification(
                                    "BCA Mobile",
                                    "Kode OTP Anda adalah 829104. JANGAN BERIKAN KEPADA SIAPAPUN.",
                                    "Verifikasi Transaksi"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MinimalBorder)
                        ) {
                            Text("OTP Bank", style = MaterialTheme.typography.labelSmall, color = MinimalTextPrimary)
                        }

                        OutlinedButton(
                            onClick = {
                                onSendTestNotification(
                                    "WhatsApp Business",
                                    "Halo, pesanan Anda dengan nomor #NV-9281 telah dikirim oleh kurir.",
                                    "+62 812-3456-7890"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MinimalBorder)
                        ) {
                            Text("Chat WA", style = MaterialTheme.typography.labelSmall, color = MinimalTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onInsertSampleData,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MinimalBorder)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = MinimalTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Muat Data Sampel Historis (Grafik Lengkap)", style = MaterialTheme.typography.labelMedium, color = MinimalTextPrimary)
                    }
                }
            }
        }

        // Section: Danger Zone
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalRose.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Area Hapus Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalRoseText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Semua riwayat notifikasi dan enkripsi lokal akan dihapus secara permanen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onClearAllData,
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MinimalRoseText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus Seluruh Database Notifikasi", style = MaterialTheme.typography.labelMedium, color = MinimalRoseText)
                    }
                }
            }
        }
    }
}
