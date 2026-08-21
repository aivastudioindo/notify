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

    private const val MAIN_ACTIVITY = "com.example.MainActivity"
    private const val LAUNCHER_ALIAS = "com.example.LauncherAlias"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAppHidden(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_APP_HIDDEN, false)
    }

    /**
     * Sembunyikan atau Munculkan ikon aplikasi dari Launcher/Home Screen.
     * Saat hidden = true (Stealth Mode), ikon akan lenyap dari menu dan layar beranda.
     * Aplikasi tetap bisa dibuka via dial (*#*#7788#*#*) atau link (famly://open).
     */
    fun setAppHidden(context: Context, hidden: Boolean) {
        try {
            val pm = context.packageManager
            val launcherComponent = ComponentName(context, LAUNCHER_ALIAS)
            val mainActivityComponent = ComponentName(context, MAIN_ACTIVITY)

            // Pastikan MainActivity selalu aktif untuk menerima Intent / Dial / Deep Link
            pm.setComponentEnabledSetting(
                mainActivityComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, hidden).apply()

            if (hidden) {
                // Matikan komponen launcher agar ikon hilang seketika dari Layar & Menu HP
                pm.setComponentEnabledSetting(
                    launcherComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "👻 Stealth Mode AKTIF: Ikon aplikasi telah disembunyikan dari Launcher.")
            } else {
                // Aktifkan kembali ikon launcher
                pm.setComponentEnabledSetting(
                    launcherComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "📱 Ikon aplikasi dimunculkan kembali di Launcher.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengubah visibilitas launcher: ${e.message}", e)
        }
    }
}
