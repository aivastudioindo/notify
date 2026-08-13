package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalyticsSummary
import com.example.ui.components.CategoryDistributionChart
import com.example.ui.components.DailyTrendChart
import com.example.ui.components.DailyVolumeHeroCard
import com.example.ui.components.HourlyNotificationChart
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalCardBackground
import com.example.ui.theme.MinimalDarkBackground
import com.example.ui.theme.MinimalLavenderPrimary
import com.example.ui.theme.MinimalSurfaceElevated
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@Composable
fun AnalyticsScreen(
    analyticsSummary: AnalyticsSummary,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Volume Hero Metric Card
        item {
            DailyVolumeHeroCard(
                dailyStats = analyticsSummary.dailyStats,
                totalToday = analyticsSummary.todayCount
            )
        }

        // 24-Hour Distribution Chart
        item {
            HourlyNotificationChart(
                hourlyStats = analyticsSummary.hourlyStats
            )
        }

        // 7-Day Trend Chart
        item {
            DailyTrendChart(
                dailyStats = analyticsSummary.dailyStats
            )
        }

        // Category Breakdown Chart
        item {
            CategoryDistributionChart(
                categoryStats = analyticsSummary.categoryStats
            )
        }

        // Security & OTP Summary Insights Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MinimalSurfaceElevated),
                border = BorderStroke(1.dp, MinimalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MinimalCardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MinimalLavenderPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Ringkasan Privasi & Keamanan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalTextPrimary
                            )
                            Text(
                                text = "Status proteksi pesan di database",
                                style = MaterialTheme.typography.labelSmall,
                                color = MinimalTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = MinimalCardBackground,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Pesan Sensitif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${analyticsSummary.sensitiveCount}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalLavenderPrimary
                                )
                                Text(
                                    text = "OTP & Finansial",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextSecondary
                                )
                            }
                        }

                        Surface(
                            color = MinimalCardBackground,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Favorit Tersimpan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${analyticsSummary.favoriteCount}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalLavenderPrimary
                                )
                                Text(
                                    text = "Pesan Disematkan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MinimalTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
