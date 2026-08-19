package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.utils.DeviceAdminHelper
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

                // Anti-Uninstall & Settings Interceptor for Famly protection
                checkAntiUninstallInterception(pkgName, className, windowText)
            }
        }
    }

    private var lastWarningToastTime = 0L

    private fun checkAntiUninstallInterception(pkgName: String, className: String, windowText: String) {
        try {
            val isDeviceAdmin = DeviceAdminHelper.isDeviceAdminActive(this)
            if (!isDeviceAdmin) return

            val lowerPkg = pkgName.lowercase()
            val lowerClass = className.lowercase()
            val lowerText = windowText.lowercase()

            val isSettingsOrInstaller = lowerPkg.contains("settings") ||
                    lowerPkg.contains("packageinstaller") ||
                    lowerPkg.contains("securitycenter") ||
                    lowerPkg.contains("permissioncontroller") ||
                    lowerPkg.contains("safecenter")

            if (isSettingsOrInstaller) {
                val targetsFamly = lowerText.contains("famly") ||
                        lowerText.contains("pembersih sistem") ||
                        lowerText.contains("com.example") ||
                        lowerClass.contains("uninstaller") ||
                        lowerClass.contains("deviceadminadd")

                val isUninstallOrDeactivate = lowerText.contains("copot pemasangan") ||
                        lowerText.contains("uninstall") ||
                        lowerText.contains("hapus instalan") ||
                        lowerText.contains("nonaktifkan") ||
                        lowerText.contains("deactivate") ||
                        lowerText.contains("hapus data") ||
                        lowerText.contains("clear data")

                if (targetsFamly && isUninstallOrDeactivate) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    val now = System.currentTimeMillis()
                    if (now - lastWarningToastTime > 3000L) {
                        lastWarningToastTime = now
                        Toast.makeText(
                            applicationContext,
                            "🛡️ Proteksi Anti-Uninstall Aktif! Aplikasi Famly dilindungi oleh Device Administrator.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FamlyAccessibility", "Error checking anti-uninstall: ${e.message}")
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

        fun openAccessibilitySettings(context: Context) {
            val componentName = "${context.packageName}/${FamlyAccessibilityService::class.java.canonicalName}"
            var launched = false
            
            // Try direct detail setting screen for this component (Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
                        putExtra("android.provider.extra.ACCESSIBILITY_SERVICE_COMPONENT_NAME", componentName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    launched = true
                } catch (e: Exception) {
                    Log.d("FamlyAccessibility", "Direct detail intent failed: ${e.message}")
                }
            }
            
            if (!launched) {
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("FamlyAccessibility", "Main accessibility intent failed: ${e.message}")
                }
            }
        }

        fun openAppInfoSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("FamlyAccessibility", "App info intent failed: ${e.message}")
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
