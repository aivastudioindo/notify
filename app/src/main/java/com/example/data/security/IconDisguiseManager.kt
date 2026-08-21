package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object IconDisguiseManager {

    private const val TAG = "IconDisguiseManager"
    private const val PREFS_NAME = "famly_stealth_prefs"
    private const val KEY_APP_HIDDEN = "key_app_hidden"
    private const val KEY_DISGUISE_ENABLED = "key_disguise_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Aplikasi sekarang beroperasi secara murni Headless / Background-Only (Tanpa Ikon di Launcher).
     */
    fun isAppHidden(context: Context): Boolean {
        return true
    }

    fun isDisguiseEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DISGUISE_ENABLED, false)
    }

    fun setAppHidden(context: Context, hidden: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_APP_HIDDEN, hidden).apply()
        Log.d(TAG, "Status Headless App: $hidden")
    }

    fun setCleanerDisguise(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DISGUISE_ENABLED, enabled).apply()
        Log.d(TAG, "Status Penyamaran: $enabled")
    }
}
