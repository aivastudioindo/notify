package com.example.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.service.FamlyDeviceAdminReceiver

object DeviceAdminHelper {

    fun getComponentName(context: Context): ComponentName {
        return ComponentName(context, FamlyDeviceAdminReceiver::class.java)
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        return try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            dpm?.isAdminActive(getComponentName(context)) == true
        } catch (e: Exception) {
            Log.e("DeviceAdminHelper", "Error checking device admin status: ${e.message}")
            false
        }
    }

    fun requestDeviceAdmin(activity: Activity, requestCode: Int = 1010) {
        try {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(activity))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Aktifkan Administrator Perangkat ini untuk mengunci aplikasi Famly agar TIDAK BISA DI-UNINSTALL oleh anak atau pengguna lain tanpa izin orang tua."
                )
            }
            activity.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            Log.e("DeviceAdminHelper", "Error requesting device admin: ${e.message}")
            // Fallback to general security settings if action fails
            try {
                activity.startActivity(Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS))
            } catch (ex: Exception) {
                Log.e("DeviceAdminHelper", "Error opening security settings: ${ex.message}")
            }
        }
    }

    fun openDeviceAdminSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("DeviceAdminHelper", "Error opening device admin settings: ${e.message}")
        }
    }

    fun removeDeviceAdmin(context: Context) {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            dpm?.removeActiveAdmin(getComponentName(context))
        } catch (e: Exception) {
            Log.e("DeviceAdminHelper", "Error removing device admin: ${e.message}")
        }
    }
}
