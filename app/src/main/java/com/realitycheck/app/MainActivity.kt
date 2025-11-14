package com.realitycheck.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.data.AppPreferences
import com.realitycheck.app.data.ThemePreferences
import com.realitycheck.app.ui.navigation.NavGraph
import com.realitycheck.app.ui.theme.RealityCheckTheme
import com.realitycheck.app.ui.viewmodel.DecisionViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent(
                quickLog = intent.getBooleanExtra(
                    com.realitycheck.app.widget.QuickDecisionWidget.EXTRA_QUICK_LOG,
                    false
                ),
                templateId = intent.getStringExtra(
                    com.realitycheck.app.widget.QuickDecisionWidget.EXTRA_TEMPLATE_ID
                ),
                decisionId = intent.getLongExtra("decision_id", -1L).takeIf { it != -1L }
            )
        }
    }
}

@Composable
fun MainContent(
    quickLog: Boolean = false,
    templateId: String? = null,
    decisionId: Long? = null
) {
    val context = LocalContext.current
    val themePreferences = remember { ThemePreferences(context) }
    
    RealityCheckTheme(themePreferences = themePreferences) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            
            // Navigate to create screen if quick log requested
            if (quickLog) {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.navigate(com.realitycheck.app.ui.navigation.Screen.CreateDecision.route)
                }
            }
            
            // Navigate to decision detail if notification clicked
            decisionId?.let { id ->
                androidx.compose.runtime.LaunchedEffect(id) {
                    navController.navigate(
                        com.realitycheck.app.ui.navigation.Screen.DecisionDetail.createRoute(id)
                    )
                }
            }
            
            val appPreferences = remember { AppPreferences(context) }
            val viewModel: DecisionViewModel = hiltViewModel()
            
            NavGraph(
                navController = navController,
                appPreferences = appPreferences,
                viewModel = viewModel
            )
        }
    }
}

