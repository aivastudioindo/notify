package com.example.data.model

data class HourlyStat(
    val hour: Int, // 0..23
    val count: Int,
    val formattedHour: String
)

data class DailyStat(
    val dateKey: String, // "13 Aug" / "13/08"
    val timestamp: Long,
    val count: Int,
    val dayName: String
)

data class CategoryStat(
    val category: NotificationCategory,
    val count: Int,
    val percentage: Float
)

data class TopAppStat(
    val packageName: String,
    val appName: String,
    val count: Int,
    val percentage: Float,
    val category: NotificationCategory
)

data class AnalyticsSummary(
    val totalRecorded: Int = 0,
    val todayCount: Int = 0,
    val yesterdayCount: Int = 0,
    val sensitiveCount: Int = 0,
    val favoriteCount: Int = 0,
    val peakHour: String = "-",
    val peakHourCount: Int = 0,
    val mostActiveApp: String = "-",
    val topCategory: NotificationCategory = NotificationCategory.OTHER,
    val hourlyStats: List<HourlyStat> = emptyList(),
    val dailyStats: List<DailyStat> = emptyList(),
    val categoryStats: List<CategoryStat> = emptyList(),
    val topApps: List<TopAppStat> = emptyList()
)
