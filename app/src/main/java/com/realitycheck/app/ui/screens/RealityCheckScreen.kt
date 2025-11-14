package com.realitycheck.app.ui.screens

import androidx.compose.foundation.layout.*
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
import com.realitycheck.app.data.DatabaseProvider
import com.realitycheck.app.data.Decision
import com.realitycheck.app.ui.components.ErrorCard
import com.realitycheck.app.ui.components.FullScreenLoading
import com.realitycheck.app.ui.components.SliderRow
import com.realitycheck.app.ui.components.formatSliderValue
import com.realitycheck.app.ui.theme.*
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealityCheckScreen(
    decisionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: DecisionViewModel = hiltViewModel()
) {
    var decision by remember { mutableStateOf<Decision?>(null) }
    val repository = DatabaseProvider.getRepository()
    
    // Outcome sliders
    var actualEnergy by remember { mutableStateOf(0f) }
    var actualMood by remember { mutableStateOf(0f) }
    var actualStress by remember { mutableStateOf(0f) }
    var actualRegret by remember { mutableStateOf(0f) } // 0-10 scale
    
    // Optional note
    var outcomeNote by remember { mutableStateOf("") }
    
    // Did you follow the decision?
    var followedDecision by remember { mutableStateOf<Boolean?>(null) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load decision and initialize sliders if already has data
    LaunchedEffect(decisionId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
            val loaded = repository.getDecisionById(decisionId)
            if (loaded != null) {
                decision = loaded
                actualEnergy = loaded.actualEnergy24h ?: loaded.predictedEnergy24h ?: 0f
                actualMood = loaded.actualMood24h ?: loaded.predictedMood24h ?: 0f
                actualStress = loaded.actualStress24h ?: loaded.predictedStress24h ?: 0f
                actualRegret = loaded.actualRegret24h ?: (loaded.predictedRegretChance24h?.let { it + 5f } ?: 0f)
                outcomeNote = loaded.outcome ?: ""
                followedDecision = loaded.followedDecision
                    errorMessage = null
                } else {
                    errorMessage = "Decision not found"
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load decision: ${e.message}"
            }
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        if (uiState is com.realitycheck.app.ui.viewmodel.DecisionUiState.Success) {
            viewModel.resetUiState()
            onNavigateBack()
        }
    }
    
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
                ErrorCard(message = errorMessage ?: "Unknown error")
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reality Check") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with decision title
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
                        text = "Time to reality-check:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = decision?.let { "\"${it.title}\"" } ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Text(
                text = "What happened actually?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Outcome Sliders
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md),
                colors = CardDefaults.cardColors(
                    containerColor = BrandSuccess.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Energy (-5 to +5)
                    decision?.predictedEnergy24h?.let { predictedEnergy ->
                        SliderRow(
                            label = "Energy",
                            value = actualEnergy,
                            onValueChange = { actualEnergy = it },
                            valueRange = -5f..5f,
                            steps = 9,
                            predictedValue = predictedEnergy
                        )
                    }
                    
                    // Mood (-5 to +5)
                    decision?.predictedMood24h?.let { predictedMood ->
                        SliderRow(
                            label = "Mood",
                            value = actualMood,
                            onValueChange = { actualMood = it },
                            valueRange = -5f..5f,
                            steps = 9,
                            predictedValue = predictedMood
                        )
                    }
                    
                    // Stress (-5 to +5)
                    decision?.predictedStress24h?.let { predictedStress ->
                        SliderRow(
                            label = "Stress",
                            value = actualStress,
                            onValueChange = { actualStress = it },
                            valueRange = -5f..5f,
                            steps = 9,
                            predictedValue = predictedStress
                        )
                    }
                    
                    // Regret (0-10)
                    SliderRow(
                        label = "Regret",
                        value = actualRegret,
                        onValueChange = { actualRegret = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        predictedValue = decision?.predictedRegretChance24h?.let { it + 5f } // Convert -5..+5 to 0..10
                    )
                }
            }
            
            // Short note (optional)
            OutlinedTextField(
                value = outcomeNote,
                onValueChange = { outcomeNote = it },
                label = { Text("Short note (optional)") },
                placeholder = { Text("Felt heavy and sleepy at standup.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(Radius.md)
            )
            
            // Did you follow the decision?
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.md)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Did you follow the decision?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Yes button
                        FilterChip(
                            selected = followedDecision == true,
                            onClick = { followedDecision = true },
                            label = { Text("Yes, I did it") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // No button
                        FilterChip(
                            selected = followedDecision == false,
                            onClick = { followedDecision = false },
                            label = { Text("No, I skipped/changed my mind") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Save Reality button
            Button(
                onClick = {
                    decision?.let { currentDecision ->
                        viewModel.updateDecisionOutcome(
                            decision = currentDecision,
                            outcome = outcomeNote,
                            actualEnergy24h = actualEnergy,
                            actualMood24h = actualMood,
                            actualStress24h = actualStress,
                            actualRegret24h = actualRegret,
                            followedDecision = followedDecision
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(Radius.md),
                enabled = followedDecision != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandSuccess
                )
            ) {
                Text(
                    text = "Save Reality",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
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

