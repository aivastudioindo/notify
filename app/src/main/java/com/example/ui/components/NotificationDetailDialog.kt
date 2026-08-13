package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationItem
import com.example.ui.theme.MinimalBankBlue
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalEmerald
import com.example.ui.theme.MinimalGoogleRed
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalRose
import com.example.ui.theme.MinimalRoseText
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationDetailDialog(
    item: NotificationItem,
    onDismiss: () -> Unit,
    onToggleFavorite: (NotificationItem) -> Unit,
    onDelete: (NotificationItem) -> Unit
) {
    val context = LocalContext.current
    val fullDate = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US).format(Date(item.postTime))

    val avatarBg = when {
        item.packageName.contains("whatsapp", ignoreCase = true) -> MinimalEmerald
        item.packageName.contains("gmail", ignoreCase = true) || item.packageName.contains("google", ignoreCase = true) -> MinimalGoogleRed
        item.packageName.contains("bank", ignoreCase = true) || item.packageName.contains("bca", ignoreCase = true) || item.packageName.contains("mandiri", ignoreCase = true) -> MinimalBankBlue
        else -> item.category.color
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
            border = BorderStroke(1.dp, MinimalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.appName.firstOrNull()?.uppercaseChar()?.toString() ?: "N",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = item.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalLavenderPrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MinimalTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MinimalBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Information
                DetailItem(label = "Waktu Penerimaan", value = fullDate)
                DetailItem(label = "Package Name", value = item.packageName, isMono = true)

                if (item.isSensitive) {
                    Surface(
                        color = MinimalLavenderPrimary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pesan berisi data OTP / Finansial Terenkripsi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalLavenderPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                if (item.title.isNotBlank()) {
                    Text(
                        text = "Judul",
                        style = MaterialTheme.typography.labelSmall,
                        color = MinimalTextMuted
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalTextPrimary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )
                }

                // SubText
                if (item.subText.isNotBlank()) {
                    DetailItem(label = "Sub Teks", value = item.subText)
                }

                // Content / Message
                Text(
                    text = "Isi Pesan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalTextMuted
                )
                Surface(
                    color = MinimalCardBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    val fullMessage = when {
                        item.bigText.isNotBlank() -> item.bigText
                        item.text.isNotBlank() -> item.text
                        else -> "(Tidak ada isi teks)"
                    }
                    Text(
                        text = fullMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MinimalTextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Security Tag
                Surface(
                    color = MinimalCardBackground,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MinimalLavenderPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AES-256 Bit Local Encryption Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalLavenderPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Famly", "${item.appName}\n${item.title}\n${item.text}")
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "Pesan disalin", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MinimalBorder)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MinimalTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin", style = MaterialTheme.typography.labelMedium, color = MinimalTextPrimary)
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "[${item.appName}] ${item.title}: ${item.text}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MinimalBorder)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = MinimalTextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan", style = MaterialTheme.typography.labelMedium, color = MinimalTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onToggleFavorite(item) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MinimalBorder)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (item.isFavorite) MinimalLavenderPrimary else MinimalTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (item.isFavorite) "Batal Favorit" else "Favorit", style = MaterialTheme.typography.labelMedium, color = MinimalTextPrimary)
                    }

                    Button(
                        onClick = {
                            onDelete(item)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalRose),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MinimalRoseText, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hapus", style = MaterialTheme.typography.labelMedium, color = MinimalRoseText)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, isMono: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MinimalTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            color = MinimalTextPrimary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

