package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FamlyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceActive = true
        Log.d("FamlyAccessibility", "Layanan Aksesibilitas Famly terhubung!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val pkgName = event.packageName?.toString() ?: return
                if (pkgName == "com.android.systemui" || pkgName == "com.google.android.inputmethod.latin") return
                
                val appLabel = getAppNameFromPackage(pkgName)
                val className = event.className?.toString() ?: ""
                val windowText = event.text.joinToString(" ")
                
                _currentActiveApp.value = ActiveAppInfo(
                    packageName = pkgName,
                    appName = appLabel,
                    className = className,
                    windowTitle = windowText,
                    timestamp = System.currentTimeMillis()
                )
                
                Log.d("FamlyAccessibility", "Anak sedang membuka: $appLabel ($pkgName)")
            }
        }
    }

    override fun onInterrupt() {
        isServiceActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceActive = false
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    data class ActiveAppInfo(
        val packageName: String = "",
        val appName: String = "Layar Utama / Tidak Diketahui",
        val className: String = "",
        val windowTitle: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    companion object {
        var isServiceActive: Boolean = false
            private set

        private val _currentActiveApp = MutableStateFlow(ActiveAppInfo())
        val currentActiveApp: StateFlow<ActiveAppInfo> = _currentActiveApp.asStateFlow()

        fun getActiveAppSummary(): String {
            val current = currentActiveApp.value
            return if (current.packageName.isNotEmpty()) {
                "📱 <b>Aplikasi Aktif:</b> ${current.appName}\n📦 <b>Package:</b> <code>${current.packageName}</code>"
            } else {
                "📱 <b>Aplikasi Aktif:</b> Layar Utama / Peluncur HP"
            }
        }
    }
}
