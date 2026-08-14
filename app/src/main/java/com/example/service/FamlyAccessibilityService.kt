package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FamlyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
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
        // Kept active if possible
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance == this) {
            instance = null
        }
        return super.onUnbind(intent)
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
        var instance: FamlyAccessibilityService? = null
            private set

        val isServiceActive: Boolean
            get() = instance != null

        private val _currentActiveApp = MutableStateFlow(ActiveAppInfo())
        val currentActiveApp: StateFlow<ActiveAppInfo> = _currentActiveApp.asStateFlow()

        fun isAccessibilityPermissionGranted(context: Context): Boolean {
            if (isServiceActive) return true
            return try {
                val expectedService = "${context.packageName}/${FamlyAccessibilityService::class.java.canonicalName}"
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: ""
                enabledServices.split(":").any {
                    it.equals(expectedService, ignoreCase = true) || it.contains("FamlyAccessibilityService")
                }
            } catch (e: Exception) {
                false
            }
        }

        fun getActiveAppSummary(context: Context): String {
            val isGranted = isAccessibilityPermissionGranted(context)
            val statusText = if (isGranted) "🟢 Aktif" else "🔴 Belum Diaktifkan (Pengaturan > Aksesibilitas > Famly)"
            val current = currentActiveApp.value
            val appText = if (current.packageName.isNotEmpty()) {
                "📱 <b>Aplikasi Aktif:</b> ${current.appName}\n📦 <b>Package:</b> <code>${current.packageName}</code>"
            } else {
                "📱 <b>Aplikasi Aktif:</b> Layar Utama / Peluncur HP"
            }
            return "$appText\n⚙️ <b>Layanan Aksesibilitas:</b> $statusText"
        }

        fun takeAccessibilityScreenshot(context: Context, callback: (Bitmap?) -> Unit) {
            val service = instance
            if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    service.takeScreenshot(
                        Display.DEFAULT_DISPLAY,
                        context.mainExecutor,
                        object : TakeScreenshotCallback {
                            override fun onSuccess(screenshotResult: ScreenshotResult) {
                                try {
                                    val hardwareBuffer = screenshotResult.hardwareBuffer
                                    val colorSpace = screenshotResult.colorSpace
                                    val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                                    val softwareBitmap = bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                                    hardwareBuffer.close()
                                    callback(softwareBitmap)
                                } catch (e: Exception) {
                                    Log.e("FamlyAccessibility", "Error processing screenshot hardware buffer: ${e.message}")
                                    callback(null)
                                }
                            }

                            override fun onFailure(errorCode: Int) {
                                Log.e("FamlyAccessibility", "takeScreenshot failed with error code: $errorCode")
                                callback(null)
                            }
                        }
                    )
                    return
                } catch (e: Exception) {
                    Log.e("FamlyAccessibility", "Error invoking takeScreenshot: ${e.message}")
                }
            }
            callback(null)
        }
    }
}
