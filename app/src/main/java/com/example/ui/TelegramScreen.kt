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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
fun TelegramScreen(
    isTelegramEnabled: Boolean,
    telegramBotToken: String,
    telegramChatId: String,
    telegramExcludeSensitive: Boolean,
    telegramTestStatus: String?,
    isTestingTelegram: Boolean,
    onUpdateTelegramSettings: (Boolean, String, String, Boolean) -> Unit,
    onSendTelegramTestMessage: () -> Unit,
    onSendLocationToTelegram: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var enabled by remember(isTelegramEnabled) { mutableStateOf(isTelegramEnabled) }
    var token by remember(telegramBotToken) { mutableStateOf(telegramBotToken) }
    var chatId by remember(telegramChatId) { mutableStateOf(telegramChatId) }
    var excludeSensitive by remember(telegramExcludeSensitive) { mutableStateOf(telegramExcludeSensitive) }
    var locationStatusMsg by remember { mutableStateOf<String?>(null) }
    var isSendingLocation by remember { mutableStateOf(false) }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MinimalLavenderPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MinimalLavenderPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Bot Telegram & Remote",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = if (enabled) "Integrasi Aktif" else "Integrasi Nonaktif",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (enabled) MinimalEmerald else MinimalTextMuted
                                )
                            }
                        }

                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                enabled = it
                                onUpdateTelegramSettings(enabled, token, chatId, excludeSensitive)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF381E72),
                                checkedTrackColor = MinimalLavenderPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Notifikasi anak otomatis diteruskan ke akun Telegram orang tua secara real-time. Anda juga dapat mengetik /lokasi atau /ping di bot Telegram untuk memantau dari jauh.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Credentials Config Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KONFIGURASI BOT TELEGRAM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = token,
                        onValueChange = {
                            token = it
                            onUpdateTelegramSettings(enabled, token, chatId, excludeSensitive)
                        },
                        label = { Text("Bot API Token (dari @BotFather)") },
                        placeholder = { Text("123456789:ABCdefGhIJKlmNoPQRstuVWXyz") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalLavenderPrimary,
                            unfocusedBorderColor = MinimalBorder,
                            focusedLabelColor = MinimalLavenderPrimary,
                            unfocusedLabelColor = MinimalTextMuted,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = chatId,
                        onValueChange = {
                            chatId = it
                            onUpdateTelegramSettings(enabled, token, chatId, excludeSensitive)
                        },
                        label = { Text("Chat ID Telegram Orang Tua (dari @userinfobot)") },
                        placeholder = { Text("987654321") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalLavenderPrimary,
                            unfocusedBorderColor = MinimalBorder,
                            focusedLabelColor = MinimalLavenderPrimary,
                            unfocusedLabelColor = MinimalTextMuted,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Exclude Sensitive Toggle
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kecualikan Data Sensitif",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "Jangan kirim notifikasi Bank & OTP ke Telegram",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextMuted
                                )
                            }
                            Switch(
                                checked = excludeSensitive,
                                onCheckedChange = {
                                    excludeSensitive = it
                                    onUpdateTelegramSettings(enabled, token, chatId, it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF381E72),
                                    checkedTrackColor = MinimalLavenderPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (Only Kirim GPS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                isSendingLocation = true
                                onSendLocationToTelegram { res ->
                                    isSendingLocation = false
                                    locationStatusMsg = res
                                }
                            },
                            enabled = !isSendingLocation && token.isNotBlank() && chatId.isNotBlank(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MinimalTextPrimary),
                            border = BorderStroke(1.dp, MinimalBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSendingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MinimalLavenderPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kirim Lokasi GPS ke Telegram", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Location Status Feedback Card
                    if (locationStatusMsg != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val isLocSuccess = locationStatusMsg!!.startsWith("SUCCESS")

                        Surface(
                            color = if (isLocSuccess) MinimalEmerald.copy(alpha = 0.15f) else MinimalRose.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isLocSuccess) MinimalEmerald else MinimalRose),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isLocSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isLocSuccess) MinimalEmerald else MinimalRoseText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = locationStatusMsg!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLocSuccess) MinimalEmerald else MinimalRoseText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Remote Commands Cheat Sheet
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERINTAH REMOTE TELEGRAM (DARI BOT)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MinimalLavenderPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    CommandItemRow(
                        command = "/screenshot",
                        desc = "Tangkap dan kirim foto layar HP anak secara langsung ke Telegram."
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CommandItemRow(
                        command = "/lokasi",
                        desc = "Minta koordinat GPS live dan peta Google Maps terkini dari HP anak."
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CommandItemRow(
                        command = "/ping",
                        desc = "Cek status aktif HP anak, status baterai, sinyal & listener."
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CommandItemRow(
                        command = "/help",
                        desc = "Daftar semua perintah pemantauan orang tua yang tersedia."
                    )
                }
            }
        }
    }
}

@Composable
private fun CommandItemRow(command: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MinimalSurfaceElevated, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MinimalLavenderPrimary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = command,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MinimalLavenderPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MinimalTextSecondary,
            lineHeight = 16.sp
        )
    }
}
