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
import com.example.data.telegram.TelegramBotManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationRepository(
    private val notificationDao: NotificationDao,
    private val encryptionManager: EncryptionManager,
    private val telegramBotManager: TelegramBotManager? = null
) {

    private val bankingKeywords = listOf(
        "bca", "mandiri", "bri", "brimo", "bni", "btn", "cimb", "octo", "danamon",
        "permata", "bsi", "jenius", "seabank", "jago", "allo", "blu", "neobank",
        "ocbc", "maybank", "muamalat", "panin", "sinarmas",
        "dana", "gopay", "ovo", "shopeepay", "linkaja", "flip", "doku", "astrapay", "i.saku",
        "paypal", "bibit", "ajaib", "pluang", "stockbit", "indodax", "tokocrypto",
        "transfer", "saldo", "rekening", "debit", "kredit", "transaksi", "atm", "va", "virtual account",
        "mutasi", "top up", "pembayaran", "tagihan", "m-banking", "klikbca", "livin"
    )

    private val otpKeywords = listOf(
        "otp", "pin", "kode", "verifikasi", "password", "sandi", "one-time", "secret code",
        "kode rahasia", "kode otentikasi", "kode konfirmasi", "security code", "passcode",
        "verification code", "auth code"
    )

    private val otpDigitRegex = Regex("""\b\d{4,8}\b""")

    fun isBankingNotification(packageName: String, appName: String, text: String): Boolean {
        val combined = "$packageName $appName $text".lowercase()
        return bankingKeywords.any { combined.contains(it) }
    }

    fun isOtpOrSensitive(text: String): Boolean {
        val lower = text.lowercase()
        val hasOtpWord = otpKeywords.any { lower.contains(it) }
        val hasDigits = otpDigitRegex.containsMatchIn(text)
        return hasOtpWord || (hasDigits && (lower.contains("kode") || lower.contains("code") || lower.contains("masukkan") || lower.contains("enter") || lower.contains("rahasia")))
    }

    fun isSensitiveContent(packageName: String, appName: String, text: String): Boolean {
        return isBankingNotification(packageName, appName, text) || isOtpOrSensitive(text)
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
        val cleanTitle = title.trim()
        val cleanText = text.trim()
        val cleanSubText = subText.trim()
        val cleanBigText = bigText.trim()

        // 1. Debounce rapid OS duplicate flutter (within 2 seconds only)
        // If Android OS fires multiple events for the exact same physical notification within 2000ms, ignore the flutter.
        val rapidFlutterCutoff = postTime - 2_000L
        val recentDuplicate = notificationDao.findRecentDuplicate(
            packageName = packageName,
            title = cleanTitle,
            text = cleanText,
            minTime = rapidFlutterCutoff
        )
        if (recentDuplicate != null &&
            recentDuplicate.encryptedSubText == cleanSubText &&
            recentDuplicate.encryptedBigText == cleanBigText
        ) {
            // Rapid identical repost within 2 seconds - ignore OS flutter
            return@withContext recentDuplicate.id
        }

        val fullContent = "$cleanTitle $cleanText $cleanSubText $cleanBigText"
        var category = NotificationCategory.categorize(packageName, cleanTitle, cleanText)
        val isBanking = isBankingNotification(packageName, appName, fullContent)
        val isSensitive = isSensitiveContent(packageName, appName, fullContent)

        if (isBanking && category == NotificationCategory.OTHER) {
            category = NotificationCategory.FINANCE
        }

        val uniqueEventKey = if (key.isNotBlank()) {
            "${key}_${postTime}_${(cleanTitle + cleanText).hashCode()}"
        } else {
            "${packageName}_${postTime}_${(cleanTitle + cleanText).hashCode()}"
        }

        // Auto-encrypt banking and sensitive OTP notifications with AES-256 GCM hardware encryption
        val (finalTitle, finalIv) = if (isSensitive) {
            val (encrypted, iv) = encryptionManager.encrypt(cleanTitle)
            Pair(encrypted, iv)
        } else {
            Pair(cleanTitle, "")
        }

        val finalText = if (isSensitive && finalIv.isNotEmpty()) {
            encryptionManager.encryptWithIv(cleanText, finalIv)
        } else {
            cleanText
        }

        val finalSubText = if (isSensitive && finalIv.isNotEmpty() && cleanSubText.isNotEmpty()) {
            encryptionManager.encryptWithIv(cleanSubText, finalIv)
        } else {
            cleanSubText
        }

        val finalBigText = if (isSensitive && finalIv.isNotEmpty() && cleanBigText.isNotEmpty()) {
            encryptionManager.encryptWithIv(cleanBigText, finalIv)
        } else {
            cleanBigText
        }

        val entity = NotificationEntity(
            notificationKey = uniqueEventKey,
            packageName = packageName,
            appName = appName,
            encryptedTitle = finalTitle,
            encryptedText = finalText,
            encryptedSubText = finalSubText,
            encryptedBigText = finalBigText,
            iv = finalIv,
            postTime = postTime,
            category = category.id,
            isSensitive = isSensitive,
            isFavorite = false,
            isRead = false
        )

        val insertedId = notificationDao.insertNotification(entity)

        // Meneruskan notifikasi ke Telegram Bot jika diaktifkan
        try {
            telegramBotManager?.sendNotification(
                appName = appName,
                title = cleanTitle,
                text = cleanText,
                subText = cleanSubText,
                postTime = postTime,
                isSensitive = isSensitive
            )
        } catch (e: Exception) {
            // Abaikan kesalahan kirim ke Telegram agar proses lokal tetap lancar
        }

        return@withContext insertedId
    }

    private fun mapToItem(entity: NotificationEntity): NotificationItem {
        val title = if (entity.iv.isEmpty()) entity.encryptedTitle else encryptionManager.decrypt(entity.encryptedTitle, entity.iv)
        val text = if (entity.iv.isEmpty()) entity.encryptedText else encryptionManager.decrypt(entity.encryptedText, entity.iv)
        val subText = if (entity.iv.isEmpty()) entity.encryptedSubText else encryptionManager.decrypt(entity.encryptedSubText, entity.iv)
        val bigText = if (entity.iv.isEmpty()) entity.encryptedBigText else encryptionManager.decrypt(entity.encryptedBigText, entity.iv)

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
        }.flowOn(Dispatchers.Default)
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
            searchQuery = searchQuery.trim(),
            category = category?.id,
            packageName = packageName,
            isFavorite = isFavorite,
            startTime = startTime,
            endTime = endTime
        ).map { entities ->
            entities.map { mapToItem(it) }
        }.flowOn(Dispatchers.Default)
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
            computeAnalytics(entities)
        }.flowOn(Dispatchers.Default)
    }

    private fun computeAnalytics(entities: List<NotificationEntity>): AnalyticsSummary {
        if (entities.isEmpty()) {
            return AnalyticsSummary()
        }

        val total = entities.size
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

        val todayItems = entities.filter { it.postTime >= todayStart }
        val yesterdayItems = entities.filter { it.postTime in yesterdayStart..yesterdayEnd }
        val sensitiveCount = entities.count { it.isSensitive }
        val favoriteCount = entities.count { it.isFavorite }

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

            val countForDay = entities.count { it.postTime in dStart..dEnd }
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
        val categoryGroup = entities.groupBy { NotificationCategory.fromId(it.category) }
        val categoryStats = categoryGroup.map { (cat, list) ->
            CategoryStat(
                category = cat,
                count = list.size,
                percentage = (list.size.toFloat() / total) * 100f
            )
        }.sortedByDescending { it.count }

        val topCategory = categoryStats.firstOrNull()?.category ?: NotificationCategory.OTHER

        // Top Apps
        val appGroup = entities.groupBy { it.packageName }
        val topApps = appGroup.map { (pkg, list) ->
            val firstItem = list.first()
            val cat = NotificationCategory.fromId(firstItem.category)
            val appName = firstItem.appName.ifBlank { pkg.substringAfterLast('.') }
            TopAppStat(
                packageName = pkg,
                appName = appName,
                count = list.size,
                percentage = (list.size.toFloat() / total) * 100f,
                category = cat
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
}
