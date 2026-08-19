package com.example.service

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class FamlyDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "🛡️ Proteksi Anti-Uninstall Famly Berhasil Diaktifkan!",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "⚠️ PERINGATAN: Menonaktifkan Device Administrator akan mematikan perlindungan keselamatan anak dan memungkinkan aplikasi Famly di-uninstall!"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "⚠️ Proteksi Administrator Dinonaktifkan.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
