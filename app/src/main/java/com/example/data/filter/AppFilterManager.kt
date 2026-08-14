package com.example.data.filter

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppFilterMode {
    BLACKLIST, // Merekam semua aplikasi kecuali yang ada di Blacklist (Hemat & Fleksibel)
    WHITELIST  // Hanya merekam aplikasi yang ada di Whitelist (Sangat Hemat Baterai & Privat)
}

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean = false
)

class AppFilterManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("famly_app_filter_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_FILTER_MODE = "pref_app_filter_mode"
        private const val PREF_BLACKLIST = "pref_app_blacklist"
        private const val PREF_WHITELIST = "pref_app_whitelist"

        // Default apps to blacklist (frequent spam/system progress that drains battery)
        val DEFAULT_BLACKLIST = setOf(
            "com.android.vending", // Google Play Store
            "com.google.android.gms", // Google Play Services
            "com.google.android.googlequicksearchbox", // Google App
            "com.android.providers.downloads", // Download Provider
            "com.android.systemui", // System UI
            "com.android.settings" // Settings
        )

        // Recommended whitelist presets
        val DEFAULT_WHITELIST = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.bca",
            "id.co.bri.brimo",
            "id.co.bankbni.mobile",
            "id.bmri.livin",
            "com.dana",
            "com.tokopedia.tkpd",
            "com.shopee.id"
        )

        @Volatile
        private var INSTANCE: AppFilterManager? = null

        fun getInstance(context: Context): AppFilterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppFilterManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val _filterMode = MutableStateFlow(loadFilterMode())
    val filterMode: StateFlow<AppFilterMode> = _filterMode.asStateFlow()

    private val _blacklist = MutableStateFlow(loadBlacklist())
    val blacklist: StateFlow<Set<String>> = _blacklist.asStateFlow()

    private val _whitelist = MutableStateFlow(loadWhitelist())
    val whitelist: StateFlow<Set<String>> = _whitelist.asStateFlow()

    private fun loadFilterMode(): AppFilterMode {
        val modeStr = prefs.getString(PREF_FILTER_MODE, AppFilterMode.BLACKLIST.name)
        return try {
            AppFilterMode.valueOf(modeStr ?: AppFilterMode.BLACKLIST.name)
        } catch (e: Exception) {
            AppFilterMode.BLACKLIST
        }
    }

    private fun loadBlacklist(): Set<String> {
        val saved = prefs.getStringSet(PREF_BLACKLIST, null)
        return saved ?: DEFAULT_BLACKLIST
    }

    private fun loadWhitelist(): Set<String> {
        val saved = prefs.getStringSet(PREF_WHITELIST, null)
        return saved ?: DEFAULT_WHITELIST
    }

    fun setFilterMode(mode: AppFilterMode) {
        prefs.edit().putString(PREF_FILTER_MODE, mode.name).apply()
        _filterMode.value = mode
    }

    fun addToBlacklist(packageName: String) {
        val updated = _blacklist.value.toMutableSet().apply { add(packageName) }
        prefs.edit().putStringSet(PREF_BLACKLIST, updated).apply()
        _blacklist.value = updated
    }

    fun removeFromBlacklist(packageName: String) {
        val updated = _blacklist.value.toMutableSet().apply { remove(packageName) }
        prefs.edit().putStringSet(PREF_BLACKLIST, updated).apply()
        _blacklist.value = updated
    }

    fun addToWhitelist(packageName: String) {
        val updated = _whitelist.value.toMutableSet().apply { add(packageName) }
        prefs.edit().putStringSet(PREF_WHITELIST, updated).apply()
        _whitelist.value = updated
    }

    fun removeFromWhitelist(packageName: String) {
        val updated = _whitelist.value.toMutableSet().apply { remove(packageName) }
        prefs.edit().putStringSet(PREF_WHITELIST, updated).apply()
        _whitelist.value = updated
    }

    fun resetToDefaults() {
        prefs.edit()
            .putString(PREF_FILTER_MODE, AppFilterMode.BLACKLIST.name)
            .putStringSet(PREF_BLACKLIST, DEFAULT_BLACKLIST)
            .putStringSet(PREF_WHITELIST, DEFAULT_WHITELIST)
            .apply()
        _filterMode.value = AppFilterMode.BLACKLIST
        _blacklist.value = DEFAULT_BLACKLIST
        _whitelist.value = DEFAULT_WHITELIST
    }

    /**
     * Check if a notification from a given package should be recorded.
     * Evaluated synchronously and fast in O(1) time.
     */
    fun shouldRecordPackage(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val mode = _filterMode.value
        return when (mode) {
            AppFilterMode.BLACKLIST -> {
                // Jangan rekam jika ada di blacklist
                !_blacklist.value.contains(packageName)
            }
            AppFilterMode.WHITELIST -> {
                // Hanya rekam jika ada di whitelist
                _whitelist.value.contains(packageName)
            }
        }
    }

    /**
     * Helper to get all installed apps on device for user selection dialog
     */
    fun getInstalledApps(): List<AppItem> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val list = mutableListOf<AppItem>()

        for (appInfo in packages) {
            // Exclude our own app
            if (appInfo.packageName == context.packageName) continue

            val appName = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                appInfo.packageName.substringAfterLast('.')
            }
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            list.add(AppItem(packageName = appInfo.packageName, appName = appName, isSystem = isSystem))
        }

        return list.sortedBy { it.appName.lowercase() }
    }

    fun getAppNameForPackage(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            when (packageName) {
                "com.whatsapp" -> "WhatsApp"
                "com.whatsapp.w4b" -> "WhatsApp Business"
                "org.telegram.messenger" -> "Telegram"
                "com.google.android.apps.messaging" -> "Pesan SMS"
                "com.android.mms" -> "Pesan SMS"
                "com.bca" -> "BCA mobile"
                "id.co.bri.brimo" -> "BRImo"
                "id.co.bankbni.mobile" -> "BNI Mobile Banking"
                "id.bmri.livin" -> "Livin' by Mandiri"
                "com.dana" -> "DANA"
                "com.tokopedia.tkpd" -> "Tokopedia"
                "com.shopee.id" -> "Shopee"
                "com.android.vending" -> "Google Play Store"
                "com.google.android.gms" -> "Google Play Services"
                "com.android.systemui" -> "System UI"
                "com.android.providers.downloads" -> "Pengelola Unduhan"
                else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }
}
