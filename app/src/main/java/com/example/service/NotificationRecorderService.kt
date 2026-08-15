package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.FamlyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class NotificationRecorderService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private data class ParsedMessageItem(
        val sender: String,
        val text: String,
        val time: Long
    )

    companion object {
        private val appNameCache = ConcurrentHashMap<String, String>()
        
        // Cache to store processed individual messages (key = packageName|sender|text|time)
        // Keeps messages for 2 hours to prevent unread messages from being re-sent
        private val processedMessagesCache = ConcurrentHashMap<String, Long>()
        
        // Cache for generic notification signatures
        private val processedNotificationSignatures = ConcurrentHashMap<String, Long>()

        private val summaryPatterns = listOf(
            Regex("""^\d+\s+(pesan\s+baru|new\s+messages|messages|pesan|notifikasi|notifications).*""", RegexOption.IGNORE_CASE),
            Regex("""^\d+\s+(pesan|messages)\s+dari\s+\d+\s+(chat|obrolan|percakapan|kontak|pengirim).*""", RegexOption.IGNORE_CASE),
            Regex("""^\d+\s+(unread\s+messages|pesan\s+belum\s+dibaca).*""", RegexOption.IGNORE_CASE),
            Regex("""^(memeriksa\s+pesan\s+baru|checking\s+for\s+new\s+messages|mencari\s+pesan\s+baru).*""", RegexOption.IGNORE_CASE),
            Regex("""^(whatsapp\s+web\s+sedang\s+aktif|whatsapp\s+web\s+is\s+currently\s+active|whatsapp\s+sedang\s+berjalan).*""", RegexOption.IGNORE_CASE),
            Regex("""^(pencadangan\s+sedang\s+berjalan|cadangan\s+chat|backing\s+up|backup\s+in\s+progress).*""", RegexOption.IGNORE_CASE)
        )

        fun isGenericSummaryText(text: String): Boolean {
            val clean = text.trim().lowercase()
            if (clean.isBlank()) return true
            return summaryPatterns.any { it.matches(clean) }
        }

        fun tryRebindService(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val componentName = ComponentName(context, NotificationRecorderService::class.java)
                    requestRebind(componentName)
                    Log.d("Famly", "Dua arah requestRebind dipanggil untuk NotificationRecorderService.")
                }
            } catch (e: Exception) {
                Log.e("Famly", "Gagal melakukan requestRebind: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Famly", "NotificationRecorderService onCreate dipanggil")
        FamlyForegroundService.startService(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("Famly", "✅ NotificationRecorderService TERHUBUNG dan AKTIF merekam semua notifikasi!")
        FamlyForegroundService.startService(applicationContext)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w("Famly", "⚠️ NotificationRecorderService TERPUTUS! Mencoba sambung ulang...")
        tryRebindService(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // Skip our own app's notifications to prevent infinite loop
        if (packageName == applicationContext.packageName) return

        val notification = sbn.notification ?: return
        
        // 1. FILTER: Ignore Group Summary notifications
        // Android creates group summary notifications (e.g. "3 messages from 2 chats") to group child notifications.
        // If we don't ignore group summary, every new message will re-fire the summary with all past unread messages.
        val isGroupSummary = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        if (isGroupSummary) {
            Log.d("Famly", "Abaikan notifikasi Group Summary dari $packageName")
            return
        }

        // Check Whitelist / Blacklist Filter for Battery & Privacy Optimization
        val filterManager = FamlyApplication.instance.appFilterManager
        if (!filterManager.shouldRecordPackage(packageName)) {
            // Ignored by user's whitelist/blacklist rule - skip immediately to save battery
            return
        }

        val extras = notification.extras
        var title = ""
        var text = ""
        var subText = ""
        var bigText = ""
        var effectivePostTime = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()

        val now = System.currentTimeMillis()

        // Clean up caches periodically if getting too large
        if (processedMessagesCache.size > 1000) {
            val cutoff = now - 7_200_000L // 2 hours
            processedMessagesCache.entries.removeIf { it.value < cutoff }
        }
        if (processedNotificationSignatures.size > 1000) {
            val cutoff = now - 7_200_000L
            processedNotificationSignatures.entries.removeIf { it.value < cutoff }
        }

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

            // 2. MessagingStyle HANDLING (WhatsApp, Telegram, Signal, Messenger, Messages)
            // When multiple unread messages exist, Android bundles ALL unread messages into "android.messages".
            // We extract ONLY the new (unprocessed) messages so previous unread messages are NOT re-sent!
            try {
                val messagesParcelables = extras.getParcelableArray("android.messages")
                if (!messagesParcelables.isNullOrEmpty()) {
                    val allMessages = mutableListOf<ParsedMessageItem>()
                    for (item in messagesParcelables) {
                        if (item is Bundle) {
                            val msgText = item.getCharSequence("text")?.toString()?.trim().orEmpty()
                            val sender = item.getCharSequence("sender")?.toString()?.trim().orEmpty()
                            val msgTime = item.getLong("time", 0L)
                            if (msgText.isNotBlank()) {
                                allMessages.add(ParsedMessageItem(sender = sender, text = msgText, time = msgTime))
                            }
                        }
                    }

                    if (allMessages.isNotEmpty()) {
                        // Find messages that haven't been processed yet
                        val newMessages = allMessages.filter { msg ->
                            val msgKey = "$packageName|${title}|${msg.sender}|${msg.text}|${msg.time}"
                            !processedMessagesCache.containsKey(msgKey)
                        }

                        if (newMessages.isEmpty()) {
                            // All messages in this notification have already been recorded/sent previously!
                            // This is just an unread notification re-trigger from Android OS.
                            Log.d("Famly", "Abaikan pembaruan unread: Semua pesan di $packageName [$title] sudah diproses.")
                            return
                        }

                        // Mark new messages as processed
                        for (msg in newMessages) {
                            val msgKey = "$packageName|${title}|${msg.sender}|${msg.text}|${msg.time}"
                            processedMessagesCache[msgKey] = now
                        }

                        // Formulate the new message text from ONLY the new incoming messages
                        text = if (newMessages.size == 1) {
                            newMessages.first().text
                        } else {
                            newMessages.joinToString("\n") { msg ->
                                if (msg.sender.isNotBlank() && msg.sender != title) "${msg.sender}: ${msg.text}" else msg.text
                            }
                        }
                        
                        bigText = text
                        val latestTime = newMessages.last().time
                        if (latestTime > 0) {
                            effectivePostTime = latestTime
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Famly", "Gagal memproses android.messages: ${e.message}")
            }

            // 3. Handle InboxStyle EXTRA_TEXT_LINES if text is blank or a generic summary
            if (text.isBlank() || isGenericSummaryText(text)) {
                val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                if (!textLines.isNullOrEmpty()) {
                    val newLines = mutableListOf<String>()
                    for (line in textLines) {
                        val lineStr = line.toString().trim()
                        if (lineStr.isNotBlank() && !isGenericSummaryText(lineStr)) {
                            val lineKey = "$packageName|$title|$lineStr"
                            if (!processedMessagesCache.containsKey(lineKey)) {
                                processedMessagesCache[lineKey] = now
                                newLines.add(lineStr)
                            }
                        }
                    }

                    if (newLines.isNotEmpty()) {
                        text = newLines.joinToString("\n")
                    } else if (isGenericSummaryText(text)) {
                        // All lines already seen and text is only summary (e.g. "2 pesan baru")
                        Log.d("Famly", "Abaikan notifikasi ringkasan unread berulang: $packageName -> $text")
                        return
                    }
                }
            }
        }

        // 4. Ignore pure generic summary text (e.g. "2 pesan baru", "3 messages from 2 chats")
        if (isGenericSummaryText(text)) {
            val tickerText = notification.tickerText?.toString()?.trim().orEmpty()
            if (tickerText.isNotBlank() && !isGenericSummaryText(tickerText)) {
                text = tickerText
            } else {
                Log.d("Famly", "Abaikan notifikasi generic summary murni: $packageName [$title] -> $text")
                return
            }
        }

        // Fallback to tickerText if title and text are still empty
        val tickerText = notification.tickerText?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) {
            if (tickerText.isNotBlank() && !isGenericSummaryText(tickerText)) {
                text = tickerText
            } else {
                // If notification has no valid text content, ignore
                return
            }
        }

        // 5. Signature-based Deduplication Check (Prevents exact duplicate notification spam)
        val signatureKey = "$packageName|${title.trim()}|${text.trim()}"
        val lastSeenSignature = processedNotificationSignatures[signatureKey]
        if (lastSeenSignature != null && (now - lastSeenSignature) < 3_600_000L) { // 1 hour duplicate window
            Log.d("Famly", "Abaikan duplikasi notifikasi (1 jam): $signatureKey")
            return
        }
        processedNotificationSignatures[signatureKey] = now

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

        Log.d("Famly", "📥 Notifikasi Baru Ditangkap -> [$appName ($packageName)]: $title | $text")

        serviceScope.launch {
            try {
                FamlyApplication.instance.repository.saveNotification(
                    key = baseKey,
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = subText,
                    bigText = bigText,
                    postTime = effectivePostTime
                )
            } catch (e: Exception) {
                Log.e("Famly", "Gagal menyimpan notifikasi dari $packageName", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}

