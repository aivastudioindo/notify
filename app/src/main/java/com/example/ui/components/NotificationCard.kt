package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
import com.example.ui.theme.MinimalBankBlue
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalEmerald
import com.example.ui.theme.MinimalGoogleRed
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationCard(
    item: NotificationItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isVaultUnlocked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onUnlockVaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLockedSensitive = item.isSensitive && !isVaultUnlocked
    val formattedTime = formatTimestamp(item.postTime)

    // Vibrant app avatar color
    val avatarBg = when {
        item.packageName.contains("whatsapp", ignoreCase = true) -> MinimalEmerald
        item.packageName.contains("gmail", ignoreCase = true) || item.packageName.contains("google", ignoreCase = true) -> MinimalGoogleRed
        item.packageName.contains("bank", ignoreCase = true) || item.packageName.contains("bca", ignoreCase = true) || item.packageName.contains("mandiri", ignoreCase = true) -> MinimalBankBlue
        item.category == NotificationCategory.CHAT -> MinimalEmerald
        item.category == NotificationCategory.WORK_EMAIL -> MinimalGoogleRed
        item.category == NotificationCategory.FINANCE -> MinimalBankBlue
        else -> item.category.color
    }

    val appInitial = item.appName.firstOrNull()?.uppercaseChar()?.toString() ?: "N"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect() else onClick()
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF322E3A) else MinimalCardBackground
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MinimalLavenderPrimary else MinimalBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MinimalLavenderPrimary,
                        checkmarkColor = Color(0xFF381E72)
                    ),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Circle App Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appInitial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Main Content Body
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                // Header Line (Title/Sender & Timestamp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = when {
                        item.title.isNotBlank() -> item.title
                        item.appName.isNotBlank() -> item.appName
                        else -> "Pemberitahuan"
                    }

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MinimalTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MinimalTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Snippet Body Text
                if (isLockedSensitive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pesan sensitif dilindungi PIN",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MinimalTextSecondary
                            )
                        }

                        Surface(
                            color = MinimalLavenderPrimary,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).combinedClickable(onClick = onUnlockVaultClick)
                        ) {
                            Text(
                                text = "Buka",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF381E72),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    val displayContent = when {
                        item.text.isNotBlank() -> item.text
                        item.bigText.isNotBlank() -> item.bigText
                        item.subText.isNotBlank() -> item.subText
                        else -> "Tidak ada teks detail"
                    }

                    Text(
                        text = displayContent,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MinimalTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Subtitle: App name / Category badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MinimalLavenderPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isFavorite) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favorit",
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Famly", "${item.title}\n${item.text}")
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Pesan disalin", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin",
                                tint = MinimalTextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private val timeFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("hh:mm a", Locale.US)
    }
}

private val dateTimeFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("MMM dd, hh:mm a", Locale.US)
    }
}

private fun formatTimestamp(timeMs: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timeMs

    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < 86400_000L -> timeFormatThreadLocal.get()?.format(Date(timeMs)) ?: ""
        diff < 2 * 86400_000L -> "Yesterday " + (timeFormatThreadLocal.get()?.format(Date(timeMs)) ?: "")
        else -> dateTimeFormatThreadLocal.get()?.format(Date(timeMs)) ?: ""
    }
}

