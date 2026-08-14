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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun SecurityScreen(
    isPinProtectionEnabled: Boolean,
    isVaultUnlocked: Boolean,
    isCalculatorDisguiseEnabled: Boolean,
    onToggleCalculatorDisguise: (Boolean) -> Unit,
    onOpenSetPinDialog: () -> Unit,
    onDisablePin: () -> Unit,
    onLockVault: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MinimalLavenderPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Keamanan & Penyamaran",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Kunci PIN & Penyamaran Pembersih Sistem",
                                style = MaterialTheme.typography.bodySmall,
                                color = MinimalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Melindungi aplikasi agar tidak dapat dibuka sembarangan oleh anak atau orang lain, dan menyamarkan tampilan aplikasi menjadi aplikasi Pembersih Sistem biasa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // PIN Protection Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KUNCI PIN APLIKASI",
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
                                    imageVector = if (isPinProtectionEnabled) Icons.Default.Lock else Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = if (isPinProtectionEnabled) MinimalEmerald else MinimalTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Status Kunci PIN",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalTextPrimary
                                    )
                                    Text(
                                        text = if (isPinProtectionEnabled) "PIN Aktif & Terlindungi" else "PIN Nonaktif (Buka Langsung)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isPinProtectionEnabled) MinimalEmerald else MinimalTextMuted
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
                            onClick = onOpenSetPinDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MinimalLavenderPrimary,
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPinProtectionEnabled) "Ganti PIN" else "Atur PIN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isPinProtectionEnabled) {
                            OutlinedButton(
                                onClick = onDisablePin,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalRoseText),
                                border = BorderStroke(1.dp, MinimalRose),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Matikan PIN", fontSize = 12.sp)
                            }
                        }
                    }

                    if (isPinProtectionEnabled && isVaultUnlocked) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onLockVault,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextSecondary),
                            border = BorderStroke(1.dp, MinimalBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kunci Vault Sekarang", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Calculator Disguise Card
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MinimalLavenderPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MinimalLavenderPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Penyamaran Pembersih Sistem",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = if (isCalculatorDisguiseEnabled) "Aktif (Tampil Sebagai Pembersih Sistem)" else "Nonaktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCalculatorDisguiseEnabled) MinimalEmerald else MinimalTextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isCalculatorDisguiseEnabled,
                            onCheckedChange = onToggleCalculatorDisguise,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF381E72),
                                checkedTrackColor = MinimalLavenderPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ketika penyamaran aktif, aplikasi akan terbuka sebagai utilitas Pembersih Sistem. Masukkan PIN Anda pada kolom Verifikasi PIN untuk membuka ruang pemantauan rahasia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // AES-256 Hardware Encryption Badge Card
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
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MinimalEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Enkripsi Otomatis Perbankan & OTP",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Semua notifikasi transaksi finansial (BCA, Mandiri, BRI, DANA, GoPay, dll.) dan kode rahasia OTP dienkripsi secara otomatis menggunakan AES-256 GCM pada hardware Android Keystore sehingga tidak dapat dibaca oleh pihak lain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
