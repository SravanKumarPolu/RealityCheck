package com.realitycheck.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import com.realitycheck.app.data.DatabaseProvider
import com.realitycheck.app.data.Decision
import com.realitycheck.app.ui.components.FullScreenLoading
import com.realitycheck.app.ui.theme.*
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionDetailScreen(
    decisionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToRealityCheck: () -> Unit = {},
    viewModel: DecisionViewModel = hiltViewModel()
) {
    var decision by remember { mutableStateOf<Decision?>(null) }
    var similarDecisions by remember { mutableStateOf<List<Decision>>(emptyList()) }
    
    val repository = DatabaseProvider.getRepository()
    val decisions by viewModel.decisions.collectAsState(initial = emptyList())
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(decisionId, decisions) {
        withContext(Dispatchers.IO) {
            try {
            val loaded = repository.getDecisionById(decisionId)
            if (loaded != null) {
                decision = loaded
                // Find similar decisions
                val analytics = com.realitycheck.app.data.AnalyticsData(
                    totalDecisions = decisions.size,
                    completedDecisions = decisions.count { it.isCompleted() },
                    averageAccuracy = 0f,
                    decisions = decisions
                )
                similarDecisions = analytics.findSimilarDecisions(loaded, limit = 3)
                    errorMessage = null
                } else {
                    errorMessage = "Decision not found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load decision: ${e.message}"
            }
        }
    }
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    
    if (decision == null) {
        if (errorMessage != null) {
            // Show error state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = errorMessage ?: "Unknown error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.lg))
                Button(onClick = onNavigateBack) {
                    Text("Go Back")
                }
            }
        } else {
        FullScreenLoading(message = "Loading decision...")
        }
        return
    }
    
    // Safe reference after null check
    val currentDecision = decision
    val isCompleted = currentDecision.isCompleted()
    val accuracy = currentDecision.getAccuracy()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Decision Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentDecision?.let {
                                viewModel.deleteDecision(it)
                            onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (!isCompleted) {
                FloatingActionButton(
                    onClick = { onNavigateToRealityCheck() },
                    containerColor = BrandSuccess,
                    contentColor = Color.White
                ) {
                    Text("Reality\nCheck", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = currentDecision.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (currentDecision.description.isNotBlank()) {
                        Text(
                            text = currentDecision.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Tags display
                    if (currentDecision.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            items(currentDecision.tags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(Radius.sm),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.padding(end = Spacing.xs)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = Spacing.sm,
                                            vertical = Spacing.xs
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    Divider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Created",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = dateFormat.format(currentDecision.createdAt),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            // Prediction Display
            val hasQuantitativePredictions = currentDecision.predictedEnergy24h != null || 
                                            currentDecision.predictedMood24h != null || 
                                            currentDecision.predictedStress24h != null ||
                                            currentDecision.predictedOverallImpact7d != null
            
            if (hasQuantitativePredictions) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Your Predictions",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                        
                        // Short-term predictions (24h)
                        if (currentDecision.predictedEnergy24h != null || 
                            currentDecision.predictedMood24h != null || 
                            currentDecision.predictedStress24h != null) {
                            Text(
                                text = "Short-term (24h):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            if (currentDecision.predictedEnergy24h != null) {
                                PredictionRow("Energy", currentDecision.predictedEnergy24h, -5f..5f)
                            }
                            if (currentDecision.predictedMood24h != null) {
                                PredictionRow("Mood", currentDecision.predictedMood24h, -5f..5f)
                            }
                            if (currentDecision.predictedStress24h != null) {
                                PredictionRow("Stress", currentDecision.predictedStress24h, -5f..5f)
                            }
                            if (currentDecision.predictedRegretChance24h != null) {
                                PredictionRow("Regret", currentDecision.predictedRegretChance24h, -5f..5f)
                            }
                        }
                        
                        // Long-term prediction (7d)
                        if (currentDecision.predictedOverallImpact7d != null) {
                            Text(
                                text = "Long-term (7d):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            PredictionRow("Overall Impact", currentDecision.predictedOverallImpact7d, -5f..5f)
                        }
                        
                        // Confidence
                        if (currentDecision.predictionConfidence != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Confidence:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${currentDecision.predictionConfidence.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }
                        }
                        
                        // Text prediction (if exists)
                        if (currentDecision.prediction.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider()
                            Text(
                                text = currentDecision.prediction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                // Fall back to text-only prediction
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Your Prediction",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimary
                        )
                        Text(
                            text = currentDecision.prediction,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            if (isCompleted) {
                // Show quantitative outcomes if available
                val hasQuantitativeOutcomes = currentDecision.actualEnergy24h != null || 
                                              currentDecision.actualMood24h != null || 
                                              currentDecision.actualStress24h != null || 
                                              currentDecision.actualRegret24h != null
                
                if (hasQuantitativeOutcomes) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandSuccess.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Actual Outcomes",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrandSuccess
                            )
                            
                            // Show comparison: Predicted vs Actual
                            if (currentDecision.predictedEnergy24h != null && currentDecision.actualEnergy24h != null) {
                                OutcomeComparisonRow(
                                    label = "Energy",
                                    predicted = currentDecision.predictedEnergy24h,
                                    actual = currentDecision.actualEnergy24h,
                                    range = -5f..5f
                                )
                            }
                            
                            if (currentDecision.predictedMood24h != null && currentDecision.actualMood24h != null) {
                                OutcomeComparisonRow(
                                    label = "Mood",
                                    predicted = currentDecision.predictedMood24h,
                                    actual = currentDecision.actualMood24h,
                                    range = -5f..5f
                                )
                            }
                            
                            if (currentDecision.predictedStress24h != null && currentDecision.actualStress24h != null) {
                                OutcomeComparisonRow(
                                    label = "Stress",
                                    predicted = currentDecision.predictedStress24h,
                                    actual = currentDecision.actualStress24h,
                                    range = -5f..5f
                                )
                            }
                            
                            if (currentDecision.predictedRegretChance24h != null && currentDecision.actualRegret24h != null) {
                                OutcomeComparisonRow(
                                    label = "Regret",
                                    predicted = currentDecision.predictedRegretChance24h + 5f, // Normalize to 0-10
                                    actual = currentDecision.actualRegret24h,
                                    range = 0f..10f
                                )
                            }
                            
                            // Show if they followed the decision
                            if (currentDecision.followedDecision != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Divider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Followed decision:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (currentDecision.followedDecision == true) "Yes" else "No",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentDecision.followedDecision == true) BrandSuccess else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            // Show optional note if exists
                            if (currentDecision.outcome?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Divider()
                                Text(
                                    text = currentDecision.outcome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            if (currentDecision.outcomeRecordedAt != null) {
                                Text(
                                    text = "Recorded on ${dateFormat.format(currentDecision.outcomeRecordedAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                } else {
                    // Fall back to text-only outcome display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandSuccess.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Actual Outcome",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = BrandSuccess
                            )
                            Text(
                                text = currentDecision.outcome ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (currentDecision.outcomeRecordedAt != null) {
                                Text(
                                    text = "Recorded on ${dateFormat.format(currentDecision.outcomeRecordedAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                // Regret Index
                val regretIndex = currentDecision.getRegretIndex()
                if (regretIndex != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Regret Index",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${regretIndex.toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (regretIndex < 50) BrandSuccess else MaterialTheme.colorScheme.error
                                )
                            }
                            
                            LinearProgressIndicator(
                                progress = regretIndex / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = if (regretIndex < 50) BrandSuccess else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                if (accuracy != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Accuracy Score",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${accuracy.toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (accuracy >= 75) BrandSuccess
                                    else if (accuracy >= 50) BrandPrimary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                            
                            LinearProgressIndicator(
                                progress = accuracy / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = if (accuracy >= 75) BrandSuccess
                                else if (accuracy >= 50) BrandPrimary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pending Outcome",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Similar Decisions Comparison
            if (similarDecisions.isNotEmpty() && isCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Text(
                            text = "Similar Decisions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Compare with similar past decisions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        
                        similarDecisions.forEach { similar ->
                            SimilarDecisionCard(
                                current = currentDecision,
                                similar = similar
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>
) {
    val valueStr = if (range.start >= 0f) {
        value.roundToInt().toString()
    } else {
        if (value >= 0) "+${value.roundToInt()}" else value.roundToInt().toString()
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = valueStr,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary
        )
    }
}

@Composable
fun OutcomeComparisonRow(
    label: String,
    predicted: Float,
    actual: Float,
    range: ClosedFloatingPointRange<Float>
) {
    val predictedStr = if (range.start >= 0f) {
        predicted.roundToInt().toString()
    } else {
        if (predicted >= 0) "+${predicted.roundToInt()}" else predicted.roundToInt().toString()
    }
    
    val actualStr = if (range.start >= 0f) {
        actual.roundToInt().toString()
    } else {
        if (actual >= 0) "+${actual.roundToInt()}" else actual.roundToInt().toString()
    }
    
    val error = kotlin.math.abs(predicted - actual)
    val isAccurate = error <= 1.5f // Within 1.5 points
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Predicted: $predictedStr → Actual: $actualStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Text(
            text = if (isAccurate) "✓" else "✗",
            style = MaterialTheme.typography.titleMedium,
            color = if (isAccurate) BrandSuccess else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun SimilarDecisionCard(
    current: Decision,
    similar: Decision
) {
    val currentAccuracy = current.getAccuracy() ?: 0f
    val similarAccuracy = similar.getAccuracy() ?: 0f
    val currentRegret = current.getRegretIndex() ?: 0f
    val similarRegret = similar.getRegretIndex() ?: 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Text(
                text = similar.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Accuracy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${similarAccuracy.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (similarAccuracy > currentAccuracy) BrandSuccess 
                               else if (similarAccuracy < currentAccuracy) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text(
                        text = "Regret Index",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${similarRegret.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (similarRegret < currentRegret) BrandSuccess
                               else if (similarRegret > currentRegret) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

