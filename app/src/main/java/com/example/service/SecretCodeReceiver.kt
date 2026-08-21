package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity
import com.example.data.security.IconDisguiseManager

class SecretCodeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val host = intent.data?.host
        Log.d("SecretCodeReceiver", "🔑 Secret Code Broadcast Diterima: $host, Action: ${intent.action}")

        // Re-open application without un-hiding launcher icon
        try {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_OPENED_VIA_SECRET_CODE", true)
            }
            context.startActivity(launchIntent)
            Log.d("SecretCodeReceiver", "🚀 Berhasil meluncurkan MainActivity dari Secret Code.")
        } catch (e: Exception) {
            Log.e("SecretCodeReceiver", "Gagal meluncurkan aplikasi dari Secret Code: ${e.message}", e)
        }
    }
}
