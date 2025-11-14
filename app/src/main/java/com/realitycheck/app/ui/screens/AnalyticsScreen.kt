package com.realitycheck.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.ui.components.FullScreenLoading
import com.realitycheck.app.ui.theme.*
import com.realitycheck.app.ui.viewmodel.DecisionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DecisionViewModel = hiltViewModel()
) {
    val analytics by viewModel.analytics.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (analytics == null) {
            FullScreenLoading(message = "Loading analytics...")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Overview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total",
                        value = analytics!!.totalDecisions.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed",
                        value = analytics!!.completedDecisions.toString(),
                        modifier = Modifier.weight(1f),
                        color = BrandSuccess
                    )
                }
                
                // Decision Accuracy Score
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Decision Accuracy Score",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (analytics!!.completedDecisions > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${analytics!!.averageAccuracy.toInt()}%",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                                Text(
                                    text = "${analytics!!.completedDecisions} decisions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            LinearProgressIndicator(
                                progress = analytics!!.averageAccuracy / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = BrandPrimary
                            )
                        } else {
                            Text(
                                text = "Complete at least one decision to see your accuracy score",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                // Regret Map
                if (analytics!!.completedDecisions > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Regret Map",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val regretPatterns = analytics!!.getRegretPatterns()
                            
                            RegretPatternItem(
                                label = "High Accuracy (75%+)",
                                percentage = regretPatterns["High"] ?: 0f,
                                color = BrandSuccess
                            )
                            RegretPatternItem(
                                label = "Medium Accuracy (50-75%)",
                                percentage = regretPatterns["Medium"] ?: 0f,
                                color = BrandPrimary
                            )
                            RegretPatternItem(
                                label = "Low Accuracy (<50%)",
                                percentage = regretPatterns["Low"] ?: 0f,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Time-Based Trends (Over Weeks)
                val timeTrends = analytics!!.getTimeBasedTrends()
                if (timeTrends.isNotEmpty() && timeTrends.size >= 2) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Trends Over Time",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Your accuracy pattern over weeks:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            // Show trend direction
                            val recentWeeks = timeTrends.takeLast(4)
                            val trendDirection = if (recentWeeks.size >= 2) {
                                val first = recentWeeks.first().averageAccuracy
                                val last = recentWeeks.last().averageAccuracy
                                if (last > first + 5) "📈 Improving"
                                else if (last < first - 5) "📉 Declining"
                                else "➡️ Stable"
                            } else ""
                            
                            if (trendDirection.isNotEmpty()) {
                                Text(
                                    text = trendDirection,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (trendDirection.contains("Improving")) BrandSuccess
                                    else if (trendDirection.contains("Declining")) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Show recent weeks
                            recentWeeks.forEach { trend ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Week ${trend.weekLabel.split("-W").last()}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LinearProgressIndicator(
                                            progress = trend.averageAccuracy / 100f,
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = BrandPrimary
                                        )
                                        Text(
                                            text = "${trend.averageAccuracy.toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Overconfidence Patterns
                val overconfidencePatterns = analytics!!.getOverconfidencePatterns()
                if (overconfidencePatterns.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Overconfidence Patterns",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Where your brain was too confident but wrong:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            overconfidencePatterns.take(3).forEach { pattern ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = pattern.decisionTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Predicted: ${pattern.prediction.take(80)}${if (pattern.prediction.length > 80) "..." else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Actual: ${pattern.outcome.take(80)}${if (pattern.outcome.length > 80) "..." else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Accuracy: ${pattern.accuracy.toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Blind Spots
                val blindSpots = analytics!!.getBlindSpots()
                if (blindSpots.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandWarning.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Blind Spots",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Patterns that appear when your predictions go wrong:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            blindSpots.forEach { blindSpot ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "\"${blindSpot.pattern}\"",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandPrimary
                                        )
                                        Text(
                                            text = blindSpot.impact,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${blindSpot.frequency}x",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                // Insights
                if (analytics!!.completedDecisions >= 5) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandPrimary.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Where Your Brain Lies to You",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val avgAccuracy = analytics!!.averageAccuracy
                            val insights = mutableListOf<String>()
                            
                            when {
                                avgAccuracy >= 80 -> insights.add("✅ Excellent! Your predictions are highly accurate. You're good at reading situations.")
                                avgAccuracy >= 60 -> insights.add("✅ Good accuracy! You're on the right track. Keep tracking to improve further.")
                                avgAccuracy >= 40 -> insights.add("⚠️ Moderate accuracy detected. Look for patterns in your low-accuracy decisions.")
                                else -> insights.add("❌ Low accuracy detected. This is a learning opportunity! Review what went wrong.")
                            }
                            
                            if (overconfidencePatterns.isNotEmpty()) {
                                insights.add("💭 You tend to be overconfident in detailed predictions. Consider being more cautious when making specific forecasts.")
                            }
                            
                            if (blindSpots.isNotEmpty()) {
                                insights.add("🎯 Watch out for these patterns: ${blindSpots.take(2).joinToString(", ") { "\"${it.pattern}\"" }}. They often appear when predictions fail.")
                            }
                            
                            if (timeTrends.isNotEmpty() && timeTrends.size >= 2) {
                                val recentTrend = timeTrends.takeLast(2)
                                if (recentTrend.last().averageAccuracy > recentTrend.first().averageAccuracy + 5) {
                                    insights.add("📈 Great news! Your accuracy is improving over time. Keep practicing!")
                                }
                            }
                            
                            insights.forEach { insight ->
                                Text(
                                    text = insight,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = BrandPrimary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RegretPatternItem(
    label: String,
    percentage: Float,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color
        )
    }
}

