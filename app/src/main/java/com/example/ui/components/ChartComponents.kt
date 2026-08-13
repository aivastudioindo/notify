package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryStat
import com.example.data.model.DailyStat
import com.example.data.model.HourlyStat
import com.example.data.model.TopAppStat
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import kotlin.math.max

/**
 * Daily Volume Hero Card matching the Clean Minimalism theme
 */
@Composable
fun DailyVolumeHeroCard(
    dailyStats: List<DailyStat>,
    totalToday: Int = 0,
    yesterdayCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val lavender = MinimalLavenderPrimary
    val barInactive = Color(0xFF49454F)

    val percentChangeStr = remember(totalToday, yesterdayCount) {
        if (yesterdayCount > 0) {
            val change = ((totalToday - yesterdayCount).toFloat() / yesterdayCount) * 100f
            val sign = if (change >= 0) "+" else ""
            String.format(java.util.Locale.US, "%s%.0f%% vs kemarin", sign, change)
        } else if (totalToday > 0) {
            "+100% vs kemarin"
        } else {
            "0% vs kemarin"
        }
    }

    val maxCount = remember(dailyStats) { max(1, dailyStats.maxOfOrNull { it.count } ?: 1) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "VOLUME HARIAN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MinimalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$totalToday",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Light,
                            color = lavender
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "notifikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MinimalTextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = percentChangeStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = lavender
                    )
                }
            }

            if (dailyStats.isNotEmpty()) {
                // Minimal Bar Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyStats.forEachIndexed { index, stat ->
                        val heightRatio = if (stat.count > 0) (stat.count.toFloat() / maxCount) else 0.08f
                        val isToday = index == dailyStats.size - 1

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(heightRatio.coerceIn(0.08f, 1f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(if (isToday) lavender else barInactive)
                            )
                        }
                    }
                }

                // Days of week labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailyStats.forEachIndexed { index, stat ->
                        val isToday = index == dailyStats.size - 1
                        Text(
                            text = stat.dayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) lavender else MinimalTextMuted
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat aktivitas harian",
                        style = MaterialTheme.typography.bodySmall,
                        color = MinimalTextMuted
                    )
                }
            }
        }
    }
}

/**
 * 24-Hour Interactive Bar Chart in Clean Minimalism style
 */
@Composable
fun HourlyNotificationChart(
    hourlyStats: List<HourlyStat>,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableStateOf<HourlyStat?>(null) }
    val maxCount = max(1, hourlyStats.maxOfOrNull { it.count } ?: 1)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MinimalLavenderPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aktivitas 24 Jam",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MinimalTextPrimary
                    )
                }

                if (selectedHour != null) {
                    Surface(
                        color = MinimalLavenderPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${selectedHour?.formattedHour}: ${selectedHour?.count} notif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MinimalLavenderPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(hourlyStats) {
                            detectTapGestures { offset ->
                                val barSlotWidth = size.width / 24f
                                val tappedIndex = (offset.x / barSlotWidth).toInt().coerceIn(0, 23)
                                if (tappedIndex in hourlyStats.indices) {
                                    selectedHour = hourlyStats[tappedIndex]
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height - 20.dp.toPx()
                    val barSlotWidth = canvasWidth / 24f
                    val barWidth = barSlotWidth * 0.65f

                    drawLine(
                        color = MinimalBorder,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    hourlyStats.forEachIndexed { index, stat ->
                        val barHeight = if (stat.count > 0) {
                            (stat.count.toFloat() / maxCount) * (canvasHeight - 12.dp.toPx()) + 4.dp.toPx()
                        } else {
                            2.dp.toPx()
                        }

                        val left = index * barSlotWidth + (barSlotWidth - barWidth) / 2f
                        val top = canvasHeight - barHeight
                        val isSelected = selectedHour?.hour == stat.hour

                        val barColor = when {
                            isSelected -> MinimalLavenderPrimary
                            stat.count == maxCount && stat.count > 0 -> MinimalLavenderPrimary
                            stat.count > 0 -> Color(0xFF49454F)
                            else -> Color(0xFF36343B)
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("00:00", "06:00", "12:00", "18:00", "23:00").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MinimalTextMuted
                    )
                }
            }
        }
    }
}

/**
 * 7-Day Trend Smooth Line Chart
 */
@Composable
fun DailyTrendChart(
    dailyStats: List<DailyStat>,
    modifier: Modifier = Modifier
) {
    val maxCount = max(1, dailyStats.maxOfOrNull { it.count } ?: 1)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MinimalLavenderPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tren 7 Hari Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MinimalTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dailyStats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat harian.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MinimalTextMuted
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height - 20.dp.toPx()
                        val stepX = if (dailyStats.size > 1) width / (dailyStats.size - 1) else width

                        val points = dailyStats.mapIndexed { index, stat ->
                            val x = index * stepX
                            val y = height - (stat.count.toFloat() / maxCount) * (height - 20.dp.toPx()) - 8.dp.toPx()
                            Offset(x, y)
                        }

                        drawLine(
                            color = MinimalBorder,
                            start = Offset(0f, height),
                            end = Offset(width, height),
                            strokeWidth = 1.dp.toPx()
                        )

                        if (points.isNotEmpty()) {
                            val fillPath = Path().apply {
                                moveTo(0f, height)
                                points.forEach { pt -> lineTo(pt.x, pt.y) }
                                lineTo(width, height)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MinimalLavenderPrimary.copy(alpha = 0.25f),
                                        MinimalLavenderPrimary.copy(alpha = 0.02f)
                                    )
                                )
                            )

                            val strokePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }

                            drawPath(
                                path = strokePath,
                                color = MinimalLavenderPrimary,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEach { pt ->
                                drawCircle(
                                    color = MinimalLavenderPrimary,
                                    radius = 3.5.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dailyStats.forEach { stat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stat.dayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MinimalTextMuted
                            )
                            Text(
                                text = "${stat.count}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MinimalTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Breakdown in Clean Minimalism style
 */
@Composable
fun CategoryDistributionChart(
    categoryStats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = null,
                    tint = MinimalLavenderPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Distribusi Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MinimalTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (categoryStats.isEmpty()) {
                Text(
                    text = "Belum ada kategori yang tercatat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MinimalTextMuted
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF36343B))
                ) {
                    categoryStats.forEach { stat ->
                        if (stat.percentage > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(stat.percentage)
                                    .fillMaxHeight()
                                    .background(stat.category.color)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryStats.take(6).forEach { stat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(stat.category.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stat.category.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MinimalTextPrimary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${stat.count}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MinimalTextMuted
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f%%", stat.percentage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalLavenderPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top App Leaderboard List
 */
@Composable
fun TopAppsLeaderboard(
    topApps: List<TopAppStat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Aplikasi Teraktif",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MinimalTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (topApps.isEmpty()) {
                Text(
                    text = "Belum ada data aplikasi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MinimalTextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    topApps.forEachIndexed { index, app ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (index == 0) MinimalLavenderPrimary else Color(0xFF49454F),
                                        shape = CircleShape,
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (index == 0) Color(0xFF381E72) else MinimalTextPrimary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MinimalTextPrimary
                                    )
                                }

                                Text(
                                    text = "${app.count} notif",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalLavenderPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF36343B))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(app.percentage / 100f)
                                        .fillMaxHeight()
                                        .background(if (index == 0) MinimalLavenderPrimary else Color(0xFFCAC4D0))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

