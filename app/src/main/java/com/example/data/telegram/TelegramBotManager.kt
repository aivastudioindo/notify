package com.example.data.telegram

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

class TelegramBotManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("notif_vault_telegram_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_IS_ENABLED = "telegram_is_enabled"
        private const val PREF_BOT_TOKEN = "telegram_bot_token"
        private const val PREF_CHAT_ID = "telegram_chat_id"
        private const val PREF_EXCLUDE_SENSITIVE = "telegram_exclude_sensitive"

        @Volatile
        private var INSTANCE: TelegramBotManager? = null

        fun getInstance(context: Context): TelegramBotManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TelegramBotManager(context.applicationContext).also { INSTANCE = it }
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
            append("🔔 <b>[NotifVault] Notifikasi Baru</b>\n\n")
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

    suspend fun testConnection(token: String, chatId: String): String = withContext(Dispatchers.IO) {
        val cleanToken = token.trim()
        val cleanChatId = chatId.trim()

        if (cleanToken.isBlank()) return@withContext "Bot Token tidak boleh kosong."
        if (cleanChatId.isBlank()) return@withContext "Chat ID tidak boleh kosong."

        val testMsg = "🎉 <b>[NotifVault] Uji Coba Bot Telegram Success!</b>\n\nBot Telegram Anda telah terhubung dan siap meneruskan notifikasi dari NotifVault."

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
