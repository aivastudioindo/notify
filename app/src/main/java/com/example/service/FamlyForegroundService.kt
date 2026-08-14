package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.telegram.TelegramBotManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class FamlyForegroundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val CHANNEL_ID = "famly_background_service_channel"
        private const val NOTIFICATION_ID = 9901

        fun startService(context: Context) {
            try {
                val intent = Intent(context, FamlyForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("FamlyForegroundService", "Gagal menjalankan Foreground Service: ${e.message}", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, FamlyForegroundService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e("FamlyForegroundService", "Gagal menghentikan Foreground Service: ${e.message}", e)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            startForegroundWithNotification()
            startTelegramPolling()
        } catch (e: Exception) {
            Log.e("FamlyForegroundService", "Error di onCreate: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForegroundWithNotification()
            startTelegramPolling()
        } catch (e: Exception) {
            Log.e("FamlyForegroundService", "Error di onStartCommand: ${e.message}", e)
        }
        return START_STICKY
    }

    private fun startTelegramPolling() {
        try {
            TelegramBotManager.getInstance(applicationContext).startPolling(serviceScope)
        } catch (e: Exception) {
            Log.e("FamlyForegroundService", "Gagal memulai Telegram polling: ${e.message}", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Layanan Latar Belakang Famly",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Layanan aktif untuk merespons perintah Telegram 24/7"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Famly Service Aktif")
            .setContentText("Layanan latar belakang aktif merespons perintah Telegram (/lokasi, /ping, /help)")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("FamlyForegroundService", "Gagal startForeground: ${e.message}", e)
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                Log.e("FamlyForegroundService", "Fallback startForeground failed: ${e2.message}", e2)
            }
        }
    }

    override fun onDestroy() {
        try {
            TelegramBotManager.getInstance(applicationContext).stopPolling()
        } catch (e: Exception) {
            Log.e("FamlyForegroundService", "Error stopping polling di onDestroy: ${e.message}", e)
        }
        try {
            serviceJob.cancel()
        } catch (e: Exception) {
            Log.e("FamlyForegroundService", "Error cancelling serviceJob: ${e.message}", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
