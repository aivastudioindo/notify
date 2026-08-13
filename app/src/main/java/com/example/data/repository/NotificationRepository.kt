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
import kotlinx.coroutines.flow.flowOn
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
        val cleanTitle = title.trim()
        val cleanText = text.trim()
        val cleanSubText = subText.trim()
        val cleanBigText = bigText.trim()

        // 1. Check if entity with same notificationKey exists
        val existingByKey = if (key.isNotBlank()) notificationDao.getByKey(key) else null
        if (existingByKey != null) {
            // Check if title, text, subText, bigText are identical
            if (existingByKey.encryptedTitle == cleanTitle &&
                existingByKey.encryptedText == cleanText &&
                existingByKey.encryptedSubText == cleanSubText &&
                existingByKey.encryptedBigText == cleanBigText
            ) {
                // Completely duplicate notification - ignore duplicate
                return@withContext existingByKey.id
            }
            // Title or text updated (e.g. WhatsApp message thread updated) -> Update existing entity!
            val updated = existingByKey.copy(
                encryptedTitle = cleanTitle,
                encryptedText = cleanText,
                encryptedSubText = cleanSubText,
                encryptedBigText = cleanBigText,
                postTime = postTime,
                isRead = false
            )
            notificationDao.updateNotification(updated)
            return@withContext existingByKey.id
        }

        // 2. Fallback duplicate check: Same package, title, and text posted within the last 15 seconds
        val recentCutoff = postTime - 15_000L
        val duplicateRecent = notificationDao.findRecentDuplicate(
            packageName = packageName,
            title = cleanTitle,
            text = cleanText,
            minTime = recentCutoff
        )
        if (duplicateRecent != null) {
            // Rapid duplicate notification - ignore
            return@withContext duplicateRecent.id
        }

        val fullContent = "$cleanTitle $cleanText $cleanSubText $cleanBigText"
        val category = NotificationCategory.categorize(packageName, cleanTitle, cleanText)
        val isSensitive = isSensitiveContent(fullContent)

        val entity = NotificationEntity(
            notificationKey = key,
            packageName = packageName,
            appName = appName,
            encryptedTitle = cleanTitle,
            encryptedText = cleanText,
            encryptedSubText = cleanSubText,
            encryptedBigText = cleanBigText,
            iv = "",
            postTime = postTime,
            category = category.id,
            isSensitive = isSensitive,
            isFavorite = false,
            isRead = false
        )

        notificationDao.insertNotification(entity)
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
