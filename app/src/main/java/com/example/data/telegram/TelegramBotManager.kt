package com.example.data.telegram

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.location.LocationHelper
import com.example.data.scanner.NetworkScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelegramBotManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notif_vault_telegram_prefs", Context.MODE_PRIVATE)

    @Volatile
    private var pollingJob: Job? = null
    @Volatile
    private var lastUpdateId: Long = 0

    companion object {
        private const val PREF_IS_ENABLED = "telegram_is_enabled"
        private const val PREF_BOT_TOKEN = "telegram_bot_token"
        private const val PREF_CHAT_ID = "telegram_chat_id"
        private const val PREF_EXCLUDE_SENSITIVE = "telegram_exclude_sensitive"

        @Volatile
        private var INSTANCE: TelegramBotManager? = null

        fun getInstance(context: Context): TelegramBotManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TelegramBotManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun isEnabled(): Boolean {
        val token = getBotToken()
        val chatId = getChatId()
        if (token.isBlank() || chatId.isBlank()) return false
        return prefs.getBoolean(PREF_IS_ENABLED, true)
    }

    fun getBotToken(): String = prefs.getString(PREF_BOT_TOKEN, "")?.trim() ?: ""

    fun getChatId(): String = prefs.getString(PREF_CHAT_ID, "")?.trim() ?: ""

    fun isExcludeSensitive(): Boolean = prefs.getBoolean(PREF_EXCLUDE_SENSITIVE, false)

    fun saveSettings(
        enabled: Boolean,
        botToken: String,
        chatId: String,
        excludeSensitive: Boolean
    ) {
        prefs.edit()
            .putBoolean(PREF_IS_ENABLED, enabled)
            .putString(PREF_BOT_TOKEN, botToken.trim())
            .putString(PREF_CHAT_ID, chatId.trim())
            .putBoolean(PREF_EXCLUDE_SENSITIVE, excludeSensitive)
            .apply()

        if (!enabled || botToken.isBlank()) {
            stopPolling()
        }
    }

    @Synchronized
    fun startPolling(scope: CoroutineScope? = null) {
        if (pollingJob?.isActive == true) {
            Log.d("TelegramBotManager", "Polling sudah aktif running, abaikan pemicu ganda.")
            return
        }
        val token = getBotToken()
        if (!isEnabled() || token.isBlank()) {
            Log.d("TelegramBotManager", "Telegram bot tidak diaktifkan atau token belum diisi.")
            return
        }

        val targetScope = scope ?: CoroutineScope(Dispatchers.IO)
        pollingJob = targetScope.launch(Dispatchers.IO) {
            Log.d("TelegramBotManager", "Memulai Telegram Long Polling di Service...")
            while (isActive && isEnabled()) {
                val currentToken = getBotToken()
                if (currentToken.isBlank()) {
                    delay(5000L)
                    continue
                }

                try {
                    pollUpdates(currentToken)
                } catch (e: Exception) {
                    Log.e("TelegramBotManager", "Exception pada polling Telegram: ${e.message}")
                    delay(3000L)
                }
            }
        }
    }

    @Synchronized
    fun stopPolling() {
        Log.d("TelegramBotManager", "Menghentikan Telegram Long Polling...")
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollUpdates(token: String) {
        val configuredChatId = getChatId()

        try {
            val urlString = "https://api.telegram.org/bot$token/getUpdates?offset=${lastUpdateId + 1}&timeout=15"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 15000
            conn.readTimeout = 25000

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                if (json.optBoolean("ok")) {
                    val result = json.optJSONArray("result") ?: return
                    for (i in 0 until result.length()) {
                        val update = result.getJSONObject(i)
                        val updateId = update.optLong("update_id")
                        if (updateId > lastUpdateId) {
                            lastUpdateId = updateId
                        }

                        val message = update.optJSONObject("message") ?: continue
                        val text = message.optString("text", "").trim()
                        if (text.isBlank()) continue

                        val fromChat = message.optJSONObject("chat")
                        val messageChatId = fromChat?.optLong("id")?.toString()?.trim() ?: ""

                        // SECURITY: ALLOWED_CHAT_ID Verification
                        if (configuredChatId.isNotBlank() && messageChatId != configuredChatId) {
                            Log.w(
                                "TelegramBotManager",
                                "Mengabaikan perintah dari Chat ID tidak terdaftar: '$messageChatId' (Allowed Chat ID: '$configuredChatId')"
                            )
                            continue
                        }

                        val command = parseCommand(text)
                        if (command.isNotBlank()) {
                            val targetChatId = if (messageChatId.isNotBlank()) messageChatId else configuredChatId
                            if (targetChatId.isNotBlank()) {
                                handleCommand(command, targetChatId)
                            }
                        }
                    }
                }
            } else {
                Log.e("TelegramBotManager", "HTTP Polling Error Code: $responseCode")
                delay(3000L)
            }
        } catch (e: IOException) {
            Log.e("TelegramBotManager", "Network IOException pada polling Telegram: ${e.message}")
            delay(3000L)
        } catch (e: Exception) {
            Log.e("TelegramBotManager", "Error tak terduga pada polling Telegram: ${e.message}")
            delay(3000L)
        }
    }

    private fun parseCommand(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("/")) return ""

        val firstWord = trimmed.split("\\s+".toRegex()).firstOrNull() ?: return ""
        return firstWord.substringBefore("@").lowercase(Locale.ROOT)
    }

    private fun handleCommand(command: String, chatId: String) {
        val token = getBotToken()
        if (token.isBlank() || chatId.isBlank()) return

        val locationHelper = LocationHelper(context)
        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMM yyyy", Locale.getDefault()).format(Date())

        when (command) {
            "/lokasi", "/location", "/where" -> {
                if (!locationHelper.hasLocationPermission()) {
                    executeSendMessage(
                        token,
                        chatId,
                        "⚠️ <b>Izin lokasi belum diberikan pada HP anak.</b>\n<i>Mohon izinkan akses lokasi di aplikasi Famly pada HP anak.</i>"
                    )
                    return
                }

                if (!locationHelper.isGpsEnabled()) {
                    executeSendMessage(
                        token,
                        chatId,
                        "⚠️ <b>GPS / Lokasi pada HP anak sedang NONAKTIF.</b>\n<i>Mohon nyalakan GPS di HP anak.</i>"
                    )
                    return
                }

                executeSendMessage(
                    token,
                    chatId,
                    "⏳ <i>[GPS] Mengambil koordinat GPS HP anak... Mohon tunggu sebentar.</i>"
                )

                locationHelper.getCurrentLocation(
                    onSuccess = { loc ->
                        CoroutineScope(Dispatchers.IO).launch {
                            sendLocationPin(token, chatId, loc.latitude, loc.longitude)
                            sendLocationDetailsText(token, chatId, loc.latitude, loc.longitude, timeStr)
                        }
                    },
                    onError = { err ->
                        CoroutineScope(Dispatchers.IO).launch {
                            executeSendMessage(
                                token,
                                chatId,
                                "⚠️ <b>Gagal Mengambil Lokasi:</b>\n$err"
                            )
                        }
                    }
                )
            }
            "/ping" -> {
                val isGpsActive = locationHelper.isGpsEnabled()
                val gpsStatusText = if (isGpsActive) "AKTIF" else "NONAKTIF"
                val replyMsg = StringBuilder().apply {
                    append("HP anak online ✅\n")
                    append("GPS: $gpsStatusText\n")
                    append("Service: AKTIF\n")
                    append("Waktu: $timeStr")
                }.toString()

                executeSendMessage(token, chatId, replyMsg)
            }
            "/scan", "/wifi", "/bluetooth" -> {
                executeSendMessage(
                    token,
                    chatId,
                    "🔍 <b>[Scan] Memulai pemindaian Wi-Fi & Bluetooth di sekitar HP...</b>\n<i>Mohon tunggu beberapa detik.</i>"
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val networkScanner = NetworkScanner(context)
                        val wifiList = networkScanner.scanWifiNetworks()
                        val btList = networkScanner.scanBluetoothDevices()

                        val sb = StringBuilder()
                        sb.append("📡 <b>HASIL PEMINDAIAN JARINGAN SEKITAR</b>\n\n")

                        sb.append("📶 <b>Jaringan Wi-Fi (${wifiList.size} Ditemukan):</b>\n")
                        if (wifiList.isEmpty()) {
                            sb.append("<i>Tidak ada sinyal Wi-Fi terdeteksi / izin lokasi belum aktif.</i>\n\n")
                        } else {
                            wifiList.take(10).forEachIndexed { idx, wifi ->
                                sb.append("${idx + 1}. <b>${escapeHtml(wifi.ssid)}</b>\n")
                                sb.append("   • MAC/BSSID: <code>${wifi.bssid}</code>\n")
                                sb.append("   • Sinyal: ${wifi.signalDbm} dBm (${wifi.signalPercent}%)\n")
                                sb.append("   • Frekuensi: ${wifi.frequencyMhz} MHz | ${wifi.capabilities}\n\n")
                            }
                        }

                        sb.append("🎧 <b>Perangkat Bluetooth (${btList.size} Ditemukan):</b>\n")
                        if (btList.isEmpty()) {
                            sb.append("<i>Tidak ada perangkat Bluetooth terdeteksi di sekitar.</i>\n\n")
                        } else {
                            btList.take(10).forEachIndexed { idx, bt ->
                                sb.append("${idx + 1}. <b>${escapeHtml(bt.name)}</b>\n")
                                sb.append("   • Alamat: <code>${bt.address}</code>\n\n")
                            }
                        }

                        sb.append("🕒 <i>Waktu: $timeStr</i>")
                        executeSendMessage(token, chatId, sb.toString())
                    } catch (e: Exception) {
                        Log.e("TelegramBotManager", "Error /scan Telegram: ${e.message}", e)
                        executeSendMessage(
                            token,
                            chatId,
                            "⚠️ <b>Gagal Pemindaian:</b> ${e.localizedMessage}"
                        )
                    }
                }
            }
            "/start", "/help" -> {
                val replyMsg = "👋 <b>Selamat Datang di Bot Famly!</b>\n\n" +
                        "Perintah yang dapat Anda gunakan:\n" +
                        "• <code>/lokasi</code> - Meminta koordinat GPS & peta lokasi terkini anak\n" +
                        "• <code>/scan</code> - Meminta pemindaian jaringan Wi-Fi & Bluetooth di sekitar\n" +
                        "• <code>/ping</code> - Cek status koneksi bot & GPS HP anak\n" +
                        "• <code>/help</code> - Menampilkan bantuan ini"
                executeSendMessage(token, chatId, replyMsg)
            }
        }
    }

    private val recentSentNotifications = java.util.concurrent.ConcurrentHashMap<String, Long>()

    suspend fun sendNotification(
        appName: String,
        title: String,
        text: String,
        subText: String = "",
        postTime: Long = System.currentTimeMillis(),
        isSensitive: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isEnabled()) return@withContext false

        val token = getBotToken()
        val chatId = getChatId()
        if (token.isBlank() || chatId.isBlank()) return@withContext false

        if (isSensitive && isExcludeSensitive()) {
            Log.d("TelegramBotManager", "Notifikasi sensitif diabaikan sesuai preferensi pengguna.")
            return@withContext false
        }

        // Deduplication filter: prevent identical messages within 12 seconds
        val dedupeKey = "${appName.trim()}|${title.trim()}|${text.trim()}"
        val now = System.currentTimeMillis()
        val lastSent = recentSentNotifications[dedupeKey]
        if (lastSent != null && (now - lastSent) < 12_000L) {
            Log.d("TelegramBotManager", "Duplikasi pesan Telegram dicegah untuk: $dedupeKey")
            return@withContext true
        }
        recentSentNotifications[dedupeKey] = now

        // Clean up old cache entries periodically
        if (recentSentNotifications.size > 200) {
            val cutoff = now - 60_000L
            recentSentNotifications.entries.removeIf { it.value < cutoff }
        }

        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMM yyyy", Locale.getDefault()).format(Date(postTime))

        val message = StringBuilder().apply {
            append("🔔 <b>[Famly] Notifikasi Baru</b>\n\n")
            append("📱 <b>Aplikasi:</b> ${escapeHtml(appName)}\n")
            if (title.isNotBlank()) {
                append("📌 <b>Judul:</b> ${escapeHtml(title)}\n")
            }
            if (text.isNotBlank()) {
                append("💬 <b>Pesan:</b> ${escapeHtml(text)}\n")
            }
            if (subText.isNotBlank()) {
                append("ℹ️ <b>Info:</b> ${escapeHtml(subText)}\n")
            }
            append("\n🕒 <i>$timeStr</i>")
        }.toString()

        return@withContext executeSendMessage(token, chatId, message)
    }

    suspend fun sendLocation(latitude: Double, longitude: Double, targetChatId: String? = null): Boolean = withContext(Dispatchers.IO) {
        if (!isEnabled()) return@withContext false

        val token = getBotToken()
        val chatId = if (!targetChatId.isNullOrBlank()) targetChatId else getChatId()
        if (token.isBlank() || chatId.isBlank()) return@withContext false

        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMM yyyy", Locale.getDefault()).format(Date())
        sendLocationPin(token, chatId, latitude, longitude)
        return@withContext sendLocationDetailsText(token, chatId, latitude, longitude, timeStr)
    }

    private fun sendLocationPin(token: String, chatId: String, latitude: Double, longitude: Double): Boolean {
        return try {
            val urlString = "https://api.telegram.org/bot$token/sendLocation"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            val jsonBody = JSONObject().apply {
                put("chat_id", chatId)
                put("latitude", latitude)
                put("longitude", longitude)
            }

            conn.outputStream.use { os ->
                val input = jsonBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Log.e("TelegramBotManager", "Gagal mengirim location pin ke Telegram: ${e.message}", e)
            false
        }
    }

    private fun sendLocationDetailsText(
        token: String,
        chatId: String,
        latitude: Double,
        longitude: Double,
        timeStr: String
    ): Boolean {
        val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"
        val textMsg = StringBuilder().apply {
            append("📍 <b>[Famly] Lokasi Terkini Anak</b>\n\n")
            append("🌐 <b>Koordinat GPS:</b> <code>$latitude, $longitude</code>\n")
            append("🗺️ <b>Peta Google Maps:</b> <a href=\"$mapsUrl\">Buka di Maps</a>\n\n")
            append("🕒 <i>$timeStr</i>")
        }.toString()

        return executeSendMessage(token, chatId, textMsg)
    }

    suspend fun testConnection(token: String, chatId: String): String = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        val cleanChatId = chatId.trim()

        if (cleanToken.isBlank()) return@withContext "Bot Token tidak boleh kosong."
        if (cleanChatId.isBlank()) return@withContext "Chat ID tidak boleh kosong."

        val testMsg = "🎉 <b>[Famly] Uji Coba Bot Telegram Sukses!</b>\n\nBot Telegram Famly telah terhubung dan siap merespons perintah /lokasi & /ping."

        val success = executeSendMessage(cleanToken, cleanChatId, testMsg)
        if (success) {
            "SUCCESS: Pesan tes berhasil dikirim ke Telegram!"
        } else {
            "ERROR: Gagal mengirim pesan. Pastikan Token dan Chat ID benar, serta sudah menekan /start pada bot."
        }
    }

    private fun executeSendMessage(token: String, chatId: String, textHtml: String): Boolean {
        return try {
            val urlString = "https://api.telegram.org/bot$token/sendMessage"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            val jsonBody = JSONObject().apply {
                put("chat_id", chatId)
                put("text", textHtml)
                put("parse_mode", "HTML")
                put("disable_web_page_preview", true)
            }

            conn.outputStream.use { os ->
                val input = jsonBody.toString().toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("TelegramBotManager", "Pesan Telegram berhasil dikirim!")
                true
            } else {
                val errorStream = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("TelegramBotManager", "Error Telegram ($responseCode): $errorStream")
                false
            }
        } catch (e: Exception) {
            Log.e("TelegramBotManager", "Gagal mengirim ke Telegram: ${e.message}", e)
            false
        }
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}
