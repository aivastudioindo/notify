package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["postTime"]),
        Index(value = ["packageName"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"]),
        Index(value = ["notificationKey"])
    ]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val notificationKey: String = "",
    val packageName: String = "",
    val appName: String = "",
    val encryptedTitle: String = "",
    val encryptedText: String = "",
    val encryptedSubText: String = "",
    val encryptedBigText: String = "",
    val iv: String = "",
    val postTime: Long = System.currentTimeMillis(),
    val category: String = NotificationCategory.OTHER.id,
    val isSensitive: Boolean = false,
    val isFavorite: Boolean = false,
    val isRead: Boolean = false
)

data class NotificationItem(
    val id: Long = 0L,
    val notificationKey: String = "",
    val packageName: String = "",
    val appName: String = "",
    val title: String = "",
    val text: String = "",
    val subText: String = "",
    val bigText: String = "",
    val postTime: Long = System.currentTimeMillis(),
    val category: NotificationCategory = NotificationCategory.OTHER,
    val isSensitive: Boolean = false,
    val isFavorite: Boolean = false,
    val isRead: Boolean = false,
    val isEncrypted: Boolean = true
)
