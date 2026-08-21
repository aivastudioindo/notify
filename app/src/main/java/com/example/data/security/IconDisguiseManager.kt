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
    private const val KEY_DISGUISE_ENABLED = "key_disguise_enabled"

    private const val MAIN_ACTIVITY = "com.example.MainActivity"
    private const val LAUNCHER_ALIAS = "com.example.LauncherAlias"
    private const val CLEANER_ALIAS = "com.example.CleanerAlias"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAppHidden(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_APP_HIDDEN, false)
    }

    fun isDisguiseEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DISGUISE_ENABLED, false)
    }

    /**
     * Sembunyikan ikon aplikasi dari Launcher/Home Screen secara total (Background-Only / Stealth Mode).
     * Saat disembunyikan, ikon langsung hilang dari menu HP.
     * Aplikasi tetap bisa dibuka kapan saja via Dial Pad (*#*#7788#*#*) atau Deep Link (famly://open).
     */
    fun setAppHidden(context: Context, hidden: Boolean) {
        try {
            val pm = context.packageManager
            val defaultLauncher = ComponentName(context, LAUNCHER_ALIAS)
            val cleanerLauncher = ComponentName(context, CLEANER_ALIAS)
            val mainActivity = ComponentName(context, MAIN_ACTIVITY)

            // Pastikan MainActivity selalu aktif agar bisa dibuka via Intent / Dial Pad / Deep Link
            pm.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, hidden).apply()

            if (hidden) {
                // Matikan kedua entry Launcher agar ikon hilang total dari Home & Drawer
                pm.setComponentEnabledSetting(
                    defaultLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    cleanerLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "👻 Stealth Mode AKTIF: Ikon aplikasi disembunyikan dari Launcher HP.")
            } else {
                val isDisguise = isDisguiseEnabled(context)
                if (isDisguise) {
                    pm.setComponentEnabledSetting(
                        cleanerLauncher,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        defaultLauncher,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } else {
                    pm.setComponentEnabledSetting(
                        defaultLauncher,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    pm.setComponentEnabledSetting(
                        cleanerLauncher,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
                Log.d(TAG, "📱 Ikon aplikasi dimunculkan kembali di Launcher.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengubah status sembunyi aplikasi: ${e.message}", e)
        }
    }

    /**
     * Ubah penyamaran ikon Pembersih Sistem
     */
    fun setCleanerDisguise(context: Context, enabled: Boolean) {
        try {
            val pm = context.packageManager
            val defaultLauncher = ComponentName(context, LAUNCHER_ALIAS)
            val cleanerLauncher = ComponentName(context, CLEANER_ALIAS)

            getPrefs(context).edit().putBoolean(KEY_DISGUISE_ENABLED, enabled).apply()

            val isHidden = isAppHidden(context)
            if (isHidden) {
                // Jika sedang mode sembunyi, jangan tampilkan ikon apa pun
                pm.setComponentEnabledSetting(
                    defaultLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    cleanerLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                return
            }

            if (enabled) {
                pm.setComponentEnabledSetting(
                    cleanerLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    defaultLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Ikon Penyamaran Pembersih Sistem Diaktifkan.")
            } else {
                pm.setComponentEnabledSetting(
                    defaultLauncher,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    cleanerLauncher,
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
