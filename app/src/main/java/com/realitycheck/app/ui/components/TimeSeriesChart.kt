package com.realitycheck.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.realitycheck.app.ui.theme.*

/**
 * Time-series chart showing accuracy trends over time
 */
@Composable
fun TimeSeriesChart(
    dataPoints: List<Pair<String, Float>>, // (label, value)
    modifier: Modifier = Modifier,
    title: String = "Accuracy Trend"
) {
    if (dataPoints.isEmpty()) {
        Text(
            text = "Not enough data for chart",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = modifier.padding(Spacing.lg)
        )
        return
    }
    
    val entries = dataPoints.mapIndexed { index, (_, value) ->
        com.patrykandpatrick.vico.core.entry.FloatEntry(
            x = index.toFloat(),
            y = value
        )
    }
    
    val chartEntryModel = entryModelOf(entries)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Chart(
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = MaterialTheme.colorScheme.primary,
                        lineThickness = 4.dp,
                        pointSize = 8.dp,
                        pointColor = MaterialTheme.colorScheme.primary
                    )
                )
            ),
            chartModel = chartEntryModel,
            startAxis = rememberStartAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    "${value.toInt()}%"
                },
                guideline = null
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = AxisValueFormatter { value, _ ->
                    val index = value.toInt()
                    if (index >= 0 && index < dataPoints.size) {
                        dataPoints[index].first
                    } else ""
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

