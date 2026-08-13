package com.example.data.telegram

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

class TelegramBotManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notif_vault_telegram_prefs", Context.MODE_PRIVATE)

    private var pollingJob: kotlinx.coroutines.Job? = null
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
                    it.startPolling()
                }
            }
        }
    }

    fun isEnabled(): Boolean = prefs.getBoolean(PREF_IS_ENABLED, false)

    fun getBotToken(): String = prefs.getString(PREF_BOT_TOKEN, "") ?: ""

    fun getChatId(): String = prefs.getString(PREF_CHAT_ID, "") ?: ""

    fun isExcludeSensitive(): Boolean = prefs.getBoolean(PREF_EXCLUDE_SENSITIVE, true)

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

        if (enabled && botToken.isNotBlank()) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        if (!isEnabled() || getBotToken().isBlank()) return

        pollingJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            Log.d("TelegramBotManager", "Mulai listener perintah Telegram Bot (/lokasi)...")
            while (isActive && isEnabled()) {
                try {
                    pollUpdates()
                } catch (e: Exception) {
                    Log.e("TelegramBotManager", "Error polling Telegram: ${e.message}")
                }
                kotlinx.coroutines.delay(2500L)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollUpdates() {
        val token = getBotToken()
        val configuredChatId = getChatId()
        if (token.isBlank()) return

        try {
            val urlString = "https://api.telegram.org/bot$token/getUpdates?offset=${lastUpdateId + 1}&timeout=3"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
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
                        val fromChat = message.optJSONObject("chat")
                        val chatIdFromMessage = fromChat?.optLong("id")?.toString() ?: ""

                        // Process command if chat matches configured chat ID (or if configuredChatId is blank)
                        val targetChatId = if (configuredChatId.isNotBlank()) configuredChatId else chatIdFromMessage
                        if (chatIdFromMessage == targetChatId || configuredChatId.isBlank()) {
                            handleCommand(text, targetChatId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TelegramBotManager", "Gagal memproses update Telegram: ${e.message}")
        }
    }

    private fun handleCommand(text: String, chatId: String) {
        val token = getBotToken()
        if (token.isBlank() || chatId.isBlank()) return

        val lowerText = text.lowercase()
        when {
            lowerText.startsWith("/lokasi") || lowerText.startsWith("/location") || lowerText.startsWith("/where") -> {
                executeSendMessage(token, chatId, "⏳ <i>Mengambil koordinat GPS HP anak... Mohon tunggu sebentar.</i>")
                val locationHelper = com.example.data.location.LocationHelper(context)
                locationHelper.getCurrentLocation(
                    onSuccess = { loc ->
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            sendLocation(loc.latitude, loc.longitude)
                        }
                    },
                    onError = { err ->
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            executeSendMessage(
                                token,
                                chatId,
                                "⚠️ <b>Gagal Mengambil Lokasi:</b> $err\n\n<i>Pastikan izin GPS pada HP anak telah diizinkan di aplikasi Famly.</i>"
                            )
                        }
                    }
                )
            }
            lowerText == "/start" || lowerText == "/help" -> {
                val replyMsg = "👋 <b>Selamat Datang di Bot Famly!</b>\n\n" +
                        "Perintah yang dapat Anda gunakan:\n" +
                        "• <code>/lokasi</code> - Meminta koordinat GPS & peta lokasi terkini anak\n" +
                        "• <code>/ping</code> - Cek status koneksi bot HP anak"
                executeSendMessage(token, chatId, replyMsg)
            }
            lowerText == "/ping" -> {
                executeSendMessage(token, chatId, "✅ <b>Bot Famly Online!</b> HP anak terhubung dan siap merespons perintah.")
            }
        }
    }

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

    suspend fun sendLocation(latitude: Double, longitude: Double): Boolean = withContext(Dispatchers.IO) {
        if (!isEnabled()) return@withContext false

        val token = getBotToken()
        val chatId = getChatId()
        if (token.isBlank() || chatId.isBlank()) return@withContext false

        val timeStr = SimpleDateFormat("HH:mm:ss - dd MMM yyyy", Locale.getDefault()).format(Date())
        val mapsUrl = "https://maps.google.com/?q=$latitude,$longitude"

        val textMsg = StringBuilder().apply {
            append("📍 <b>[Famly] Lokasi Terkini Anak</b>\n\n")
            append("🌐 <b>Koordinat GPS:</b> <code>$latitude, $longitude</code>\n")
            append("🗺️ <b>Peta Google Maps:</b> <a href=\"$mapsUrl\">Buka di Maps</a>\n\n")
            append("🕒 <i>$timeStr</i>")
        }.toString()

        // First send location pin
        executeSendLocationPin(token, chatId, latitude, longitude)
        // Second send text with link
        return@withContext executeSendMessage(token, chatId, textMsg)
    }

    suspend fun testConnection(token: String, chatId: String): String = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        val cleanChatId = chatId.trim()

        if (cleanToken.isBlank()) return@withContext "Bot Token tidak boleh kosong."
        if (cleanChatId.isBlank()) return@withContext "Chat ID tidak boleh kosong."

        val testMsg = "🎉 <b>[Famly] Uji Coba Bot Telegram Sukses!</b>\n\nBot Telegram Famly telah terhubung dan siap meneruskan notifikasi & koordinat lokasi anak."

        val success = executeSendMessage(cleanToken, cleanChatId, testMsg)
        if (success) {
            "SUCCESS: Pesan tes berhasil dikirim ke Telegram!"
        } else {
            "ERROR: Gagal mengirim pesan. Pastikan Token dan Chat ID benar, serta sudah menekan /start pada bot."
        }
    }

    private fun executeSendLocationPin(token: String, chatId: String, latitude: Double, longitude: Double): Boolean {
        return try {
            val urlString = "https://api.telegram.org/bot$token/sendLocation"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
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
            Log.e("TelegramBotManager", "Gagal mengirim location pin Telegram: ${e.message}", e)
            false
        }
    }

    private fun executeSendMessage(token: String, chatId: String, textHtml: String): Boolean {
        return try {
            val urlString = "https://api.telegram.org/bot$token/sendMessage"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
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
