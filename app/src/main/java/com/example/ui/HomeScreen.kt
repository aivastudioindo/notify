package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
import com.example.ui.components.NotificationCard
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

@Composable
fun HomeScreen(
    notifications: List<NotificationItem>,
    filterState: FilterState,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    isVaultUnlocked: Boolean,
    hasNotificationAccess: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (NotificationCategory?) -> Unit,
    onSelectDateFilter: (DateFilter) -> Unit,
    onToggleFavoritesOnly: () -> Unit,
    onResetFilters: () -> Unit,
    onNotificationClick: (NotificationItem) -> Unit,
    onNotificationLongClick: (NotificationItem) -> Unit,
    onToggleFavorite: (NotificationItem) -> Unit,
    onDeleteSingle: (NotificationItem) -> Unit,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit,
    onRequestPermission: () -> Unit,
    onUnlockVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground)
    ) {
        // Top Selection Bar when multiple notifications are selected
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = MinimalSurfaceElevated,
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClearSelection,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal Pilih",
                                tint = MinimalTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedIds.size} dipilih",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalTextPrimary
                        )
                    }

                    Surface(
                        color = MinimalRose,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onDeleteSelected() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MinimalRoseText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hapus",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalRoseText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Permission Banner if missing
        if (!hasNotificationAccess) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalLavenderPrimary.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MinimalLavenderPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MinimalLavenderPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Akses Notifikasi Diperlukan",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "Aktifkan listener agar NotiVault dapat merekam notifikasi otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = onRequestPermission) {
                        Text(
                            text = "Aktifkan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MinimalLavenderPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = filterState.searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Cari judul, pesan, atau nama aplikasi...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MinimalTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MinimalLavenderPrimary
                )
            },
            trailingIcon = {
                if (filterState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus pencarian",
                            tint = MinimalTextMuted
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MinimalSurfaceElevated,
                unfocusedContainerColor = MinimalSurfaceElevated,
                focusedBorderColor = MinimalLavenderPrimary,
                unfocusedBorderColor = MinimalBorder,
                focusedTextColor = MinimalTextPrimary,
                unfocusedTextColor = MinimalTextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Favorites Only Chip
            FilterChip(
                selected = filterState.favoritesOnly,
                onClick = onToggleFavoritesOnly,
                label = {
                    Text(
                        text = "Favorit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (filterState.favoritesOnly) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (filterState.favoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (filterState.favoritesOnly) MinimalLavenderPrimary else MinimalTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MinimalLavenderPrimary.copy(alpha = 0.15f),
                    selectedLabelColor = MinimalLavenderPrimary,
                    containerColor = MinimalSurfaceElevated,
                    labelColor = MinimalTextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = filterState.favoritesOnly,
                    borderColor = if (filterState.favoritesOnly) MinimalLavenderPrimary else MinimalBorder
                ),
                shape = RoundedCornerShape(20.dp)
            )

            // Date Filters
            DateFilter.entries.forEach { df ->
                FilterChip(
                    selected = filterState.dateFilter == df,
                    onClick = { onSelectDateFilter(df) },
                    label = {
                        Text(
                            text = df.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (filterState.dateFilter == df) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MinimalLavenderPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = MinimalLavenderPrimary,
                        containerColor = MinimalSurfaceElevated,
                        labelColor = MinimalTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filterState.dateFilter == df,
                        borderColor = if (filterState.dateFilter == df) MinimalLavenderPrimary else MinimalBorder
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Categories
            NotificationCategory.entries.forEach { cat ->
                val isSelected = filterState.category == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(if (isSelected) null else cat) },
                    label = {
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MinimalLavenderPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = MinimalLavenderPrimary,
                        containerColor = MinimalSurfaceElevated,
                        labelColor = MinimalTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) MinimalLavenderPrimary else MinimalBorder
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Active Filter Indicators summary (Reset button if filtered)
        val isFiltered = filterState.searchQuery.isNotEmpty() ||
                filterState.category != null ||
                filterState.packageName != null ||
                filterState.dateFilter != DateFilter.ALL ||
                filterState.favoritesOnly

        if (isFiltered) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ditemukan ${notifications.size} notifikasi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MinimalTextMuted
                )

                TextButton(
                    onClick = onResetFilters,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Reset Filter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MinimalLavenderPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Notifications List or Empty State
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Surface(
                        color = MinimalSurfaceElevated,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isFiltered) "Tidak ada pesan yang sesuai filter" else "Belum Ada Notifikasi Terekam",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isFiltered) "Coba sesuaikan kata kunci pencarian atau reset filter kategori." else "Notifikasi yang masuk ke perangkat Anda akan otomatis tersimpan dan terenkripsi di sini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    if (isFiltered) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onResetFilters) {
                            Text(
                                text = "Tampilkan Semua Notifikasi",
                                style = MaterialTheme.typography.labelMedium,
                                color = MinimalLavenderPrimary
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = notifications,
                    key = { it.id },
                    contentType = { "notification_card" }
                ) { item ->
                    val isSelected = selectedIds.contains(item.id)
                    NotificationCard(
                        item = item,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        isVaultUnlocked = isVaultUnlocked,
                        onClick = {
                            if (isSelectionMode) {
                                onNotificationLongClick(item)
                            } else {
                                onNotificationClick(item)
                            }
                        },
                        onLongClick = { onNotificationLongClick(item) },
                        onToggleSelect = { onNotificationLongClick(item) },
                        onToggleFavorite = { onToggleFavorite(item) },
                        onUnlockVaultClick = onUnlockVault
                    )
                }
            }
        }
    }
}
