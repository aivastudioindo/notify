package com.example.data.repository

import android.content.Context
import com.example.data.local.AppCountResult
import com.example.data.local.CategoryCountResult
import com.example.data.local.NotificationDao
import com.example.data.model.AnalyticsSummary
import com.example.data.model.CategoryStat
import com.example.data.model.DailyStat
import com.example.data.model.HourlyStat
import com.example.data.model.NotificationCategory
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationItem
import com.example.data.model.TopAppStat
import com.example.data.security.EncryptionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val encryptionManager: EncryptionManager
) {

    private val otpRegex = Regex("""\b\d{4,6}\b|otp|pin|kode|verifikasi|password|sandi|transfer|saldo""", RegexOption.IGNORE_CASE)

    fun isSensitiveContent(text: String): Boolean {
        return otpRegex.containsMatchIn(text)
    }

    suspend fun saveNotification(
        key: String,
        packageName: String,
        appName: String,
        title: String,
        text: String,
        subText: String = "",
        bigText: String = "",
        postTime: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val fullContent = "$title $text $subText $bigText"
        val category = NotificationCategory.categorize(packageName, title, text)
        val isSensitive = isSensitiveContent(fullContent)

        // Check if encryption is active
        val (encTitle, iv) = encryptionManager.encrypt(title)
        val (encText, _) = encryptionManager.encrypt(text)
        val (encSubText, _) = encryptionManager.encrypt(subText)
        val (encBigText, _) = encryptionManager.encrypt(bigText)

        val entity = NotificationEntity(
            notificationKey = key,
            packageName = packageName,
            appName = appName,
            encryptedTitle = encTitle,
            encryptedText = encText,
            encryptedSubText = encSubText,
            encryptedBigText = encBigText,
            iv = iv,
            postTime = postTime,
            category = category.id,
            isSensitive = isSensitive,
            isFavorite = false,
            isRead = false
        )

        notificationDao.insertNotification(entity)
    }

    private fun mapToItem(entity: NotificationEntity): NotificationItem {
        val title = encryptionManager.decrypt(entity.encryptedTitle, entity.iv)
        val text = encryptionManager.decrypt(entity.encryptedText, entity.iv)
        val subText = encryptionManager.decrypt(entity.encryptedSubText, entity.iv)
        val bigText = encryptionManager.decrypt(entity.encryptedBigText, entity.iv)

        return NotificationItem(
            id = entity.id,
            notificationKey = entity.notificationKey,
            packageName = entity.packageName,
            appName = entity.appName.ifBlank { entity.packageName.substringAfterLast('.') },
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            postTime = entity.postTime,
            category = NotificationCategory.fromId(entity.category),
            isSensitive = entity.isSensitive,
            isFavorite = entity.isFavorite,
            isRead = entity.isRead,
            isEncrypted = entity.iv.isNotBlank()
        )
    }

    fun getAllNotifications(): Flow<List<NotificationItem>> {
        return notificationDao.getAllNotifications().map { list ->
            list.map { mapToItem(it) }
        }
    }

    fun getFilteredNotifications(
        searchQuery: String = "",
        category: NotificationCategory? = null,
        packageName: String? = null,
        isFavorite: Boolean? = null,
        startTime: Long = 0L,
        endTime: Long = Long.MAX_VALUE
    ): Flow<List<NotificationItem>> {
        return notificationDao.getFilteredNotifications(
            category = category?.id,
            packageName = packageName,
            isFavorite = isFavorite,
            startTime = startTime,
            endTime = endTime
        ).map { entities ->
            val items = entities.map { mapToItem(it) }
            if (searchQuery.isBlank()) {
                items
            } else {
                val query = searchQuery.trim().lowercase()
                items.filter { item ->
                    item.title.lowercase().contains(query) ||
                            item.text.lowercase().contains(query) ||
                            item.appName.lowercase().contains(query) ||
                            item.subText.lowercase().contains(query) ||
                            item.bigText.lowercase().contains(query) ||
                            item.packageName.lowercase().contains(query)
                }
            }
        }
    }

    val distinctAppsWithCount: Flow<List<AppCountResult>> =
        notificationDao.getDistinctAppsWithCount()

    val categoryCounts: Flow<List<CategoryCountResult>> =
        notificationDao.getCategoryCounts()

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        notificationDao.setFavorite(id, !currentFavorite)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteById(id)
    }

    suspend fun deleteByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        notificationDao.deleteByIds(ids)
    }

    suspend fun deleteByPackage(packageName: String) = withContext(Dispatchers.IO) {
        notificationDao.deleteByPackage(packageName)
    }

    suspend fun deleteByCategory(category: NotificationCategory) = withContext(Dispatchers.IO) {
        notificationDao.deleteByCategory(category.id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        notificationDao.clearAll()
    }

    fun getAnalyticsSummary(): Flow<AnalyticsSummary> {
        return notificationDao.getAllNotifications().map { entities ->
            val items = entities.map { mapToItem(it) }
            computeAnalytics(items)
        }
    }

    private fun computeAnalytics(items: List<NotificationItem>): AnalyticsSummary {
        if (items.isEmpty()) {
            return AnalyticsSummary()
        }

        val total = items.size
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        // Today start
        calendar.timeInMillis = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        // Yesterday start
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = calendar.timeInMillis
        val yesterdayEnd = todayStart - 1

        val todayItems = items.filter { it.postTime >= todayStart }
        val yesterdayItems = items.filter { it.postTime in yesterdayStart..yesterdayEnd }
        val sensitiveCount = items.count { it.isSensitive }
        val favoriteCount = items.count { it.isFavorite }

        // Hourly stats for today (0..23)
        val hourlyBuckets = IntArray(24) { 0 }
        todayItems.forEach { item ->
            calendar.timeInMillis = item.postTime
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            if (hour in 0..23) {
                hourlyBuckets[hour]++
            }
        }

        val hourlyStats = (0..23).map { h ->
            HourlyStat(
                hour = h,
                count = hourlyBuckets[h],
                formattedHour = String.format(Locale.getDefault(), "%02d:00", h)
            )
        }

        // Find peak hour
        var maxHour = 0
        var maxHourCount = 0
        hourlyBuckets.forEachIndexed { h, count ->
            if (count > maxHourCount) {
                maxHourCount = count
                maxHour = h
            }
        }
        val peakHourStr = if (maxHourCount > 0) String.format(Locale.getDefault(), "%02d:00 - %02d:59", maxHour, maxHour) else "-"

        // Daily stats for last 7 days
        val dayFormat = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        val dayNameFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        val dailyStatsList = mutableListOf<DailyStat>()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = now
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val dStart = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val dEnd = calendar.timeInMillis

            val countForDay = items.count { it.postTime in dStart..dEnd }
            dailyStatsList.add(
                DailyStat(
                    dateKey = dayFormat.format(Date(dStart)),
                    timestamp = dStart,
                    count = countForDay,
                    dayName = dayNameFormat.format(Date(dStart))
                )
            )
        }

        // Category breakdown
        val categoryGroup = items.groupBy { it.category }
        val categoryStats = categoryGroup.map { (cat, list) ->
            CategoryStat(
                category = cat,
                count = list.size,
                percentage = (list.size.toFloat() / total) * 100f
            )
        }.sortedByDescending { it.count }

        val topCategory = categoryStats.firstOrNull()?.category ?: NotificationCategory.OTHER

        // Top Apps
        val appGroup = items.groupBy { it.packageName }
        val topApps = appGroup.map { (pkg, list) ->
            val firstItem = list.first()
            TopAppStat(
                packageName = pkg,
                appName = firstItem.appName,
                count = list.size,
                percentage = (list.size.toFloat() / total) * 100f,
                category = firstItem.category
            )
        }.sortedByDescending { it.count }.take(8)

        val mostActiveApp = topApps.firstOrNull()?.appName ?: "-"

        return AnalyticsSummary(
            totalRecorded = total,
            todayCount = todayItems.size,
            yesterdayCount = yesterdayItems.size,
            sensitiveCount = sensitiveCount,
            favoriteCount = favoriteCount,
            peakHour = peakHourStr,
            peakHourCount = maxHourCount,
            mostActiveApp = mostActiveApp,
            topCategory = topCategory,
            hourlyStats = hourlyStats,
            dailyStats = dailyStatsList,
            categoryStats = categoryStats,
            topApps = topApps
        )
    }

    suspend fun insertSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L
        val dayMs = 86400_000L

        val samples = listOf(
            SampleMock("com.whatsapp", "WhatsApp", "Budi Santoso", "Bro, besok jadi meeting proyek jam 10 pagi di kantor?", now - 5 * 60_000L, NotificationCategory.CHAT),
            SampleMock("com.bca", "BCA Mobile", "Transfer Masuk", "Rekening 8271xxxx mendapat transfer Rp 1.500.000 dari SITI NURHALIZA. Saldo Anda saat ini Rp 4.250.000.", now - 25 * 60_000L, NotificationCategory.FINANCE),
            SampleMock("com.shopee.id", "Shopee", "Flash Sale 80% Dimulai!", "Serbu gratis ongkir Rp0 dan diskon kilat gadget pilihan Anda sekarang juga!", now - 45 * 60_000L, NotificationCategory.SHOPPING),
            SampleMock("com.instagram.android", "Instagram", "dina_lestari menyukai postingan Anda", "Foto Anda mendapat 42 suka baru hari ini.", now - 1 * hourMs, NotificationCategory.SOCIAL),
            SampleMock("com.google.android.gm", "Gmail", "GitHub - Security Alert", "[Critical] New personal access token generated on your account notif-vault-dev.", now - 2 * hourMs, NotificationCategory.WORK_EMAIL),
            SampleMock("com.telkomsel.mytelkomsel", "MyTelkomsel", "Kode Verifikasi (OTP)", "JANGAN BERIKAN KODE INI KE SIAPAPUN! Kode OTP login MyTelkomsel Anda adalah 749201 berlaku 5 menit.", now - 3 * hourMs, NotificationCategory.FINANCE),
            SampleMock("org.telegram.messenger", "Telegram", "Komunitas Developer Android", "Rian: Ada rilis compose BOM terbaru yang lebih hemat memori nih kawan-kawan.", now - 4 * hourMs, NotificationCategory.CHAT),
            SampleMock("com.tokopedia.tkpd", "Tokopedia", "Pesanan Sedang Dikirim", "Paket nomor resi TKP09827112 telah diserahkan kurir ke alamat tujuan.", now - 6 * hourMs, NotificationCategory.SHOPPING),
            SampleMock("com.spotify.music", "Spotify", "Rilisan Musik Baru", "Artis favoritmu Tulus baru saja merilis single terbaru hari ini. Dengarkan sekarang!", now - 8 * hourMs, NotificationCategory.ENTERTAINMENT),
            SampleMock("com.twitter.android", "X (Twitter)", "Trending Hari Ini", "#TechNews dan #Android15 menjadi topik terhangat di Indonesia dengan 120k postingan.", now - 10 * hourMs, NotificationCategory.SOCIAL),
            SampleMock("com.slack", "Slack", "Product Team", "Sarah: Task sprint review telah diselesaikan dan diupdate di Jira board.", now - 1 * dayMs - 2 * hourMs, NotificationCategory.WORK_EMAIL),
            SampleMock("id.dana", "DANA", "Pembayaran Berhasil", "Pembayaran QRIS sebesar Rp 25.000 di Kopi Kenangan berhasil. Sisa saldo DANA Rp 145.000.", now - 1 * dayMs - 5 * hourMs, NotificationCategory.FINANCE),
            SampleMock("com.whatsapp", "WhatsApp", "Grup Keluarga Besar", "Ibu: Jangan lupa hari Minggu kumpul arisan ya anak-anak.", now - 2 * dayMs - 3 * hourMs, NotificationCategory.CHAT),
            SampleMock("com.google.android.apps.messaging", "Pesan SMS", "BANK MANDIRI", "Info Transaksi Debit: Penarikan ATM Rp 500.000 di ATM Mandiri Sudirman. Saldo akhir Rp 3.120.000.", now - 3 * dayMs - 4 * hourMs, NotificationCategory.FINANCE),
            SampleMock("com.zhiliaoapp.musically", "TikTok", "Video Viral Untuk Anda", "Lihat video tutorial resep masakan simpel yang ditonton 2.5 juta kali!", now - 4 * dayMs - 6 * hourMs, NotificationCategory.SOCIAL)
        )

        for (item in samples) {
            saveNotification(
                key = "${item.pkg}_${item.time}",
                packageName = item.pkg,
                appName = item.appName,
                title = item.title,
                text = item.text,
                postTime = item.time
            )
        }
    }

    private data class SampleMock(
        val pkg: String,
        val appName: String,
        val title: String,
        val text: String,
        val time: Long,
        val category: NotificationCategory
    )
}
