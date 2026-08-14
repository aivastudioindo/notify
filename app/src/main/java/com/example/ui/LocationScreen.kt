package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
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
fun LocationScreen(
    hasLocationPermission: Boolean,
    isGpsEnabled: Boolean,
    currentLocationState: String?,
    isFetchingLocation: Boolean,
    onTestLocation: ((String) -> Unit) -> Unit,
    onSendLocationToTelegram: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var permissionGranted by remember(hasLocationPermission) { mutableStateOf(hasLocationPermission) }
    var backgroundPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var locationResultText by remember(currentLocationState) { mutableStateOf(currentLocationState) }
    var telegramSendStatus by remember { mutableStateOf<String?>(null) }
    var isSendingTelegram by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permissionGranted = fine || coarse
    }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        backgroundPermissionGranted = isGranted
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Status Card
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
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MinimalLavenderPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Lokasi & GPS HP Anak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Pelacakan posisi real-time via bot Telegram",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Ketika orang tua mengirim perintah /lokasi di bot Telegram, HP anak akan otomatis membaca koordinat GPS dan mengirimkan tautan Google Maps secara akurat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Sensor & Permission Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STATUS SENSOR & IZIN SISTEM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Permission Status Row
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
                                    imageVector = if (permissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (permissionGranted) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Izin Lokasi (GPS)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalTextPrimary
                                    )
                                    Text(
                                        text = if (permissionGranted) "Telah diberikan (Akurat)" else "Belum diberikan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (permissionGranted) MinimalEmerald else MinimalRoseText
                                    )
                                }
                            }

                            if (!permissionGranted) {
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MinimalLavenderPrimary, contentColor = Color(0xFF381E72)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Izinkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Background Location Permission Row
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
                                    imageVector = if (backgroundPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (backgroundPermissionGranted) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Akses Lokasi Latar Belakang (24/7)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalTextPrimary
                                    )
                                    Text(
                                        text = if (backgroundPermissionGranted) "Diizinkan (Siap respon Telegram saat lock)" else "Perlu izin 'Izinkan Setiap Saat'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (backgroundPermissionGranted) MinimalEmerald else MinimalRoseText
                                    )
                                }
                            }

                            if (!backgroundPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                Button(
                                    onClick = {
                                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MinimalLavenderPrimary, contentColor = Color(0xFF381E72)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Izinkan 24/7", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // GPS Hardware Status Row
                    Surface(
                        color = MinimalSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isGpsEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isGpsEnabled) MinimalEmerald else MinimalRoseText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Layanan Lokasi (GPS Hardware)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = if (isGpsEnabled) "GPS Aktif & Siap Menerima Sinyal" else "GPS Nonaktif di Pengaturan HP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isGpsEnabled) MinimalEmerald else MinimalRoseText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Test & GPS Fix Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "UJI COBA PENGAMBILAN LOKASI SEKARANG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onTestLocation { res ->
                                    locationResultText = res
                                }
                            },
                            enabled = !isFetchingLocation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalLavenderPrimary,
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isFetchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color(0xFF381E72),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mencari GPS...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tes Ambil GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                isSendingTelegram = true
                                onSendLocationToTelegram { res ->
                                    isSendingTelegram = false
                                    telegramSendStatus = res
                                }
                            },
                            enabled = !isSendingTelegram,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary),
                            border = BorderStroke(1.dp, MinimalBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSendingTelegram) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MinimalLavenderPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kirim ke Telegram", fontSize = 12.sp)
                            }
                        }
                    }

                    // Result Display Box
                    if (locationResultText != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val isError = locationResultText!!.contains("ERROR", ignoreCase = true) || locationResultText!!.contains("Gagal", ignoreCase = true)

                        Surface(
                            color = if (isError) MinimalRose.copy(alpha = 0.15f) else MinimalSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isError) MinimalRose else MinimalBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isError) "HASIL PENGUJIAN (GAGAL):" else "HASIL KOORDINAT GPS TERKINI:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isError) MinimalRoseText else MinimalLavenderPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = locationResultText!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MinimalTextPrimary,
                                    lineHeight = 20.sp
                                )

                                if (!isError && locationResultText!!.contains("Latitude")) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val latRegex = Regex("""Latitude:\s*([-+]?\d*\.?\d+)""")
                                                val lonRegex = Regex("""Longitude:\s*([-+]?\d*\.?\d+)""")
                                                val lat = latRegex.find(locationResultText!!)?.groupValues?.get(1)
                                                val lon = lonRegex.find(locationResultText!!)?.groupValues?.get(1)
                                                if (lat != null && lon != null) {
                                                    val mapUri = Uri.parse("https://maps.google.com/?q=$lat,$lon")
                                                    val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                                    context.startActivity(intent)
                                                }
                                            } catch (e: Exception) {}
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalLavenderPrimary),
                                        border = BorderStroke(1.dp, MinimalLavenderPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Buka di Google Maps", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Telegram Send Status
                    if (telegramSendStatus != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val isTelSuccess = telegramSendStatus!!.startsWith("SUCCESS")

                        Surface(
                            color = if (isTelSuccess) MinimalEmerald.copy(alpha = 0.15f) else MinimalRose.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isTelSuccess) MinimalEmerald else MinimalRose),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isTelSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isTelSuccess) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = telegramSendStatus!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isTelSuccess) MinimalEmerald else MinimalRoseText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
