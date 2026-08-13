package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AnalyticsScreen
import com.example.ui.CategoriesScreen
import com.example.ui.HomeScreen
import com.example.ui.NavDestination
import com.example.ui.NotificationViewModel
import com.example.ui.PinDialogMode
import com.example.ui.SettingsScreen
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.NotificationDetailDialog
import com.example.ui.components.PinAuthDialog
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.NotifVaultTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            NotifVaultTheme(themeMode = themeMode) {
                NotifVaultApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermission()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifVaultApp(viewModel: NotificationViewModel) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val selectedNotification by viewModel.selectedNotification.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val isPinProtectionEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()
    val pinDialogMode by viewModel.pinDialogMode.collectAsState()
    val hasNotificationAccess by viewModel.hasNotificationAccess.collectAsState()
    val analyticsSummary by viewModel.analyticsSummary.collectAsState()
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val distinctApps by viewModel.distinctApps.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 720.dp

        if (isTablet) {
            // Tablet / Wide-screen layout with side NavigationRail
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
                                        NavDestination.ANALYTICS -> Icons.Default.Analytics
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
                        filterState = filterState,
                        notifications = notifications,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        isVaultUnlocked = isVaultUnlocked,
                        isPinProtectionEnabled = isPinProtectionEnabled,
                        hasNotificationAccess = hasNotificationAccess,
                        analyticsSummary = analyticsSummary,
                        categoryCounts = categoryCounts,
                        distinctApps = distinctApps,
                        themeMode = themeMode,
                        onOpenDrawer = { /* Tablet uses rail */ },
                        showHamburger = false,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            // Smartphone layout with modern Hamburger Drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MinimalDarkBackground,
                        drawerContentColor = MinimalTextPrimary
                    ) {
                        AppDrawerContent(
                            currentDestination = currentDest,
                            hasNotificationAccess = hasNotificationAccess,
                            totalRecorded = analyticsSummary.totalRecorded,
                            onSelectDestination = { dest -> viewModel.setDestination(dest) },
                            onSelectCategoryFilter = { cat -> viewModel.selectCategoryFilter(cat) },
                            onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                        )
                    }
                }
            ) {
                AppMainScaffold(
                    currentDest = currentDest,
                    filterState = filterState,
                    notifications = notifications,
                    selectedIds = selectedIds,
                    isSelectionMode = isSelectionMode,
                    isVaultUnlocked = isVaultUnlocked,
                    isPinProtectionEnabled = isPinProtectionEnabled,
                    hasNotificationAccess = hasNotificationAccess,
                    analyticsSummary = analyticsSummary,
                    categoryCounts = categoryCounts,
                    distinctApps = distinctApps,
                    themeMode = themeMode,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    showHamburger = true,
                    viewModel = viewModel
                )
            }
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

    // PIN Authentication Dialog
    if (showPinDialog) {
        PinAuthDialog(
            mode = pinDialogMode,
            onDismiss = { viewModel.dismissPinDialog() },
            onPinSubmit = { pin -> viewModel.unlockVault(pin) },
            onSetNewPin = { pin -> viewModel.setNewPin(pin) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppMainScaffold(
    currentDest: NavDestination,
    filterState: com.example.ui.FilterState,
    notifications: List<com.example.data.model.NotificationItem>,
    selectedIds: Set<Long>,
    isSelectionMode: Boolean,
    isVaultUnlocked: Boolean,
    isPinProtectionEnabled: Boolean,
    hasNotificationAccess: Boolean,
    analyticsSummary: com.example.data.model.AnalyticsSummary,
    categoryCounts: List<com.example.data.local.CategoryCountResult>,
    distinctApps: List<com.example.data.local.AppCountResult>,
    themeMode: com.example.ui.theme.ThemeMode,
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
                                contentDescription = if (isVaultUnlocked) "Kunci Brankas" else "Buka Brankas",
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

                NavDestination.ANALYTICS -> {
                    AnalyticsScreen(
                        analyticsSummary = analyticsSummary
                    )
                }

                NavDestination.CATEGORIES -> {
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
                    SettingsScreen(
                        hasNotificationAccess = hasNotificationAccess,
                        isPinProtectionEnabled = isPinProtectionEnabled,
                        isVaultUnlocked = isVaultUnlocked,
                        themeMode = themeMode,
                        onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                        onOpenSetPinDialog = { viewModel.openSetPinDialog() },
                        onDisablePin = { viewModel.disablePinProtection() },
                        onLockVault = { viewModel.lockVault() },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSendTestNotification = { title, msg, sub ->
                            viewModel.sendTestNotification(title, msg, sub)
                        },
                        onInsertSampleData = { viewModel.insertSampleData() },
                        onClearAllData = { viewModel.clearAll() }
                    )
                }
            }
        }
    }
}
