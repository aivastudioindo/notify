package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationCategory
import com.example.ui.NavDestination
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@Composable
fun AppDrawerContent(
    currentDestination: NavDestination,
    hasNotificationAccess: Boolean,
    totalRecorded: Int,
    onSelectDestination: (NavDestination) -> Unit,
    onSelectCategoryFilter: (NotificationCategory?) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MinimalDarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Drawer Header with Minimal Charcoal Elevation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MinimalSurfaceElevated)
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MinimalLavenderPrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "NotiVault",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "Clean Minimal Notification Logger",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Service Status Indicator
                Surface(
                    color = if (hasNotificationAccess) Color(0xFF25D366).copy(alpha = 0.15f) else MinimalLavenderPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasNotificationAccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasNotificationAccess) Color(0xFF25D366) else MinimalLavenderPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasNotificationAccess) "Listener Aktif ($totalRecorded rekaman)" else "Perlu Izin Notifikasi",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (hasNotificationAccess) Color(0xFF25D366) else MinimalLavenderPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Navigation Items
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = "NAVIGASI",
                style = MaterialTheme.typography.labelSmall,
                color = MinimalTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Semua Notifikasi", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                selected = currentDestination == NavDestination.ALL_NOTIFICATIONS,
                onClick = {
                    onSelectDestination(NavDestination.ALL_NOTIFICATIONS)
                    onCloseDrawer()
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MinimalSurfaceElevated,
                    selectedIconColor = MinimalLavenderPrimary,
                    selectedTextColor = MinimalLavenderPrimary,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = MinimalTextSecondary,
                    unselectedTextColor = MinimalTextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            NavigationDrawerItem(
                label = { Text("Kategori & Aplikasi", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                selected = currentDestination == NavDestination.CATEGORIES,
                onClick = {
                    onSelectDestination(NavDestination.CATEGORIES)
                    onCloseDrawer()
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MinimalSurfaceElevated,
                    selectedIconColor = MinimalLavenderPrimary,
                    selectedTextColor = MinimalLavenderPrimary,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = MinimalTextSecondary,
                    unselectedTextColor = MinimalTextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            NavigationDrawerItem(
                label = { Text("Pengaturan", fontWeight = FontWeight.Medium) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                selected = currentDestination == NavDestination.SETTINGS,
                onClick = {
                    onSelectDestination(NavDestination.SETTINGS)
                    onCloseDrawer()
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MinimalSurfaceElevated,
                    selectedIconColor = MinimalLavenderPrimary,
                    selectedTextColor = MinimalLavenderPrimary,
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = MinimalTextSecondary,
                    unselectedTextColor = MinimalTextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MinimalBorder)
        Spacer(modifier = Modifier.height(12.dp))

        // Quick Category Filter Section
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = "FILTER KATEGORI",
                style = MaterialTheme.typography.labelSmall,
                color = MinimalTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            NotificationCategory.entries.take(5).forEach { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onSelectCategoryFilter(cat)
                            onSelectDestination(NavDestination.ALL_NOTIFICATIONS)
                            onCloseDrawer()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cat.color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MinimalTextPrimary
                        )
                    }

                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        tint = MinimalTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Footer App Info
        Surface(
            color = MinimalSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MinimalLavenderPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "NOTIVAULT LITE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MinimalLavenderPrimary
                    )
                    Text(
                        text = "Aplikasi cepat & responsif",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MinimalTextSecondary
                    )
                }
            }
        }
    }
}

