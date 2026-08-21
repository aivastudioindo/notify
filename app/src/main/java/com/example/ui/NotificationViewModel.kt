package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FamlyApplication
import com.example.data.local.AppCountResult
import com.example.data.local.CategoryCountResult
import com.example.data.model.AnalyticsSummary
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
import com.example.data.security.IconDisguiseManager
import com.example.service.NotificationHelper
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavDestination(val title: String) {
    ALL_NOTIFICATIONS("Semua Notifikasi"),
    CATEGORIES("Kategori & Statistik"),
    PERMISSIONS("Pusat Izin Akses"),
    APP_FILTER("Whitelist & Blacklist"),
    TELEGRAM("Bot Telegram"),
    LOCATION("Lokasi & GPS"),
    SYSTEM_CLEANER("Pembersih Memori & Cache"),
    SECURITY("Keamanan & PIN"),
    SETTINGS("Sistem & Cadangan"),
    DISCLAIMER("Disclaimer & Hukum")
}

enum class DateFilter(val label: String) {
    ALL("Semua Waktu"),
    TODAY("Hari Ini"),
    LAST_7_DAYS("7 Hari Terakhir"),
    THIS_MONTH("Bulan Ini")
}

data class FilterState(
    val searchQuery: String = "",
    val category: NotificationCategory? = null,
    val packageName: String? = null,
    val dateFilter: DateFilter = DateFilter.ALL,
    val favoritesOnly: Boolean = false
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as FamlyApplication).repository
    private val encryptionManager = (application as FamlyApplication).encryptionManager
    private val telegramBotManager = (application as FamlyApplication).telegramBotManager
    private val appFilterManager = (application as FamlyApplication).appFilterManager
    private val locationHelper by lazy { com.example.data.location.LocationHelper(getApplication()) }
    private val context: Context get() = getApplication<Application>().applicationContext

    // App Whitelist & Blacklist Filter State
    val filterMode: StateFlow<com.example.data.filter.AppFilterMode> = appFilterManager.filterMode
    val blacklist: StateFlow<Set<String>> = appFilterManager.blacklist
    val whitelist: StateFlow<Set<String>> = appFilterManager.whitelist

    fun setFilterMode(mode: com.example.data.filter.AppFilterMode) {
        appFilterManager.setFilterMode(mode)
    }

    fun addToBlacklist(packageName: String) {
        appFilterManager.addToBlacklist(packageName)
    }

    fun removeFromBlacklist(packageName: String) {
        appFilterManager.removeFromBlacklist(packageName)
    }

    fun addToWhitelist(packageName: String) {
        appFilterManager.addToWhitelist(packageName)
    }

    fun removeFromWhitelist(packageName: String) {
        appFilterManager.removeFromWhitelist(packageName)
    }

    fun resetFilterDefaults() {
        appFilterManager.resetToDefaults()
    }

    fun getInstalledApps(): List<com.example.data.filter.AppItem> {
        return appFilterManager.getInstalledApps()
    }

    fun getAppNameForPackage(packageName: String): String {
        return appFilterManager.getAppNameForPackage(packageName)
    }

    // Telegram Bot Settings State
    private val _isTelegramEnabled = MutableStateFlow(telegramBotManager.isEnabled())
    val isTelegramEnabled: StateFlow<Boolean> = _isTelegramEnabled.asStateFlow()

    private val _telegramBotToken = MutableStateFlow(telegramBotManager.getBotToken())
    val telegramBotToken: StateFlow<String> = _telegramBotToken.asStateFlow()

    private val _telegramChatId = MutableStateFlow(telegramBotManager.getChatId())
    val telegramChatId: StateFlow<String> = _telegramChatId.asStateFlow()

    private val _telegramExcludeSensitive = MutableStateFlow(telegramBotManager.isExcludeSensitive())
    val telegramExcludeSensitive: StateFlow<Boolean> = _telegramExcludeSensitive.asStateFlow()

    private val _telegramTestStatus = MutableStateFlow<String?>(null)
    val telegramTestStatus: StateFlow<String?> = _telegramTestStatus.asStateFlow()

    private val _isTestingTelegram = MutableStateFlow(false)
    val isTestingTelegram: StateFlow<Boolean> = _isTestingTelegram.asStateFlow()

    private val _currentDestination = MutableStateFlow(NavDestination.ALL_NOTIFICATIONS)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _selectedNotification = MutableStateFlow<NotificationItem?>(null)
    val selectedNotification: StateFlow<NotificationItem?> = _selectedNotification.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    // Security & App PIN Lock State
    private val _isVaultUnlocked = MutableStateFlow(!encryptionManager.isPinProtectionEnabled())
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _isPinProtectionEnabled = MutableStateFlow(encryptionManager.isPinProtectionEnabled())
    val isPinProtectionEnabled: StateFlow<Boolean> = _isPinProtectionEnabled.asStateFlow()

    private val _isCleanerDisguiseEnabled = MutableStateFlow(encryptionManager.isCleanerDisguiseEnabled())
    val isCleanerDisguiseEnabled: StateFlow<Boolean> = _isCleanerDisguiseEnabled.asStateFlow()
    val isCalculatorDisguiseEnabled: StateFlow<Boolean> = isCleanerDisguiseEnabled

    // Anti-Uninstall Device Admin State
    private val _isDeviceAdminActive = MutableStateFlow(com.example.utils.DeviceAdminHelper.isDeviceAdminActive(context))
    val isDeviceAdminActive: StateFlow<Boolean> = _isDeviceAdminActive.asStateFlow()

    private val _showPinDialog = MutableStateFlow(encryptionManager.isPinProtectionEnabled())
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _pinDialogMode = MutableStateFlow<PinDialogMode>(PinDialogMode.UNLOCK)
    val pinDialogMode: StateFlow<PinDialogMode> = _pinDialogMode.asStateFlow()

    // Permission state
    private val _hasNotificationAccess = MutableStateFlow(false)
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private val prefs by lazy { context.getSharedPreferences("famly_onboarding_prefs", Context.MODE_PRIVATE) }
    private val _showOnboardingDialog = MutableStateFlow(false)
    val showOnboardingDialog: StateFlow<Boolean> = _showOnboardingDialog.asStateFlow()

    // Notifications Flow derived from FilterState
    val notifications: StateFlow<List<NotificationItem>> = _filterState.flatMapLatest { filter ->
        val (startTime, endTime) = getTimeRangeForFilter(filter.dateFilter)
        repository.getFilteredNotifications(
            searchQuery = filter.searchQuery,
            category = filter.category,
            packageName = filter.packageName,
            isFavorite = if (filter.favoritesOnly) true else null,
            startTime = startTime,
            endTime = endTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Distinct Apps
    val distinctApps: StateFlow<List<AppCountResult>> = repository.distinctAppsWithCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Category Counts
    val categoryCounts: StateFlow<List<CategoryCountResult>> = repository.categoryCounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        checkPermission()
        if (encryptionManager.isPinProtectionEnabled()) {
            _isVaultUnlocked.value = false
            _showPinDialog.value = true
            _pinDialogMode.value = PinDialogMode.UNLOCK
        }
    }

    fun onAppResume() {
        checkPermission()
        if (encryptionManager.isPinProtectionEnabled()) {
            _isPinProtectionEnabled.value = true
            _isVaultUnlocked.value = false
            _showPinDialog.value = true
            _pinDialogMode.value = PinDialogMode.UNLOCK
        }
    }

    private fun checkFirstLaunchOnboarding() {
        // Consolidated into Pusat Izin Akses Screen
    }

    fun openOnboardingDialog() {
        setDestination(NavDestination.PERMISSIONS)
    }

    fun dismissOnboardingDialog() {
        _showOnboardingDialog.value = false
    }

    fun checkPermission() {
        _hasNotificationAccess.value = NotificationHelper.isNotificationAccessGranted(context)
        _isDeviceAdminActive.value = com.example.utils.DeviceAdminHelper.isDeviceAdminActive(context)
    }

    fun requestDeviceAdmin(activity: android.app.Activity) {
        com.example.utils.DeviceAdminHelper.requestDeviceAdmin(activity)
    }

    fun openDeviceAdminSettings() {
        com.example.utils.DeviceAdminHelper.openDeviceAdminSettings(context)
    }

    fun openNotificationSettings() {
        NotificationHelper.openNotificationAccessSettings(context)
    }

    fun setDestination(dest: NavDestination) {
        _currentDestination.value = dest
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun selectCategoryFilter(category: NotificationCategory?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun selectPackageFilter(packageName: String?) {
        _filterState.value = _filterState.value.copy(packageName = packageName)
    }

    fun selectDateFilter(dateFilter: DateFilter) {
        _filterState.value = _filterState.value.copy(dateFilter = dateFilter)
    }

    fun toggleFavoritesOnly() {
        _filterState.value = _filterState.value.copy(favoritesOnly = !_filterState.value.favoritesOnly)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun selectNotification(item: NotificationItem?) {
        _selectedNotification.value = item
    }

    fun toggleFavorite(item: NotificationItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
            if (_selectedNotification.value?.id == item.id) {
                _selectedNotification.value = _selectedNotification.value?.copy(isFavorite = !item.isFavorite)
            }
        }
    }

    fun deleteNotification(item: NotificationItem) {
        viewModelScope.launch {
            repository.deleteById(item.id)
            if (_selectedNotification.value?.id == item.id) {
                _selectedNotification.value = null
            }
        }
    }

    fun deleteByPackage(packageName: String) {
        viewModelScope.launch {
            repository.deleteByPackage(packageName)
            if (_selectedNotification.value?.packageName == packageName) {
                _selectedNotification.value = null
            }
        }
    }

    fun deleteByCategory(category: NotificationCategory) {
        viewModelScope.launch {
            repository.deleteByCategory(category)
            if (_selectedNotification.value?.category == category) {
                _selectedNotification.value = null
            }
        }
    }

    fun toggleSelectId(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
        _isSelectionMode.value = current.isNotEmpty()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            repository.deleteByIds(ids)
            clearSelection()
            _selectedNotification.value = null
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedNotification.value = null
            clearSelection()
        }
    }

    // Security & PIN Vault
    fun openUnlockPinDialog() {
        _pinDialogMode.value = PinDialogMode.UNLOCK
        _showPinDialog.value = true
    }

    fun openSetPinDialog() {
        _pinDialogMode.value = PinDialogMode.SET_NEW
        _showPinDialog.value = true
    }

    fun dismissPinDialog() {
        _showPinDialog.value = false
    }

    fun lockVault() {
        if (encryptionManager.isPinProtectionEnabled()) {
            _isVaultUnlocked.value = false
        }
    }

    fun unlockVault(pin: String): Boolean {
        val success = encryptionManager.verifyPin(pin)
        if (success) {
            _isVaultUnlocked.value = true
            _showPinDialog.value = false
        }
        return success
    }

    fun setNewPin(pin: String) {
        encryptionManager.setPinProtection(true, pin)
        _isPinProtectionEnabled.value = true
        _isVaultUnlocked.value = true
        _showPinDialog.value = false
    }

    fun disablePinProtection() {
        encryptionManager.setPinProtection(false, null)
        encryptionManager.setCleanerDisguiseEnabled(false)
        IconDisguiseManager.setCleanerDisguise(context, false)
        _isCleanerDisguiseEnabled.value = false
        _isPinProtectionEnabled.value = false
        _isVaultUnlocked.value = true
    }

    fun setCleanerDisguise(enabled: Boolean) {
        if (enabled && !encryptionManager.isPinProtectionEnabled()) {
            openSetPinDialog()
            return
        }
        encryptionManager.setCleanerDisguiseEnabled(enabled)
        IconDisguiseManager.setCleanerDisguise(context, enabled)
        _isCleanerDisguiseEnabled.value = enabled
    }

    fun setCalculatorDisguise(enabled: Boolean) = setCleanerDisguise(enabled)

    private val _isAppHidden = MutableStateFlow(IconDisguiseManager.isAppHidden(context))
    val isAppHidden: StateFlow<Boolean> = _isAppHidden.asStateFlow()

    fun setAppHidden(hidden: Boolean) {
        IconDisguiseManager.setAppHidden(context, hidden)
        _isAppHidden.value = hidden
    }

    fun unlockVaultFromSecret() {
        _isVaultUnlocked.value = true
        _showPinDialog.value = false
        _isAppHidden.value = false
    }

    // Telegram Bot Control Methods
    fun updateTelegramSettings(
        enabled: Boolean,
        token: String,
        chatId: String,
        excludeSensitive: Boolean
    ) {
        _isTelegramEnabled.value = enabled
        _telegramBotToken.value = token
        _telegramChatId.value = chatId
        _telegramExcludeSensitive.value = excludeSensitive
        telegramBotManager.saveSettings(enabled, token, chatId, excludeSensitive)
        _telegramTestStatus.value = null
    }

    fun sendTelegramTestMessage() {
        viewModelScope.launch {
            _isTestingTelegram.value = true
            _telegramTestStatus.value = "Mengirim pesan tes ke Telegram..."
            val result = telegramBotManager.testConnection(_telegramBotToken.value, _telegramChatId.value)
            _telegramTestStatus.value = result
            _isTestingTelegram.value = false
        }
    }

    fun hasLocationPermission(): Boolean = locationHelper.hasLocationPermission()
    fun hasBackgroundLocationPermission(): Boolean = locationHelper.hasBackgroundLocationPermission()
    fun isGpsEnabled(): Boolean = locationHelper.isGpsEnabled()
    fun isIgnoringBatteryOptimizations(): Boolean = com.example.utils.AutostartHelper.isIgnoringBatteryOptimizations(context)

    private val _currentLocationState = MutableStateFlow<String?>(null)
    val currentLocationState: StateFlow<String?> = _currentLocationState.asStateFlow()

    private val _isFetchingLocation = MutableStateFlow(false)
    val isFetchingLocation: StateFlow<Boolean> = _isFetchingLocation.asStateFlow()

    fun testCurrentLocation(onComplete: ((String) -> Unit)? = null) {
        _isFetchingLocation.value = true
        _currentLocationState.value = "Sedang mengambil koordinat GPS..."
        locationHelper.getCurrentLocation(
            onSuccess = { loc ->
                _isFetchingLocation.value = false
                val res = "Latitude: ${loc.latitude}\nLongitude: ${loc.longitude}\nAkurasi: ±${loc.accuracy.toInt()} meter\nProvider: ${loc.provider}"
                _currentLocationState.value = res
                onComplete?.invoke("SUCCESS: Koordinat didapat ($res)")
            },
            onError = { err ->
                _isFetchingLocation.value = false
                _currentLocationState.value = "Error: $err"
                onComplete?.invoke("ERROR: $err")
            }
        )
    }

    fun sendLocationToTelegram(onResult: (String) -> Unit) {
        if (!telegramBotManager.isEnabled()) {
            onResult("Bot Telegram belum diaktifkan dalam Pengaturan.")
            return
        }
        locationHelper.getCurrentLocation(
            onSuccess = { location ->
                viewModelScope.launch {
                    val success = telegramBotManager.sendLocation(location.latitude, location.longitude)
                    if (success) {
                        onResult("SUCCESS: Koordinat GPS (${location.latitude}, ${location.longitude}) berhasil dikirim ke Telegram!")
                    } else {
                        onResult("ERROR: Gagal mengirim koordinat ke Telegram. Cek koneksi & bot settings.")
                    }
                }
            },
            onError = { err ->
                onResult("ERROR: $err")
            }
        )
    }

    private fun getTimeRangeForFilter(filter: DateFilter): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance()
        val now = System.currentTimeMillis()
        return when (filter) {
            DateFilter.ALL -> Pair(0L, Long.MAX_VALUE)
            DateFilter.TODAY -> {
                cal.timeInMillis = now
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, Long.MAX_VALUE)
            }
            DateFilter.LAST_7_DAYS -> {
                cal.timeInMillis = now
                cal.add(java.util.Calendar.DAY_OF_YEAR, -7)
                Pair(cal.timeInMillis, Long.MAX_VALUE)
            }
            DateFilter.THIS_MONTH -> {
                cal.timeInMillis = now
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, Long.MAX_VALUE)
            }
        }
    }
}

enum class PinDialogMode {
    UNLOCK, SET_NEW
}
