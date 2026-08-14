package com.example.ui

import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
fun DisclaimerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("famly_disclaimer_prefs", Context.MODE_PRIVATE) }
    var isAgreed by remember { mutableStateOf(prefs.getBoolean("has_accepted_disclaimer", true)) }

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
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = MinimalLavenderPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Disclaimer & Hukum",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "Syarat Penggunaan Aplikasi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextSecondary
                                )
                            }
                        }

                        Surface(
                            color = if (isAgreed) MinimalEmerald.copy(alpha = 0.15f) else MinimalRose.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isAgreed) MinimalEmerald else MinimalRose)
                        ) {
                            Text(
                                text = if (isAgreed) "DISETUJUI" else "PERLU DISETUJUI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAgreed) MinimalEmerald else MinimalRoseText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Pernyataan penolakan tanggung jawab hukum ini mengatur batasan penggunaan aplikasi Famly. Harap baca dengan teliti sebelum menggunakan fitur perekaman dan pemantauan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Clause 1: Legal Liability Exemption
        item {
            DisclaimerClauseCard(
                icon = Icons.Default.Shield,
                title = "1. Pelepasan Tanggung Jawab Pengembang",
                description = "Pengembang dan penyedia perangkat lunak ini DIBEBASKAN SEPENUHNYA dari segala tuntutan, gugatan, sanksi pidana, maupun kewajiban ganti rugi perdata yang timbul akibat penyalahgunaan aplikasi oleh pengguna (termasuk pemantauan tanpa izin, penyadapan ilegal, atau tindakan yang melanggar hukum setempat)."
            )
        }

        // Clause 2: User Sole Responsibility
        item {
            DisclaimerClauseCard(
                icon = Icons.Default.Gavel,
                title = "2. Tanggung Jawab Mutlak Pengguna",
                description = "Pengguna bertanggung jawab penuh 100% atas seluruh aktivitas, perekaman notifikasi, lokasi GPS, dan penerusan data ke Telegram Bot. Pengguna wajib memastikan bahwa aplikasi dipasang pada perangkat pribadi atau anak di bawah pengawasan sah sesuai Undang-Undang ITE dan UU Perlindungan Data Pribadi (UU PDP)."
            )
        }

        // Clause 3: Encryption & Privacy Security
        item {
            DisclaimerClauseCard(
                icon = Icons.Default.Lock,
                title = "3. Enkripsi Khusus Data Bank & OTP",
                description = "Guna melindungi privasi keuangan pengguna, sistem secara otomatis mengenkripsi data sensitif (seperti transaksi perbankan, saldo, dan kode verifikasi OTP) menggunakan algoritma AES-256 GCM di penyimpanan lokal HP, sementara notifikasi lainnya tetap diteruskan secara transparan ke bot Telegram terhubung."
            )
        }

        // Clause 4: Authorized Monitoring Purpose
        item {
            DisclaimerClauseCard(
                icon = Icons.Default.Info,
                title = "4. Tujuan Penggunaan yang Sah",
                description = "Aplikasi ini dirancang khusus untuk pemantauan mandiri, perlindungan keluarga, pengawasan anak (*parental control*), dan pencatatan notifikasi pribadi. Dilarang keras menggunakan aplikasi ini untuk tujuan spionase, intimidasi, pelecehan, atau tindak pidana lainnya."
            )
        }

        // Agreement Toggle Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isAgreed) MinimalEmerald.copy(alpha = 0.5f) else MinimalBorder),
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
                            Icon(
                                imageVector = if (isAgreed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isAgreed) MinimalEmerald else MinimalRoseText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Persetujuan Ketentuan Hukum",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "Saya memahami dan menerima seluruh syarat di atas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isAgreed,
                            onCheckedChange = { newValue ->
                                isAgreed = newValue
                                prefs.edit().putBoolean("has_accepted_disclaimer", newValue).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MinimalEmerald,
                                uncheckedThumbColor = MinimalTextMuted,
                                uncheckedTrackColor = MinimalSurfaceElevated
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DisclaimerClauseCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MinimalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MinimalSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MinimalLavenderPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MinimalTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MinimalTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
