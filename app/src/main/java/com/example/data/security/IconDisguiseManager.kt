package com.example.data.security

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

object IconDisguiseManager {

    private const val TAG = "IconDisguiseManager"
    private const val MAIN_ACTIVITY = "com.example.MainActivity"
    private const val CALCULATOR_ALIAS = "com.example.CalculatorAlias"

    fun setCalculatorDisguise(context: Context, enabled: Boolean) {
        try {
            val pm = context.packageManager
            val defaultComponent = ComponentName(context, MAIN_ACTIVITY)
            val aliasComponent = ComponentName(context, CALCULATOR_ALIAS)

            if (enabled) {
                // Enable Calculator Alias Icon
                pm.setComponentEnabledSetting(
                    aliasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                // Disable Original NotifVault Icon
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Ikon Penyamaran Kalkulator Ditingkatkan / Diaktifkan.")
            } else {
                // Enable Original NotifVault Icon
                pm.setComponentEnabledSetting(
                    defaultComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                // Disable Calculator Alias Icon
                pm.setComponentEnabledSetting(
                    aliasComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Log.d(TAG, "Ikon Asli NotifVault Dipulihkan.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengubah ikon penyamaran launcher: ${e.message}", e)
        }
    }
}
