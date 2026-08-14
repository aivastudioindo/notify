package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.SystemCleanerScreen
import com.example.ui.HomeScreen
import com.example.ui.PermissionsScreen
import com.example.ui.AppFilterScreen
import com.example.ui.TelegramScreen
import com.example.ui.LocationScreen
import com.example.ui.SecurityScreen
import com.example.ui.NavDestination
import com.example.ui.NotificationViewModel
import com.example.ui.PinDialogMode
import com.example.ui.SettingsScreen
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.NotificationDetailDialog
import com.example.ui.components.PinAuthDialog
import com.example.service.NotificationRecorderService
import androidx.compose.ui.platform.LocalContext
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
        com.example.service.FamlyForegroundService.startService(this)

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
    val isCalculatorDisguiseEnabled by viewModel.isCalculatorDisguiseEnabled.collectAsState()
    val showPinDialog by viewModel.showPinDialog.collectAsState()
    val pinDialogMode by viewModel.pinDialogMode.collectAsState()

    val currentContext = LocalContext.current

    // Activity Result Launcher for Background Location (Android 10+)
    val bgLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.checkPermission()
    }

    // Activity Result Launcher for System Runtime Permissions (Location & Notifications)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.checkPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
                currentContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = androidx.core.content.ContextCompat.checkSelfPermission(
                currentContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasBg = androidx.core.content.ContextCompat.checkSelfPermission(
                currentContext,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if ((hasFine || hasCoarse) && !hasBg) {
                try {
                    bgLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } catch (e: Exception) {
                    Log.e("NotifVault", "Gagal meminta bg location: ${e.message}")
                }
            }
        }
    }

    val requestAllSystemPermissions: () -> Unit = {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissionLauncher.launch(perms.toTypedArray())
        NotificationRecorderService.tryRebindService(currentContext)
    }

    // Auto-trigger runtime permissions request on first app launch
    LaunchedEffect(Unit) {
        requestAllSystemPermissions()
    }

    if (isCalculatorDisguiseEnabled && !isVaultUnlocked) {
        SystemCleanerScreen(
            onUnlockWithPin = { pin ->
                viewModel.unlockVault(pin)
            }
        )
        return
    }

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
                                    NavDestination.PERMISSIONS -> Icons.Default.Security
                                    NavDestination.APP_FILTER -> Icons.Default.FilterList
                                    NavDestination.TELEGRAM -> Icons.Default.Send
                                    NavDestination.LOCATION -> Icons.Default.LocationOn
                                    NavDestination.SECURITY -> Icons.Default.Lock
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
                    viewModel = viewModel,
                    onRequestSystemPermissions = requestAllSystemPermissions
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
                viewModel = viewModel,
                onRequestSystemPermissions = requestAllSystemPermissions
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
    viewModel: NotificationViewModel,
    onRequestSystemPermissions: () -> Unit
) {
    val scaffoldContext = LocalContext.current
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
                        onRequestPermission = { viewModel.setDestination(NavDestination.PERMISSIONS) },
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

                NavDestination.PERMISSIONS -> {
                    val hasNotificationAccess by viewModel.hasNotificationAccess.collectAsState()

                    PermissionsScreen(
                        hasNotificationAccess = hasNotificationAccess,
                        hasLocationPermission = viewModel.hasLocationPermission(),
                        hasBackgroundLocationPermission = viewModel.hasBackgroundLocationPermission(),
                        isIgnoringBatteryOptimizations = viewModel.isIgnoringBatteryOptimizations(),
                        onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                        onRequestSystemPermissions = onRequestSystemPermissions,
                        onRequestBatteryOptimization = { com.example.utils.AutostartHelper.requestDisableBatteryOptimization(scaffoldContext) },
                        onOpenAutostart = { com.example.utils.AutostartHelper.openAutostartSettings(scaffoldContext) }
                    )
                }

                NavDestination.APP_FILTER -> {
                    val filterMode by viewModel.filterMode.collectAsState()
                    val blacklist by viewModel.blacklist.collectAsState()
                    val whitelist by viewModel.whitelist.collectAsState()

                    AppFilterScreen(
                        filterMode = filterMode,
                        blacklist = blacklist,
                        whitelist = whitelist,
                        onSetFilterMode = { mode -> viewModel.setFilterMode(mode) },
                        onAddToBlacklist = { pkg -> viewModel.addToBlacklist(pkg) },
                        onRemoveFromBlacklist = { pkg -> viewModel.removeFromBlacklist(pkg) },
                        onAddToWhitelist = { pkg -> viewModel.addToWhitelist(pkg) },
                        onRemoveFromWhitelist = { pkg -> viewModel.removeFromWhitelist(pkg) },
                        onResetFilterDefaults = { viewModel.resetFilterDefaults() },
                        onGetInstalledApps = { viewModel.getInstalledApps() },
                        onGetAppName = { pkg -> viewModel.getAppNameForPackage(pkg) }
                    )
                }

                NavDestination.TELEGRAM -> {
                    val isTelegramEnabled by viewModel.isTelegramEnabled.collectAsState()
                    val telegramBotToken by viewModel.telegramBotToken.collectAsState()
                    val telegramChatId by viewModel.telegramChatId.collectAsState()
                    val telegramExcludeSensitive by viewModel.telegramExcludeSensitive.collectAsState()
                    val telegramTestStatus by viewModel.telegramTestStatus.collectAsState()
                    val isTestingTelegram by viewModel.isTestingTelegram.collectAsState()

                    TelegramScreen(
                        isTelegramEnabled = isTelegramEnabled,
                        telegramBotToken = telegramBotToken,
                        telegramChatId = telegramChatId,
                        telegramExcludeSensitive = telegramExcludeSensitive,
                        telegramTestStatus = telegramTestStatus,
                        isTestingTelegram = isTestingTelegram,
                        onUpdateTelegramSettings = { enabled, token, chatId, excludeSensitive ->
                            viewModel.updateTelegramSettings(enabled, token, chatId, excludeSensitive)
                        },
                        onSendTelegramTestMessage = { viewModel.sendTelegramTestMessage() },
                        onSendLocationToTelegram = { onResult -> viewModel.sendLocationToTelegram(onResult) }
                    )
                }

                NavDestination.LOCATION -> {
                    val currentLocationState by viewModel.currentLocationState.collectAsState()
                    val isFetchingLocation by viewModel.isFetchingLocation.collectAsState()

                    LocationScreen(
                        hasLocationPermission = viewModel.hasLocationPermission(),
                        isGpsEnabled = viewModel.isGpsEnabled(),
                        currentLocationState = currentLocationState,
                        isFetchingLocation = isFetchingLocation,
                        onTestLocation = { onResult -> viewModel.testCurrentLocation(onResult) },
                        onSendLocationToTelegram = { onResult -> viewModel.sendLocationToTelegram(onResult) }
                    )
                }

                NavDestination.SECURITY -> {
                    val isCalculatorDisguiseEnabled by viewModel.isCalculatorDisguiseEnabled.collectAsState()

                    SecurityScreen(
                        isPinProtectionEnabled = isPinProtectionEnabled,
                        isVaultUnlocked = isVaultUnlocked,
                        isCalculatorDisguiseEnabled = isCalculatorDisguiseEnabled,
                        onToggleCalculatorDisguise = { enabled ->
                            viewModel.setCalculatorDisguise(enabled)
                        },
                        onOpenSetPinDialog = { viewModel.openSetPinDialog() },
                        onDisablePin = { viewModel.disablePinProtection() },
                        onLockVault = { viewModel.lockVault() }
                    )
                }

                NavDestination.SETTINGS -> {
                    val hasNotificationAccess by viewModel.hasNotificationAccess.collectAsState()

                    SettingsScreen(
                        hasNotificationAccess = hasNotificationAccess,
                        hasBackgroundLocationAccess = viewModel.hasBackgroundLocationPermission(),
                        isIgnoringBatteryOptimizations = viewModel.isIgnoringBatteryOptimizations(),
                        onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                        onRequestBackgroundPermissions = onRequestSystemPermissions,
                        onRequestBatteryOptimization = { com.example.utils.AutostartHelper.requestDisableBatteryOptimization(scaffoldContext) },
                        onOpenAutostart = { com.example.utils.AutostartHelper.openAutostartSettings(scaffoldContext) },
                        onClearAllData = { viewModel.clearAll() }
                    )
                }
            }
        }
    }
}
