package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

data class AppCountResult(
    val packageName: String,
    val appName: String,
    val count: Int,
    val category: String
)

data class CategoryCountResult(
    val category: String,
    val count: Int
)

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY postTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications 
        WHERE (:category IS NULL OR category = :category)
        AND (:packageName IS NULL OR packageName = :packageName)
        AND (:isFavorite IS NULL OR isFavorite = :isFavorite)
        AND (postTime >= :startTime AND postTime <= :endTime)
        AND (
            :searchQuery = '' OR 
            encryptedTitle LIKE '%' || :searchQuery || '%' OR 
            encryptedText LIKE '%' || :searchQuery || '%' OR 
            appName LIKE '%' || :searchQuery || '%' OR 
            encryptedSubText LIKE '%' || :searchQuery || '%' OR
            packageName LIKE '%' || :searchQuery || '%'
        )
        ORDER BY postTime DESC
        LIMIT :limit
    """)
    fun getFilteredNotifications(
        searchQuery: String = "",
        category: String? = null,
        packageName: String? = null,
        isFavorite: Boolean? = null,
        startTime: Long = 0L,
        endTime: Long = Long.MAX_VALUE,
        limit: Int = 300
    ): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE notificationKey = :key LIMIT 1")
    suspend fun getByKey(key: String): NotificationEntity?

    @Query("""
        SELECT * FROM notifications 
        WHERE packageName = :packageName 
        AND encryptedTitle = :title 
        AND encryptedText = :text 
        AND postTime >= :minTime 
        LIMIT 1
    """)
    suspend fun findRecentDuplicate(
        packageName: String,
        title: String,
        text: String,
        minTime: Long
    ): NotificationEntity?

    @Query("SELECT DISTINCT packageName, appName, count(*) as count, category FROM notifications GROUP BY packageName ORDER BY count DESC")
    fun getDistinctAppsWithCount(): Flow<List<AppCountResult>>

    @Query("SELECT category, count(*) as count FROM notifications GROUP BY category ORDER BY count DESC")
    fun getCategoryCounts(): Flow<List<CategoryCountResult>>

    @Query("SELECT * FROM notifications WHERE postTime >= :startTime AND postTime <= :endTime ORDER BY postTime ASC")
    fun getNotificationsInRange(startTime: Long, endTime: Long): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE postTime >= :todayStart")
    fun getTodayCount(todayStart: Long): Flow<Int>

    @Query("UPDATE notifications SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notifications WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM notifications WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Query("DELETE FROM notifications WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
