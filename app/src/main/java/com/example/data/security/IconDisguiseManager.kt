package com.example.data.security

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log

object IconDisguiseManager {

    private const val TAG = "IconDisguiseManager"
    private const val PREFS_NAME = "famly_stealth_prefs"
    private const val KEY_APP_HIDDEN = "key_app_hidden"
    private const val KEY_SETUP_COMPLETED = "key_setup_completed"

    private const val MAIN_ACTIVITY = "com.example.MainActivity"
    private const val LAUNCHER_ALIAS = "com.example.LauncherAlias"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAppHidden(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_APP_HIDDEN, false)
    }

    fun isSetupCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SETUP_COMPLETED, false)
    }

    /**
     * Programmatically hide or show the app icon from the Launcher using PackageManager.setComponentEnabledSetting.
     * When hidden = true (Stealth Mode), the Launcher alias is disabled, hiding the app icon from the home screen
     * and app drawer while keeping MainActivity enabled for secret code dial (*#*#7788#*#*) and deep link (famly://open).
     */
    fun setAppHidden(context: Context, hidden: Boolean) {
        try {
            val pm = context.packageManager
            val launcherComponent = ComponentName(context, LAUNCHER_ALIAS)
            val mainActivityComponent = ComponentName(context, MAIN_ACTIVITY)

            // Ensure MainActivity is always enabled so it can be launched via Intent / Dial Pad / Deep Link
            pm.setComponentEnabledSetting(
                mainActivityComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, hidden).apply()

            val state = if (hidden) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            // Update component enabled setting on the launcher activity-alias
            pm.setComponentEnabledSetting(
                launcherComponent,
                state,
                PackageManager.DONT_KILL_APP
            )
            Log.d(TAG, "PackageManager: setComponentEnabledSetting $LAUNCHER_ALIAS -> ${if (hidden) "DISABLED" else "ENABLED"}")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengubah visibilitas launcher via PackageManager: ${e.message}", e)
        }
    }

    /**
     * Programmatically completes initial setup flow and immediately hides the app icon from launcher.
     */
    fun completeSetupAndHideLauncher(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_SETUP_COMPLETED, true).apply()
        setAppHidden(context, true)
        Log.d(TAG, "Alur setup awal selesai. Ikon aplikasi disembunyikan secara terprogram.")
    }
}
