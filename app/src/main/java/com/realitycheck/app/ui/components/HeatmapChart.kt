package com.realitycheck.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.realitycheck.app.ui.theme.*

/**
 * Heatmap showing regret/accuracy patterns by category and time
 */
@Composable
fun HeatmapChart(
    data: Map<String, Map<String, Float>>, // category -> (timeLabel -> value)
    modifier: Modifier = Modifier,
    title: String = "Pattern Heatmap",
    valueLabel: String = "Regret Index"
) {
    if (data.isEmpty()) {
        Text(
            text = "Not enough data for heatmap",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = modifier.padding(Spacing.lg)
        )
        return
    }
    
    val categories = data.keys.toList()
    val timeLabels = data.values.flatMap { it.keys }.distinct().sorted()
    
    // Find min/max for color scaling
    val allValues = data.values.flatMap { it.values }
    val minValue = allValues.minOrNull() ?: 0f
    val maxValue = allValues.maxOrNull() ?: 100f
    val range = maxValue - minValue
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Low",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { index ->
                    val intensity = index / 4f
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getHeatmapColor(intensity))
                    )
                }
            }
            Text(
                text = "High",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Heatmap grid
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header row (time labels)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(modifier = Modifier.width(80.dp)) // Category column width
                timeLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Data rows
            categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(80.dp)
                    )
                    timeLabels.forEach { timeLabel ->
                        val value = data[category]?.get(timeLabel) ?: 0f
                        val normalizedValue = if (range > 0) (value - minValue) / range else 0f
                        val color = getHeatmapColor(normalizedValue)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${value.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (normalizedValue > 0.5f) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getHeatmapColor(intensity: Float): Color {
    // Green (low) to Red (high) gradient
    val clampedIntensity = intensity.coerceIn(0f, 1f)
    return when {
        clampedIntensity < 0.25f -> Color(0xFF00B894) // Green
        clampedIntensity < 0.5f -> Color(0xFFFDCB6E) // Yellow
        clampedIntensity < 0.75f -> Color(0xFFFF7675) // Orange
        else -> Color(0xFFE17055) // Red
    }
}

