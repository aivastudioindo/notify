package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CategoriesScreen
import com.example.ui.HomeScreen
import com.example.ui.NavDestination
import com.example.ui.NotificationViewModel
import com.example.ui.PinDialogMode
import com.example.ui.SettingsScreen
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.NotificationDetailDialog
import com.example.ui.components.PinAuthDialog
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.NotifVaultTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotifVaultTheme {
                NotifVaultApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResume()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifVaultApp(viewModel: NotificationViewModel) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val selectedNotification by viewModel.selectedNotification.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val isPinProtectionEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()
    val pinDialogMode by viewModel.pinDialogMode.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val isTablet = LocalConfiguration.current.screenWidthDp >= 720

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MinimalDarkBackground)
        ) {
            NavigationRail(
                containerColor = MinimalSurfaceElevated,
                contentColor = MinimalTextPrimary,
                header = {
                    Surface(
                        color = MinimalLavenderPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                NavDestination.entries.forEach { dest ->
                    NavigationRailItem(
                        selected = currentDest == dest,
                        onClick = { viewModel.setDestination(dest) },
                        icon = {
                            Icon(
                                imageVector = when (dest) {
                                    NavDestination.ALL_NOTIFICATIONS -> Icons.Default.Notifications
                                    NavDestination.CATEGORIES -> Icons.Default.Category
                                    NavDestination.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = dest.title
                            )
                        },
                        label = { Text(dest.title.take(8), fontSize = 10.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MinimalLavenderPrimary,
                            selectedTextColor = MinimalLavenderPrimary,
                            unselectedIconColor = MinimalTextMuted,
                            unselectedTextColor = MinimalTextMuted,
                            indicatorColor = MinimalCardBackground
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AppMainScaffold(
                    currentDest = currentDest,
                    isVaultUnlocked = isVaultUnlocked,
                    isPinProtectionEnabled = isPinProtectionEnabled,
                    onOpenDrawer = { /* Tablet uses rail */ },
                    showHamburger = false,
                    viewModel = viewModel
                )
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MinimalDarkBackground,
                    drawerContentColor = MinimalTextPrimary
                ) {
                    val hasAccess by viewModel.hasNotificationAccess.collectAsState()
                    val notifications by viewModel.notifications.collectAsState()
                    AppDrawerContent(
                        currentDestination = currentDest,
                        hasNotificationAccess = hasAccess,
                        totalRecorded = notifications.size,
                        onSelectDestination = { dest -> viewModel.setDestination(dest) },
                        onSelectCategoryFilter = { cat -> viewModel.selectCategoryFilter(cat) },
                        onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                    )
                }
            }
        ) {
            AppMainScaffold(
                currentDest = currentDest,
                isVaultUnlocked = isVaultUnlocked,
                isPinProtectionEnabled = isPinProtectionEnabled,
                onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                showHamburger = true,
                viewModel = viewModel
            )
        }
    }

    // Detail Dialog
    if (selectedNotification != null) {
        NotificationDetailDialog(
            item = selectedNotification!!,
            onDismiss = { viewModel.selectNotification(null) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDelete = { viewModel.deleteNotification(it) }
        )
    }

    // PIN Authentication Dialog / App Lock
    if (showPinDialog) {
        PinAuthDialog(
            mode = pinDialogMode,
            onDismiss = {
                if (isVaultUnlocked || pinDialogMode == PinDialogMode.SET_NEW) {
                    viewModel.dismissPinDialog()
                }
            },
            onPinSubmit = { pin -> viewModel.unlockVault(pin) },
            onSetNewPin = { pin -> viewModel.setNewPin(pin) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppMainScaffold(
    currentDest: NavDestination,
    isVaultUnlocked: Boolean,
    isPinProtectionEnabled: Boolean,
    onOpenDrawer: () -> Unit,
    showHamburger: Boolean,
    viewModel: NotificationViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentDest.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalTextPrimary
                    )
                },
                navigationIcon = {
                    if (showHamburger) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Navigasi",
                                tint = MinimalTextPrimary
                            )
                        }
                    }
                },
                actions = {
                    if (isPinProtectionEnabled) {
                        IconButton(
                            onClick = {
                                if (isVaultUnlocked) {
                                    viewModel.lockVault()
                                } else {
                                    viewModel.openUnlockPinDialog()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (isVaultUnlocked) "Kunci Aplikasi" else "Buka Aplikasi",
                                tint = if (isVaultUnlocked) MinimalTextMuted else MinimalLavenderPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MinimalDarkBackground,
                    titleContentColor = MinimalTextPrimary
                )
            )
        },
        containerColor = MinimalDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDest) {
                NavDestination.ALL_NOTIFICATIONS -> {
                    val notifications by viewModel.notifications.collectAsState()
                    val filterState by viewModel.filterState.collectAsState()
                    val selectedIds by viewModel.selectedIds.collectAsState()
                    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
                    val hasNotificationAccess by viewModel.hasNotificationAccess.collectAsState()

                    HomeScreen(
                        notifications = notifications,
                        filterState = filterState,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        isVaultUnlocked = isVaultUnlocked,
                        hasNotificationAccess = hasNotificationAccess,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onSelectCategory = { viewModel.selectCategoryFilter(it) },
                        onSelectDateFilter = { viewModel.selectDateFilter(it) },
                        onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
                        onResetFilters = { viewModel.resetFilters() },
                        onNotificationClick = { viewModel.selectNotification(it) },
                        onNotificationLongClick = { viewModel.toggleSelectId(it.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeleteSingle = { viewModel.deleteNotification(it) },
                        onDeleteSelected = { viewModel.deleteSelected() },
                        onClearSelection = { viewModel.clearSelection() },
                        onRequestPermission = { viewModel.openNotificationSettings() },
                        onUnlockVault = { viewModel.openUnlockPinDialog() }
                    )
                }

                NavDestination.CATEGORIES -> {
                    val categoryCounts by viewModel.categoryCounts.collectAsState()
                    val distinctApps by viewModel.distinctApps.collectAsState()

                    CategoriesScreen(
                        categoryCounts = categoryCounts,
                        distinctApps = distinctApps,
                        onSelectCategory = { cat ->
                            viewModel.selectCategoryFilter(cat)
                            viewModel.setDestination(NavDestination.ALL_NOTIFICATIONS)
                        },
                        onSelectApp = { pkg ->
                            viewModel.selectPackageFilter(pkg)
                            viewModel.setDestination(NavDestination.ALL_NOTIFICATIONS)
                        },
                        onDeleteCategory = { cat -> viewModel.deleteByCategory(cat) },
                        onDeleteApp = { pkg -> viewModel.deleteByPackage(pkg) }
                    )
                }

                NavDestination.SETTINGS -> {
                    val hasNotificationAccess by viewModel.hasNotificationAccess.collectAsState()

                    SettingsScreen(
                        hasNotificationAccess = hasNotificationAccess,
                        isPinProtectionEnabled = isPinProtectionEnabled,
                        isVaultUnlocked = isVaultUnlocked,
                        onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                        onOpenSetPinDialog = { viewModel.openSetPinDialog() },
                        onDisablePin = { viewModel.disablePinProtection() },
                        onLockVault = { viewModel.lockVault() },
                        onClearAllData = { viewModel.clearAll() }
                    )
                }
            }
        }
    }
}
