package com.realitycheck.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.data.Decision
import com.realitycheck.app.data.DecisionTemplate
import com.realitycheck.app.ui.components.ErrorCard
import com.realitycheck.app.ui.components.NotificationPermissionBanner
import com.realitycheck.app.ui.components.PatternSuggestionBanner
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
    
    // Validation errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    
    // Pattern suggestions
    var patternSuggestion by remember { mutableStateOf<com.realitycheck.app.data.DecisionPatternSuggestion?>(null) }
    val decisions by viewModel.decisions.collectAsState(initial = emptyList())
    
    // Group selection
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    val groups by viewModel.groups.collectAsState(initial = emptyList())
    
    val categories = Decision.CATEGORIES
    val checkInOptions = Decision.CHECK_IN_OPTIONS
    
    // Real-time validation
    LaunchedEffect(title, selectedCategory) {
        if (hasAttemptedSubmit || title.isNotEmpty() || selectedCategory.isNotEmpty()) {
            // Validate title
            titleError = when {
                title.isBlank() -> "Title cannot be empty"
                title.trim().length > 200 -> "Title is too long (maximum 200 characters)"
                else -> null
            }
            
            // Validate category
            categoryError = when {
                selectedCategory.isBlank() -> "Please select a category"
                selectedCategory !in Decision.CATEGORIES -> "Invalid category selected"
                else -> null
            }
        }
    }
    
    // Update check-in days when category changes
    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isNotEmpty()) {
            selectedCheckInDays = Decision.getDefaultCheckInDays(selectedCategory)
            // Clear category error when category is selected
            if (categoryError != null) {
                categoryError = null
            }
        }
    }
    
    // Get pattern suggestions when title and category are entered
    LaunchedEffect(title, selectedCategory, decisions) {
        if (title.isNotBlank() && selectedCategory.isNotEmpty() && title.length > 3 && decisions.isNotEmpty()) {
            val analytics = com.realitycheck.app.data.AnalyticsData(
                totalDecisions = decisions.size,
                completedDecisions = decisions.count { it.isCompleted() },
                averageAccuracy = 0f,
                decisions = decisions
            )
            patternSuggestion = analytics.getDecisionPatternSuggestion(title, selectedCategory)
        } else {
            patternSuggestion = null
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        if (uiState is com.realitycheck.app.ui.viewmodel.DecisionUiState.Success) {
            viewModel.resetUiState()
            onNavigateBack()
        } else if (uiState is com.realitycheck.app.ui.viewmodel.DecisionUiState.Error) {
            // Show validation errors from ViewModel
            val error = uiState as com.realitycheck.app.ui.viewmodel.DecisionUiState.Error
            val errorMessage = error.message.lowercase()
            
            when {
                errorMessage.contains("title") -> titleError = error.message
                errorMessage.contains("category") -> categoryError = error.message
            }
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
            // Notification Permission Banner
            NotificationPermissionBanner(viewModel = viewModel)
            
            // Decision Pattern Suggestion Banner
            patternSuggestion?.let { suggestion ->
                PatternSuggestionBanner(suggestion = suggestion)
            }
            
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
            Column {
            OutlinedTextField(
                value = title,
                    onValueChange = { 
                        title = it
                        // Clear error when user starts typing
                        if (titleError != null) {
                            titleError = null
                        }
                    },
                label = { Text("What are you deciding?") },
                placeholder = { Text("Order Burger King at 11:30 PM") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(Radius.md),
                    isError = titleError != null,
                colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (titleError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (titleError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorSupportingTextColor = MaterialTheme.colorScheme.error
                ),
                supportingText = {
                        if (titleError != null) {
                            Text(
                                text = titleError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                    Text(
                        "e.g., Take late-night bug fix call / Buy this course for ₹799",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                    }
                )
                
                // Show character count
                if (title.isNotEmpty() && titleError == null) {
                    Text(
                        text = "${title.trim().length}/200 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = Spacing.md, top = Spacing.xs)
                    )
                }
            }
            
            // Category Selection
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                    
                    // Show required indicator
                    if (categoryError != null) {
                        Text(
                            text = "Required *",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    colors = CardDefaults.cardColors(
                        containerColor = if (categoryError != null && selectedCategory.isEmpty()) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        }
                    ),
                    border = if (categoryError != null && selectedCategory.isEmpty()) {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error
                        )
                    } else {
                        null
                    }
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.xs)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                                onClick = { 
                                    selectedCategory = category
                                    // Clear error when category is selected
                                    if (categoryError != null) {
                                        categoryError = null
                                    }
                                },
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
                
                // Show category error message
                if (categoryError != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = Spacing.md, top = Spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error", // Important for screen readers
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = categoryError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Group Selection (Optional)
            Column {
                Text(
                    text = "Group/Project (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                
                if (groups.isEmpty()) {
                    Text(
                        text = "No groups available. Create groups in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.sm)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        contentPadding = PaddingValues(horizontal = Spacing.xs)
                    ) {
                        // "No Group" option
                        item {
                            FilterChip(
                                selected = selectedGroupId == null,
                                onClick = { selectedGroupId = null },
                                label = { 
                                    Text(
                                        "No Group",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        
                        // Group options
                        items(groups) { group ->
                            FilterChip(
                                selected = selectedGroupId == group.id,
                                onClick = { 
                                    selectedGroupId = if (selectedGroupId == group.id) null else group.id
                                },
                                label = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = androidx.compose.ui.graphics.Color(
                                                        android.graphics.Color.parseColor(group.color)
                                                    ),
                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                )
                                        )
                                        Text(
                                            group.name,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
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
                                    if (newTag.isNotEmpty() && !tags.any { it.equals(newTag, ignoreCase = true) }) {
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
                    hasAttemptedSubmit = true
                    
                    // Trigger validation
                    titleError = when {
                        title.isBlank() -> "Title cannot be empty"
                        title.trim().length > 200 -> "Title is too long (maximum 200 characters)"
                        else -> null
                    }
                    
                    categoryError = when {
                        selectedCategory.isBlank() -> "Please select a category"
                        selectedCategory !in Decision.CATEGORIES -> "Invalid category selected"
                        else -> null
                    }
                    
                    // Only submit if validation passes
                    if (titleError == null && categoryError == null && title.isNotBlank() && selectedCategory.isNotEmpty()) {
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
                            tags = tags,
                            groupId = selectedGroupId
                        )
                    } else {
                        // Show validation summary if there are errors
                        if (titleError != null || categoryError != null) {
                            // Scroll to top to show errors
                            // Errors are already displayed inline
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(Radius.md),
                enabled = title.isNotBlank() && selectedCategory.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            
            // Show validation summary when button is clicked with errors
            if (hasAttemptedSubmit && (titleError != null || categoryError != null)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Please fix the following errors:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            if (titleError != null) {
                                Text(
                                    text = "• ${titleError}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(top = Spacing.xs)
                                )
                            }
                            if (categoryError != null) {
                                Text(
                                    text = "• ${categoryError}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(top = Spacing.xs)
                                )
                            }
                        }
                    }
                }
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
