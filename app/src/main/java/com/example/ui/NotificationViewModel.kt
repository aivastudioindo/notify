package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.NotifVaultApplication
import com.example.data.local.AppCountResult
import com.example.data.local.CategoryCountResult
import com.example.data.model.AnalyticsSummary
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationItem
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
    ANALYTICS("Statistik & Grafik"),
    CATEGORIES("Kategori Aplikasi"),
    SETTINGS("Pengaturan & Privasi")
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

    private val repository = (application as NotifVaultApplication).repository
    private val encryptionManager = (application as NotifVaultApplication).encryptionManager
    private val context: Context get() = getApplication<Application>().applicationContext

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

    // Security & Vault Lock
    private val _isVaultUnlocked = MutableStateFlow(!encryptionManager.isPinProtectionEnabled())
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _isPinProtectionEnabled = MutableStateFlow(encryptionManager.isPinProtectionEnabled())
    val isPinProtectionEnabled: StateFlow<Boolean> = _isPinProtectionEnabled.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _pinDialogMode = MutableStateFlow<PinDialogMode>(PinDialogMode.UNLOCK)
    val pinDialogMode: StateFlow<PinDialogMode> = _pinDialogMode.asStateFlow()

    // Theme Mode
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Permission state
    private val _hasNotificationAccess = MutableStateFlow(false)
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

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

    // Analytics Summary Flow
    val analyticsSummary: StateFlow<AnalyticsSummary> = repository.getAnalyticsSummary().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsSummary()
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
        // Check if DB is empty on first launch and populate sample data so graphs look vibrant immediately
        viewModelScope.launch {
            repository.getAllNotifications().collect { list ->
                if (list.isEmpty()) {
                    repository.insertSampleData()
                }
            }
        }
    }

    fun checkPermission() {
        _hasNotificationAccess.value = NotificationHelper.isNotificationAccessGranted(context)
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

    fun insertSampleData() {
        viewModelScope.launch {
            repository.insertSampleData()
        }
    }

    fun sendTestNotification(title: String, message: String, subText: String = "NotifVault Test") {
        NotificationHelper.sendSimulatedNotification(context, title, message, subText)
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
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
        _isPinProtectionEnabled.value = false
        _isVaultUnlocked.value = true
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
