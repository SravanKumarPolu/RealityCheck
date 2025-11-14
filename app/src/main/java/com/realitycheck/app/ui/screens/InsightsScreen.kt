package com.realitycheck.app.ui.screens

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
import com.realitycheck.app.ui.components.HeatmapChart
import com.realitycheck.app.ui.components.TimeSeriesChart
import com.realitycheck.app.ui.theme.*
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import kotlin.math.max
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit = {},
    viewModel: DecisionViewModel = hiltViewModel()
) {
    val analytics by viewModel.analytics.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToExport) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export Data"
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
        val currentAnalytics = analytics
        if (currentAnalytics == null || currentAnalytics.completedDecisions == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No insights yet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete at least one decision to see your insights",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Decision Accuracy Score
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Decision Accuracy Score",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${currentAnalytics.averageAccuracy.toInt()}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                        
                        Text(
                            text = "You're ${currentAnalytics.averageAccuracy.toInt()}% accurate about your future feelings.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        LinearProgressIndicator(
                            progress = currentAnalytics.averageAccuracy / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = BrandPrimary
                        )
                    }
                }
                
                // Top Regret Areas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Top Regret Areas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val topRegretCategory = currentAnalytics.getTopRegretCategory()
                        if (topRegretCategory != null) {
                            Text(
                                text = "Most regretful category: ${topRegretCategory.first}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        val repeatedRegret = currentAnalytics.getRepeatedRegret()
                        if (repeatedRegret != null) {
                            Text(
                                text = "Repeated regret: $repeatedRegret",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // V1 Requirement: Regret score per category
                        val regretPerCategory = currentAnalytics.getRegretScorePerCategory()
                        if (regretPerCategory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Regret Score by Category:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            regretPerCategory.toList().sortedByDescending { it.second }.forEach { (category, score) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${score.toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (score >= 60) MaterialTheme.colorScheme.error else BrandPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Miscalibration Indicators
                val overconfidence = currentAnalytics.getOverconfidenceByCategory()
                val underestimation = currentAnalytics.getUnderestimationPattern()
                
                if (overconfidence != null || underestimation != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Miscalibration Indicators",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (overconfidence != null) {
                                Text(
                                    text = overconfidence,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            if (underestimation != null) {
                                Text(
                                    text = underestimation,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                
                // Category Accuracy Bar Chart
                val categoryAccuracy = currentAnalytics.getCategoryAccuracy()
                if (categoryAccuracy.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandSuccess.copy(alpha = 0.05f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Predictions vs Reality",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Accuracy by category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            val maxAccuracy = categoryAccuracy.values.maxOrNull() ?: 100f
                            
                            categoryAccuracy.toList().sortedByDescending { it.second }.forEach { (category, accuracy) ->
                                CategoryBarChartRow(
                                    category = category,
                                    accuracy = accuracy,
                                    maxAccuracy = max(maxAccuracy, 1f) // Avoid division by zero
                                )
                            }
                        }
                    }
                }
                
                // Streak
                val streak = currentAnalytics.getDecisionStreak()
                if (streak > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandSuccess.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Streak",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$streak day${if (streak != 1) "s" else ""}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandSuccess
                                )
                                Text(
                                    text = "Days with at least 1 decision logged",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Time-Series Chart
                val timeTrends = currentAnalytics.getTimeBasedTrends()
                if (timeTrends.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TimeSeriesChart(
                                dataPoints = timeTrends.map { it.weekLabel to it.averageAccuracy },
                                title = "Accuracy Over Time"
                            )
                        }
                    }
                }
                
                // Heatmap Chart
                val heatmapData = buildHeatmapData(currentAnalytics)
                if (heatmapData.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HeatmapChart(
                                data = heatmapData,
                                title = "Regret Index by Category & Time",
                                valueLabel = "Regret Index"
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildHeatmapData(analytics: com.realitycheck.app.data.AnalyticsData): Map<String, Map<String, Float>> {
    val completed = analytics.decisions.filter { 
        it.isCompleted() && 
        it.getRegretIndex() != null && 
        it.category != null &&
        it.outcomeRecordedAt != null
    }
    
    if (completed.isEmpty()) return emptyMap()
    
    val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val result = mutableMapOf<String, MutableMap<String, Float>>()
    
    // Group by category and month, then calculate averages
    val grouped = completed
        .mapNotNull { decision ->
            val category = decision.category ?: return@mapNotNull null
        val month = dateFormat.format(decision.outcomeRecordedAt ?: decision.createdAt)
            Triple(category, month, decision)
    }
        .groupBy { it.first to it.second }
    
    grouped.forEach { (categoryMonth, triplets) ->
        val category = categoryMonth.first
        val month = categoryMonth.second
        val decisions = triplets.map { it.third }
        val avgRegret = decisions.mapNotNull { it.getRegretIndex() }.average().toFloat()
        
        val categoryMap = result.getOrPut(category) { mutableMapOf() }
        categoryMap[month] = avgRegret
    }
    
    return result
}

@Composable
fun CategoryBarChartRow(
    category: String,
    accuracy: Float,
    maxAccuracy: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${accuracy.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary
            )
        }
        
        LinearProgressIndicator(
            progress = accuracy / maxAccuracy,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = when {
                accuracy >= 75 -> BrandSuccess
                accuracy >= 50 -> BrandPrimary
                else -> MaterialTheme.colorScheme.error
            }
        )
    }
}

