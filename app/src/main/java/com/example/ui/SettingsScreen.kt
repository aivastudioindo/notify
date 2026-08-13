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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalRose
import com.example.ui.theme.MinimalRoseText
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary

@Composable
fun SettingsScreen(
    hasNotificationAccess: Boolean,
    isPinProtectionEnabled: Boolean,
    isVaultUnlocked: Boolean,
    // Calculator Disguise
    isCalculatorDisguiseEnabled: Boolean = false,
    onToggleCalculatorDisguise: (Boolean) -> Unit = {},
    // Telegram Bot parameters
    isTelegramEnabled: Boolean = false,
    telegramBotToken: String = "",
    telegramChatId: String = "",
    telegramExcludeSensitive: Boolean = true,
    telegramTestStatus: String? = null,
    isTestingTelegram: Boolean = false,
    onUpdateTelegramSettings: (enabled: Boolean, token: String, chatId: String, excludeSensitive: Boolean) -> Unit = { _, _, _, _ -> },
    onSendTelegramTestMessage: () -> Unit = {},
    // Action handlers
    onOpenNotificationSettings: () -> Unit,
    onOpenSetPinDialog: () -> Unit,
    onDisablePin: () -> Unit,
    onLockVault: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var botTokenInput by remember(telegramBotToken) { mutableStateOf(telegramBotToken) }
    var chatIdInput by remember(telegramChatId) { mutableStateOf(telegramChatId) }
    var excludeSensitiveInput by remember(telegramExcludeSensitive) { mutableStateOf(telegramExcludeSensitive) }
    var isGuideVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Notification Service Permission
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

        // Section: Telegram Bot Integration
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
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color(0xFF2AABEE), // Telegram Blue Accent
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Penerusan Bot Telegram",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Kirim notifikasi otomatis ke bot Telegram Anda",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }
                        Switch(
                            checked = isTelegramEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateTelegramSettings(enabled, botTokenInput, chatIdInput, excludeSensitiveInput)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MinimalDarkBackground,
                                checkedTrackColor = Color(0xFF2AABEE),
                                uncheckedThumbColor = MinimalTextMuted,
                                uncheckedTrackColor = MinimalCardBackground
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bot Token Field
                    Text(
                        text = "Bot Token Telegram",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = botTokenInput,
                        onValueChange = {
                            botTokenInput = it
                            onUpdateTelegramSettings(isTelegramEnabled, it, chatIdInput, excludeSensitiveInput)
                        },
                        placeholder = { Text("Contoh: 123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ", color = MinimalTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2AABEE),
                            unfocusedBorderColor = MinimalBorder,
                            focusedContainerColor = MinimalCardBackground,
                            unfocusedContainerColor = MinimalCardBackground,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat ID Field
                    Text(
                        text = "Telegram Chat ID / Channel ID",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = chatIdInput,
                        onValueChange = {
                            chatIdInput = it
                            onUpdateTelegramSettings(isTelegramEnabled, botTokenInput, it, excludeSensitiveInput)
                        },
                        placeholder = { Text("Contoh: 987654321 atau @nama_channel", color = MinimalTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2AABEE),
                            unfocusedBorderColor = MinimalBorder,
                            focusedContainerColor = MinimalCardBackground,
                            unfocusedContainerColor = MinimalCardBackground,
                            focusedTextColor = MinimalTextPrimary,
                            unfocusedTextColor = MinimalTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Privacy option: Exclude Sensitive OTP/Passwords
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = excludeSensitiveInput,
                            onCheckedChange = { checked ->
                                excludeSensitiveInput = checked
                                onUpdateTelegramSettings(isTelegramEnabled, botTokenInput, chatIdInput, checked)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2AABEE),
                                uncheckedColor = MinimalTextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Abaikan OTP / Kode Sandi Sensitif (Disarankan)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Telegram Bot Button
                    Button(
                        onClick = onSendTelegramTestMessage,
                        enabled = !isTestingTelegram && botTokenInput.isNotBlank() && chatIdInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2AABEE),
                            contentColor = Color.White,
                            disabledContainerColor = MinimalCardBackground,
                            disabledContentColor = MinimalTextMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTestingTelegram) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mengirim Tes...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tes Kirim Pesan ke Telegram", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // Test Status Banner
                    if (telegramTestStatus != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val isSuccess = telegramTestStatus.startsWith("SUCCESS")
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess) Color(0xFF1B3B2B) else Color(0xFF3D1E24)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSuccess) Color(0xFF25D366) else MinimalRoseText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = telegramTestStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSuccess) Color(0xFF80FFB4) else Color(0xFFFFB4B4),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Expandable Help Guide
                    OutlinedButton(
                        onClick = { isGuideVisible = !isGuideVisible },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MinimalTextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGuideVisible) "Sembunyikan Panduan Bot" else "Cara Membuat Bot & Chat ID",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalTextMuted
                        )
                    }

                    if (isGuideVisible) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MinimalCardBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📖 Langkah Mudah Menghubungkan Telegram:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2AABEE)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "1. Buka aplikasi Telegram lalu cari @BotFather\n" +
                                            "2. Kirim perintah /newbot dan ikuti petunjuk nama bot\n" +
                                            "3. Salin API Token (misal 123456:ABC...) ke kolom Bot Token di atas\n" +
                                            "4. Buka bot Anda lalu tekan /start\n" +
                                            "5. Cari @userinfobot di Telegram lalu tekan /start untuk melihat Chat ID Anda\n" +
                                            "6. Salin Chat ID ke kolom di atas lalu tekan 'Tes Kirim Pesan'",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextPrimary,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3f
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Kunci Aplikasi Menggunakan PIN
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
                                text = "Kunci Aplikasi (PIN)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Minta PIN saat membuka atau kembali ke aplikasi",
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kunci PIN Aplikasi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = if (isPinProtectionEnabled) "PIN 4-digit aktif melindungi aplikasi" else "Proteksi PIN dinonaktifkan",
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
                        Spacer(modifier = Modifier.height(14.dp))

                        // Calculator Disguise Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Penyamaran Ikon Kalkulator",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = if (isCalculatorDisguiseEnabled)
                                        "Aktif - Aplikasi menyamar sebagai Kalkulator"
                                    else
                                        "Ubah ikon & layar pembuka menjadi Kalkulator rahasia",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextMuted
                                )
                            }

                            Switch(
                                checked = isCalculatorDisguiseEnabled,
                                onCheckedChange = { onToggleCalculatorDisguise(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalDarkBackground,
                                    checkedTrackColor = MinimalLavenderPrimary,
                                    uncheckedThumbColor = MinimalTextMuted,
                                    uncheckedTrackColor = MinimalCardBackground
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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

        // Section: Area Hapus Data
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
                        text = "Semua riwayat notifikasi lokal akan dihapus secara permanen.",
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
