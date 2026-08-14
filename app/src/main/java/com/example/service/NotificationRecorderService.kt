package com.example.service

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.NotifVaultApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import java.util.concurrent.ConcurrentHashMap

class NotificationRecorderService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val appNameCache = ConcurrentHashMap<String, String>()
    }

    override fun onCreate() {
        super.onCreate()
        FamlyForegroundService.startService(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotifVault", "NotificationRecorderService terhubung dan aktif merekam!")
        FamlyForegroundService.startService(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // Skip our own notifications
        if (packageName == applicationContext.packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        // Ignore empty title and text notifications
        if (title.isBlank() && text.isBlank() && bigText.isBlank()) return

        val appName = appNameCache.getOrPut(packageName) {
            try {
                val pm = applicationContext.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName.substringAfterLast('.')
            }
        }

        // Use the stable sbn.key provided by Android OS so updates to the same notification replace cleanly
        val key = sbn.key ?: "${packageName}_${sbn.id}"
        val postTime = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()

        serviceScope.launch {
            try {
                NotifVaultApplication.instance.repository.saveNotification(
                    key = key,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = subText,
                    bigText = bigText,
                    postTime = postTime
                )
            } catch (e: Exception) {
                Log.e("NotifVault", "Gagal menyimpan notifikasi", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
