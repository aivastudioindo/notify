package com.example.data.security

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log

object IconDisguiseManager {

    private const val TAG = "IconDisguiseManager"
    private const val PREFS_NAME = "famly_stealth_prefs"
    private const val KEY_APP_HIDDEN = "key_app_hidden"
    private const val KEY_SETUP_COMPLETED = "key_setup_completed"

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
        val pm = context.packageManager
        val pkgName = context.packageName
        Log.d(TAG, "setAppHidden called: hidden=$hidden, pkgName=$pkgName")

        getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, hidden).apply()

        val newState = if (hidden) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        // 1. Always ensure MainActivity is ENABLED so dial pad, deep links, and services continue to function
        val mainCandidates = listOf(
            ComponentName(pkgName, "com.example.MainActivity"),
            ComponentName(pkgName, "$pkgName.MainActivity")
        )
        for (comp in mainCandidates) {
            try {
                pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Ensured MainActivity ENABLED: $comp")
            } catch (e: Exception) {
                Log.w(TAG, "MainActivity component candidate $comp: ${e.message}")
            }
        }

        // 2. Discover all active Launcher components dynamically for this package
        var matchedComponentsCount = 0
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(pkgName)
            }
            val resolvedList = pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            Log.d(TAG, "Found ${resolvedList.size} dynamic launcher activities for $pkgName")

            for (resolveInfo in resolvedList) {
                val actInfo = resolveInfo.activityInfo
                if (actInfo != null && actInfo.packageName == pkgName) {
                    val comp = ComponentName(actInfo.packageName, actInfo.name)
                    try {
                        pm.setComponentEnabledSetting(comp, newState, PackageManager.DONT_KILL_APP)
                        matchedComponentsCount++
                        Log.d(TAG, "Dynamically updated launcher component $comp -> $newState")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating dynamic launcher component $comp: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying dynamic launcher activities: ${e.message}", e)
        }

        // 3. Explicitly target all known Launcher Alias variations to guarantee coverage
        val aliasCandidates = listOf(
            ComponentName(pkgName, "$pkgName.LauncherAlias"),
            ComponentName(pkgName, "com.example.LauncherAlias"),
            ComponentName("com.example", "com.example.LauncherAlias"),
            ComponentName(pkgName, "$pkgName.CleanerAlias"),
            ComponentName(pkgName, "com.example.CleanerAlias")
        )

        for (comp in aliasCandidates) {
            try {
                pm.setComponentEnabledSetting(comp, newState, PackageManager.DONT_KILL_APP)
                matchedComponentsCount++
                Log.d(TAG, "Explicitly setComponentEnabledSetting on $comp -> $newState")
            } catch (e: Exception) {
                // Ignore candidate not found logs as we try multiple combinations
                Log.v(TAG, "Candidate $comp not present in manifest: ${e.message}")
            }
        }

        Log.i(TAG, "Stealth Mode updated successfully ($matchedComponentsCount operations performed). App hidden=$hidden")
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
