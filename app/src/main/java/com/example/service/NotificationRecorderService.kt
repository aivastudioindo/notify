package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

        fun tryRebindService(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val componentName = ComponentName(context, NotificationRecorderService::class.java)
                    requestRebind(componentName)
                    Log.d("NotifVault", "Dua arah requestRebind dipanggil untuk NotificationRecorderService.")
                }
            } catch (e: Exception) {
                Log.e("NotifVault", "Gagal melakukan requestRebind: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NotifVault", "NotificationRecorderService onCreate dipanggil")
        FamlyForegroundService.startService(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotifVault", "✅ NotificationRecorderService TERHUBUNG dan AKTIF merekam semua notifikasi!")
        FamlyForegroundService.startService(applicationContext)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w("NotifVault", "⚠️ NotificationRecorderService TERPUTUS! Mencoba sambung ulang...")
        tryRebindService(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // Skip our own app's notifications to prevent infinite loop
        if (packageName == applicationContext.packageName) return

        // Check Whitelist / Blacklist Filter for Battery & Privacy Optimization
        val filterManager = NotifVaultApplication.instance.appFilterManager
        if (!filterManager.shouldRecordPackage(packageName)) {
            // Ignored by user's whitelist/blacklist rule - skip immediately to save battery
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras

        var title = ""
        var text = ""
        var subText = ""
        var bigText = ""

        if (extras != null) {
            val titleCharSeq = extras.getCharSequence(Notification.EXTRA_TITLE)
                ?: extras.getCharSequence("android.title.big")
                ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)

            title = titleCharSeq?.toString()?.trim().orEmpty()

            val textCharSeq = extras.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_INFO_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)

            text = textCharSeq?.toString()?.trim().orEmpty()

            subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim().orEmpty()
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()

            // Handle MessagingStyle array ("android.messages" / EXTRA_MESSAGES) for WhatsApp
            try {
                val messagesParcelables = extras.getParcelableArray("android.messages")
                if (!messagesParcelables.isNullOrEmpty()) {
                    val extractedMessages = mutableListOf<String>()
                    for (item in messagesParcelables) {
                        if (item is android.os.Bundle) {
                            val msgText = item.getCharSequence("text")?.toString()?.trim()
                            val sender = item.getCharSequence("sender")?.toString()?.trim()
                            if (!msgText.isNullOrBlank()) {
                                if (!sender.isNullOrBlank()) {
                                    extractedMessages.add("$sender: $msgText")
                                } else {
                                    extractedMessages.add(msgText)
                                }
                            }
                        }
                    }
                    if (extractedMessages.isNotEmpty()) {
                        val fullExtracted = extractedMessages.distinct().joinToString("\n")
                        if (fullExtracted.isNotBlank()) {
                            bigText = if (bigText.isNotBlank()) "$bigText\n$fullExtracted" else fullExtracted
                            if (text.isBlank()) text = extractedMessages.last()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotifVault", "Gagal memproses android.messages: ${e.message}")
            }

            // Handle EXTRA_TEXT_LINES if text is still blank
            if (text.isBlank()) {
                val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                if (!textLines.isNullOrEmpty()) {
                    text = textLines.joinToString("\n") { it.toString() }.trim()
                }
            }
        }

        // Fallback to tickerText if title and text are still empty
        val tickerText = notification.tickerText?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) {
            if (tickerText.isNotBlank()) {
                text = tickerText
            } else {
                // If notification has no text content at all (purely silent icon/badge), ignore
                return
            }
        }

        // Custom label mapping for WhatsApp and WhatsApp Business
        val appName = when (packageName) {
            "com.whatsapp" -> "WhatsApp"
            "com.whatsapp.w4b" -> "WhatsApp Business"
            else -> appNameCache.getOrPut(packageName) {
                try {
                    val pm = applicationContext.packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                }
            }
        }

        if (title.isBlank()) {
            title = appName
        }

        // Enhanced detection for WhatsApp Voice & Video Calls
        val isWhatsApp = packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b"
        val isCallCategory = notification.category == Notification.CATEGORY_CALL
        val lowerContent = "$title $text $subText".lowercase()
        val isCallKeyword = lowerContent.contains("panggilan") || lowerContent.contains("call") ||
                lowerContent.contains("memanggil") || lowerContent.contains("terlewat") || lowerContent.contains("missed")

        if (isWhatsApp && (isCallCategory || isCallKeyword)) {
            if (subText.isBlank()) {
                subText = if (lowerContent.contains("video")) "Panggilan Video WhatsApp" else "Panggilan Suara WhatsApp"
            }
        }

        val baseKey = sbn.key ?: "${packageName}_${sbn.id}"
        val postTime = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()

        Log.d("NotifVault", "📥 Notifikasi Ditangkap -> [$appName ($packageName)]: $title | $text")

        serviceScope.launch {
            try {
                NotifVaultApplication.instance.repository.saveNotification(
                    key = baseKey,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = subText,
                    bigText = bigText,
                    postTime = postTime
                )
            } catch (e: Exception) {
                Log.e("NotifVault", "Gagal menyimpan notifikasi dari $packageName", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
