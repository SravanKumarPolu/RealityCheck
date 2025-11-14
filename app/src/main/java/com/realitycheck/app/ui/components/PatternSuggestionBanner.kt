package com.realitycheck.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.realitycheck.app.data.DecisionPatternSuggestion
import com.realitycheck.app.data.DecisionPatternType
import com.realitycheck.app.ui.theme.Spacing

/**
 * Banner that shows proactive decision pattern suggestions
 * Based on past similar decisions
 */
@Composable
fun PatternSuggestionBanner(
    suggestion: DecisionPatternSuggestion,
    modifier: Modifier = Modifier
) {
    val (icon, containerColor, contentColor) = when (suggestion.type) {
        DecisionPatternType.HIGH_REGRET -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        DecisionPatternType.LOW_ACCURACY -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        DecisionPatternType.POSITIVE_PATTERN -> Triple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = when (suggestion.type) {
                        DecisionPatternType.HIGH_REGRET -> "Pattern Detected"
                        DecisionPatternType.LOW_ACCURACY -> "Prediction Pattern"
                        DecisionPatternType.POSITIVE_PATTERN -> "Good Pattern"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = suggestion.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
                if (suggestion.similarCount > 0) {
                    Text(
                        text = "Based on ${suggestion.similarCount} similar past decision${if (suggestion.similarCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

