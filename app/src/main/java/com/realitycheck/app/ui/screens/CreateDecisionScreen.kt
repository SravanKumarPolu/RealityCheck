package com.realitycheck.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.data.Decision
import com.realitycheck.app.data.DecisionTemplate
import com.realitycheck.app.ui.components.ErrorCard
import com.realitycheck.app.ui.components.SliderRow
import com.realitycheck.app.ui.components.formatSliderValue
import com.realitycheck.app.ui.theme.*
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDecisionScreen(
    onNavigateBack: () -> Unit,
    viewModel: DecisionViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var tagInput by remember { mutableStateOf("") }
    
    // Short-term predictions (24h): -5 to +5
    var energy24h by remember { mutableStateOf(0f) }
    var mood24h by remember { mutableStateOf(0f) }
    var stress24h by remember { mutableStateOf(0f) }
    var regretChance24h by remember { mutableStateOf(0f) }
    
    // Long-term prediction (7d): -5 to +5
    var overallImpact7d by remember { mutableStateOf(0f) }
    
    // Confidence: 0-100
    var confidence by remember { mutableStateOf(50f) }
    
    // Check-in timing - auto-update based on category
    var selectedCheckInDays by remember { mutableStateOf(3) }
    
    val categories = Decision.CATEGORIES
    val checkInOptions = Decision.CHECK_IN_OPTIONS
    
    // Update check-in days when category changes
    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isNotEmpty()) {
            selectedCheckInDays = Decision.getDefaultCheckInDays(selectedCategory)
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        if (uiState is com.realitycheck.app.ui.viewmodel.DecisionUiState.Success) {
            viewModel.resetUiState()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "New Decision",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // Templates Section
            Column {
                Text(
                    text = "Quick Start Templates",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.xs)
                ) {
                    items(DecisionTemplate.TEMPLATES) { template ->
                        AssistChip(
                            onClick = {
                                // Apply template values
                                title = template.title
                                selectedCategory = template.category
                                energy24h = template.defaultEnergy24h
                                mood24h = template.defaultMood24h
                                stress24h = template.defaultStress24h
                                regretChance24h = template.defaultRegretChance24h
                                overallImpact7d = template.defaultOverallImpact7d
                                confidence = template.defaultConfidence
                                selectedCheckInDays = template.defaultCheckInDays
                            },
                            label = { 
                                Text(
                                    template.title,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
            
            // What are you deciding?
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What are you deciding?") },
                placeholder = { Text("Order Burger King at 11:30 PM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(Radius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                supportingText = {
                    Text(
                        "e.g., Take late-night bug fix call / Buy this course for ₹799",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            
            // Category Selection
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.xs)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { 
                                Text(
                                    category,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            
            // Short-term Predictions (24h)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    Text(
                        text = "Short-term (next 24 hours)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Energy
                    SliderRow(
                        label = "Energy tomorrow",
                        value = energy24h,
                        onValueChange = { energy24h = it },
                        valueRange = -5f..5f,
                        steps = 9
                    )
                    
                    // Mood
                    SliderRow(
                        label = "Mood",
                        value = mood24h,
                        onValueChange = { mood24h = it },
                        valueRange = -5f..5f,
                        steps = 9
                    )
                    
                    // Stress
                    SliderRow(
                        label = "Stress",
                        value = stress24h,
                        onValueChange = { stress24h = it },
                        valueRange = -5f..5f,
                        steps = 9
                    )
                    
                    // Regret Chance
                    SliderRow(
                        label = "Regret chance",
                        value = regretChance24h,
                        onValueChange = { regretChance24h = it },
                        valueRange = -5f..5f,
                        steps = 9
                    )
                }
            }
            
            // Long-term Prediction (7d)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Long-term (next 7 days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    SliderRow(
                        label = "Overall impact of this decision",
                        value = overallImpact7d,
                        onValueChange = { overallImpact7d = it },
                        valueRange = -5f..5f,
                        steps = 9,
                        negativeLabel = "Very Harmful",
                        positiveLabel = "Very Helpful"
                    )
                }
            }
            
            // Confidence
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.sm)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "How sure are you?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${confidence.roundToInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = confidence,
                        onValueChange = { confidence = it },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
            
            // Tags Section
            Column {
                Text(
                    text = "Tags (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("Add tag") },
                        placeholder = { Text("e.g., urgent, important") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(Radius.md),
                        trailingIcon = {
                            if (tagInput.isNotBlank()) {
                                IconButton(onClick = {
                                    val newTag = tagInput.trim()
                                    if (newTag.isNotEmpty() && !tags.contains(newTag, ignoreCase = true)) {
                                        tags = tags + newTag
                                        tagInput = ""
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add tag"
                                    )
                                }
                            }
                        }
                    )
                }
                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        items(tags) { tag ->
                            AssistChip(
                                onClick = {
                                    tags = tags.filter { it != tag }
                                },
                                label = { Text(tag) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }
                }
            }
            
            // When to check reality
            Column {
                Text(
                    text = "When to check reality",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    checkInOptions.forEach { (label, days) ->
                        FilterChip(
                            selected = selectedCheckInDays == days,
                            onClick = { selectedCheckInDays = days },
                            label = { 
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                // Custom reminder time option
                var showCustomTime by remember { mutableStateOf(false) }
                var customDays by remember { mutableStateOf(3) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showCustomTime,
                        onClick = { showCustomTime = !showCustomTime },
                        label = { 
                            Text(
                                "Custom",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (showCustomTime) {
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Days:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(60.dp)
                        )
                        Slider(
                            value = customDays.toFloat(),
                            onValueChange = { customDays = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 28,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$customDays",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                    selectedCheckInDays = customDays
                }
                
                if (selectedCategory.isNotEmpty() && !showCustomTime) {
                    Text(
                        text = "Default: ${Decision.getDefaultCheckInDays(selectedCategory)} days (${selectedCategory})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Lock in prediction button
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedCategory.isNotEmpty()) {
                        viewModel.createDecision(
                            title = title,
                            description = "", // No longer needed
                            prediction = buildPredictionText(
                                energy24h, mood24h, stress24h, regretChance24h,
                                overallImpact7d, confidence
                            ),
                            reminderDays = selectedCheckInDays,
                            category = selectedCategory,
                            energy24h = energy24h,
                            mood24h = mood24h,
                            stress24h = stress24h,
                            regretChance24h = regretChance24h,
                            overallImpact7d = overallImpact7d,
                            confidence = confidence,
                            tags = tags
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(Radius.md),
                enabled = title.isNotBlank() && selectedCategory.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = Elevation.md,
                    pressedElevation = Elevation.sm
                )
            ) {
                Text(
                    text = "Lock in prediction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Error message display
            if (uiState is com.realitycheck.app.ui.viewmodel.DecisionUiState.Error) {
                val error = uiState as com.realitycheck.app.ui.viewmodel.DecisionUiState.Error
                ErrorCard(message = error.message)
            }
        }
    }
}

// SliderRow and formatSliderValue are now in ui.components package

fun buildPredictionText(
    energy24h: Float,
    mood24h: Float,
    stress24h: Float,
    regretChance24h: Float,
    overallImpact7d: Float,
    confidence: Float
): String {
    return "Energy: ${formatSliderValue(energy24h)}, Mood: ${formatSliderValue(mood24h)}, " +
            "Stress: ${formatSliderValue(stress24h)}, Regret: ${formatSliderValue(regretChance24h)}, " +
            "Impact: ${formatSliderValue(overallImpact7d)}, Confidence: ${confidence.roundToInt()}%"
}
