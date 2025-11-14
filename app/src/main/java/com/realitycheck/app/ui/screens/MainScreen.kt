package com.realitycheck.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.data.Decision
import com.realitycheck.app.ui.components.DecisionListSkeleton
import com.realitycheck.app.ui.components.FilterBar
import com.realitycheck.app.ui.components.RealityCheckLogo
import com.realitycheck.app.ui.components.RealityCheckLogoSmall
import com.realitycheck.app.ui.theme.*
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: DecisionViewModel = hiltViewModel()
) {
    val decisions by viewModel.decisions.collectAsState(initial = emptyList())
    val analytics by viewModel.analytics.collectAsState()
    val groups by viewModel.groups.collectAsState(initial = emptyList())
    
    // Filter state
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var availableTags by remember { mutableStateOf<List<String>>(emptyList()) }
    val repository = viewModel.repository
    val scope = rememberCoroutineScope()
    
    // Load available tags
    LaunchedEffect(Unit) {
        scope.launch {
            availableTags = repository.getAllTags()
        }
    }
    
    // Filter decisions
    val filteredDecisions = remember(decisions, selectedCategory, selectedTags, selectedGroupId) {
        val categoryFiltered = repository.filterDecisions(
            decisions = decisions,
            selectedCategory = selectedCategory,
            selectedTags = selectedTags
        )
        // Apply group filter
        if (selectedGroupId != null) {
            categoryFiltered.filter { it.groupId == selectedGroupId }
        } else {
            categoryFiltered
        }
    }
    
    // Separate active and completed decisions
    val activeDecisions = filteredDecisions.filter { !it.isCompleted() }
    val completedDecisions = filteredDecisions.filter { it.isCompleted() }
    
    // Calculate streak
    val streak = analytics?.getDecisionStreak() ?: 0
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Today's Decisions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.pressAnimation()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.pressAnimation()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (decisions.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onNavigateToCreate = onNavigateToCreate
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Filter Bar
                item {
                    FilterBar(
                        selectedCategory = selectedCategory,
                        selectedTags = selectedTags,
                        availableTags = availableTags,
                        categories = Decision.CATEGORIES,
                        groups = groups,
                        selectedGroupId = selectedGroupId,
                        onCategorySelected = { selectedCategory = it },
                        onTagSelected = { 
                            if (!selectedTags.contains(it)) {
                                selectedTags = selectedTags + it
                            }
                        },
                        onTagRemoved = { selectedTags = selectedTags.filter { tag -> tag != it } },
                        onGroupSelected = { selectedGroupId = it },
                        onClearFilters = {
                            selectedCategory = null
                            selectedTags = emptyList()
                            selectedGroupId = null
                        }
                    )
                }
                
                // Streak Card
                if (streak > 0) {
                    item {
                        StreakCard(streak = streak)
                    }
                }
                
                // Big CTA Button
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                    ) {
                        Button(
                            onClick = onNavigateToCreate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressAnimation(),
                            shape = RoundedCornerShape(Radius.lg),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = Elevation.md,
                                pressedElevation = Elevation.sm
                            ),
                            contentPadding = PaddingValues(
                                horizontal = Spacing.xl,
                                vertical = Spacing.lg
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "Log a Decision Before You Act",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Active Decisions Section
                if (activeDecisions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Decisions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                top = Spacing.md,
                                bottom = Spacing.sm
                            )
                        )
                    }
                    items(
                        items = activeDecisions,
                        key = { it.id }
                    ) { decision ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) {
                            DecisionCard(
                                decision = decision,
                                onClick = { onNavigateToDetail(decision.id) }
                            )
                        }
                    }
                }
                
                // Completed Decisions Section
                if (completedDecisions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed Decisions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(
                                top = Spacing.md,
                                bottom = Spacing.sm
                            )
                        )
                    }
                    items(
                        items = completedDecisions,
                        key = { it.id }
                    ) { decision ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        ) {
                            DecisionCard(
                                decision = decision,
                                onClick = { onNavigateToDetail(decision.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DecisionCard(
    decision: Decision,
    onClick: () -> Unit
) {
    val isCompleted = decision.isCompleted()
    val regretIndicator = decision.getRegretIndicator()
    val checkInTime = decision.getCheckInTimeString()
    val category = decision.getDisplayCategory()
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.md,
            pressedElevation = Elevation.sm
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // Title Row with Regret/Good Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = decision.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Show regret/good indicator for completed
                if (isCompleted && regretIndicator != null) {
                    Text(
                        text = regretIndicator,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = Spacing.sm)
                    )
                }
            }
            
            // Category Chip
            Surface(
                shape = RoundedCornerShape(Radius.sm),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(top = Spacing.xs)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(
                        horizontal = Spacing.sm,
                        vertical = Spacing.xs
                    )
                )
            }
            
            // Tags display
            if (decision.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    items(decision.tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(Radius.xs),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(end = Spacing.xs)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = Spacing.xs,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
            }
            
            // Time until check-in (for active decisions)
            if (!isCompleted && checkInTime != null) {
                Row(
                    modifier = Modifier.padding(top = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = checkInTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onNavigateToCreate: () -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(Spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxl)
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                RealityCheckLogo(size = 120.dp)
            }
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    delayMillis = 200,
                    animationSpec = tween(400)
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "No decisions yet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Start tracking your choices and train your intuition!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.lg)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    delayMillis = 400,
                    animationSpec = tween(400)
                ) + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Button(
                    onClick = onNavigateToCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressAnimation(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = Elevation.md,
                        pressedElevation = Elevation.sm
                    ),
                    contentPadding = PaddingValues(
                        horizontal = Spacing.xl,
                        vertical = Spacing.lg
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = "Log a Decision Before You Act",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Extension for press animation
fun Modifier.pressAnimation(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    
    this
        .clickable(interactionSource = interactionSource, indication = null) {}
        .scale(scale)
}

@Composable
fun StreakCard(streak: Int) {
    val badge = getStreakBadge(streak)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.sm
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = "🔥 $streak Day Streak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                } else {
                    val nextMilestone = getNextStreakMilestone(streak)
                    if (nextMilestone > 0) {
                        Text(
                            text = "${nextMilestone - streak} days until next milestone",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Badge icon
            if (badge != null) {
                Text(
                    text = getBadgeEmoji(streak),
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }
    }
}

private fun getStreakBadge(streak: Int): String? {
    return when {
        streak >= 100 -> "Centurion! 🏆"
        streak >= 50 -> "Half Century! 🎯"
        streak >= 30 -> "Monthly Champion! 📅"
        streak >= 21 -> "3 Week Warrior! ⚔️"
        streak >= 14 -> "2 Week Streak! 🌟"
        streak >= 7 -> "Week Warrior! 💪"
        else -> null
    }
}

private fun getBadgeEmoji(streak: Int): String {
    return when {
        streak >= 100 -> "🏆"
        streak >= 50 -> "🎯"
        streak >= 30 -> "📅"
        streak >= 21 -> "⚔️"
        streak >= 14 -> "🌟"
        streak >= 7 -> "💪"
        else -> "🔥"
    }
}

private fun getNextStreakMilestone(currentStreak: Int): Int {
    val milestones = listOf(7, 14, 21, 30, 50, 100)
    return milestones.firstOrNull { it > currentStreak } ?: 0
}

