package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationCategory(
    val id: String,
    val displayName: String,
    val description: String,
    val color: Color
) {
    CHAT(
        id = "CHAT",
        displayName = "Pesan & Chat",
        description = "WhatsApp, Telegram, SMS, Discord, Line",
        color = Color(0xFF10B981) // Emerald Green
    ),
    SOCIAL(
        id = "SOCIAL",
        displayName = "Media Sosial",
        description = "Instagram, X/Twitter, TikTok, Facebook",
        color = Color(0xFF06B6D4) // Cyan
    ),
    FINANCE(
        id = "FINANCE",
        displayName = "Keuangan & Bank",
        description = "BCA, Mandiri, Dana, GoPay, OVO, Bank",
        color = Color(0xFFF59E0B) // Amber
    ),
    SHOPPING(
        id = "SHOPPING",
        displayName = "Belanja & E-Commerce",
        description = "Shopee, Tokopedia, Lazada, Blibli",
        color = Color(0xFFF97316) // Orange
    ),
    WORK_EMAIL(
        id = "WORK_EMAIL",
        displayName = "Email & Kerja",
        description = "Gmail, Outlook, Slack, Teams, Notion",
        color = Color(0xFF6366F1) // Indigo
    ),
    ENTERTAINMENT(
        id = "ENTERTAINMENT",
        displayName = "Hiburan & Media",
        description = "Spotify, YouTube, Netflix, Game",
        color = Color(0xFFEC4899) // Pink
    ),
    SYSTEM(
        id = "SYSTEM",
        displayName = "Sistem & Alat",
        description = "Android System, Baterai, Unduhan",
        color = Color(0xFF64748B) // Slate
    ),
    OTHER(
        id = "OTHER",
        displayName = "Lainnya",
        description = "Notifikasi aplikasi lainnya",
        color = Color(0xFF8B5CF6) // Purple
    );

    val icon: ImageVector
        get() = when (this) {
            CHAT -> Icons.Default.Chat
            SOCIAL -> Icons.Default.Public
            FINANCE -> Icons.Default.AccountBalance
            SHOPPING -> Icons.Default.ShoppingCart
            WORK_EMAIL -> Icons.Default.Mail
            ENTERTAINMENT -> Icons.Default.MusicNote
            SYSTEM -> Icons.Default.Settings
            OTHER -> Icons.Default.Apps
        }

    companion object {
        fun fromId(id: String?): NotificationCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: OTHER
        }

        fun categorize(packageName: String, title: String = "", text: String = ""): NotificationCategory {
            val pkg = packageName.lowercase()
            val content = (title + " " + text).lowercase()

            return when {
                // CHAT
                pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") ||
                        pkg.contains("discord") || pkg.contains("line") || pkg.contains("mms") ||
                        pkg.contains("messaging") || pkg.contains("message") || pkg.contains("viber") ||
                        pkg.contains("wechat") || pkg.contains("orca") || pkg.contains("skype") -> CHAT

                // SOCIAL
                pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("tiktok") ||
                        pkg.contains("facebook") || pkg.contains("reddit") || pkg.contains("threads") ||
                        pkg.contains("snapchat") || pkg.contains("linkedin") || pkg.contains("pinterest") -> SOCIAL

                // FINANCE & BANKING
                pkg.contains("bank") || pkg.contains("bca") || pkg.contains("mandiri") ||
                        pkg.contains("bri") || pkg.contains("bni") || pkg.contains("cimb") ||
                        pkg.contains("danamon") || pkg.contains("permata") || pkg.contains("bsi") ||
                        pkg.contains("dana") || pkg.contains("gopay") || pkg.contains("ovo") ||
                        pkg.contains("shopeepay") || pkg.contains("linkaja") || pkg.contains("jenius") ||
                        pkg.contains("jago") || pkg.contains("seabank") || pkg.contains("neobank") ||
                        pkg.contains("paypal") || pkg.contains("finance") || pkg.contains("crypto") ||
                        pkg.contains("binance") || pkg.contains("bibit") || pkg.contains("ajaib") ||
                        pkg.contains("stockbit") || pkg.contains("pluang") || pkg.contains("flip") -> FINANCE

                // SHOPPING
                pkg.contains("shopee") || pkg.contains("tokopedia") || pkg.contains("lazada") ||
                        pkg.contains("blibli") || pkg.contains("bukalapak") || pkg.contains("amazon") ||
                        pkg.contains("aliexpress") || pkg.contains("zalora") || pkg.contains("tiktokshop") ||
                        content.contains("diskon") || content.contains("promo") || content.contains("cashback") ||
                        content.contains("pesanan") || content.contains("ongkir") -> SHOPPING

                // WORK & EMAIL
                pkg.contains("gmail") || pkg.contains("mail") || pkg.contains("outlook") ||
                        pkg.contains("slack") || pkg.contains("teams") || pkg.contains("zoom") ||
                        pkg.contains("notion") || pkg.contains("trello") || pkg.contains("asana") ||
                        pkg.contains("jira") || pkg.contains("docs") || pkg.contains("drive") ||
                        pkg.contains("github") -> WORK_EMAIL

                // ENTERTAINMENT
                pkg.contains("spotify") || pkg.contains("youtube") || pkg.contains("netflix") ||
                        pkg.contains("music") || pkg.contains("primevideo") || pkg.contains("disney") ||
                        pkg.contains("game") || pkg.contains("twitch") || pkg.contains("soundcloud") -> ENTERTAINMENT

                // SYSTEM
                pkg.contains("android") || pkg.contains("systemui") || pkg.contains("download") ||
                        pkg.contains("settings") || pkg.contains("google.android.gms") ||
                        pkg.contains("security") || pkg.contains("battery") -> SYSTEM

                else -> OTHER
            }
        }
    }
}
