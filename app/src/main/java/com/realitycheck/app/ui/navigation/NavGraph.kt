package com.realitycheck.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.realitycheck.app.data.AppPreferences
import com.realitycheck.app.ui.screens.AnalyticsScreen
import com.realitycheck.app.ui.screens.CreateDecisionScreen
import com.realitycheck.app.ui.screens.DecisionDetailScreen
import com.realitycheck.app.ui.screens.ExportScreen
import com.realitycheck.app.ui.screens.GroupsScreen
import com.realitycheck.app.ui.screens.InsightsScreen
import com.realitycheck.app.ui.screens.MainScreen
import com.realitycheck.app.ui.screens.OnboardingScreen
import com.realitycheck.app.ui.screens.RealityCheckScreen
import com.realitycheck.app.ui.screens.SettingsScreen
import com.realitycheck.app.ui.viewmodel.DecisionViewModel
import javax.inject.Inject

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object CreateDecision : Screen("create_decision")
    object DecisionDetail : Screen("decision_detail/{decisionId}") {
        fun createRoute(decisionId: Long) = "decision_detail/$decisionId"
    }
    object RealityCheck : Screen("reality_check/{decisionId}") {
        fun createRoute(decisionId: Long) = "reality_check/$decisionId"
    }
    object Analytics : Screen("analytics")
    object Insights : Screen("insights")
    object Export : Screen("export")
    object Settings : Screen("settings")
    object Groups : Screen("groups")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    appPreferences: AppPreferences,
    viewModel: DecisionViewModel = hiltViewModel()
) {
    // Check if onboarding is needed
    val needsOnboarding = !appPreferences.hasCompletedOnboarding()
    val startDestination = if (needsOnboarding) Screen.Onboarding.route else Screen.Main.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        // Clear back stack so user can't go back to onboarding
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                appPreferences = appPreferences,
                viewModel = viewModel
            )
        }
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToCreate = { navController.navigate(Screen.CreateDecision.route) },
                onNavigateToDetail = { decisionId ->
                    navController.navigate(Screen.DecisionDetail.createRoute(decisionId))
                },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.CreateDecision.route) {
            CreateDecisionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.DecisionDetail.route) { backStackEntry ->
            val decisionId = backStackEntry.arguments?.getString("decisionId")?.toLongOrNull()
            if (decisionId != null) {
                DecisionDetailScreen(
                    decisionId = decisionId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRealityCheck = { 
                        navController.navigate(Screen.RealityCheck.createRoute(decisionId))
                    }
                )
            }
        }
        
        composable(Screen.RealityCheck.route) { backStackEntry ->
            val decisionId = backStackEntry.arguments?.getString("decisionId")?.toLongOrNull()
            if (decisionId != null) {
                RealityCheckScreen(
                    decisionId = decisionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.Analytics.route) {
            InsightsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Insights.route) {
            InsightsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExport = { navController.navigate(Screen.Export.route) }
            )
        }
        
        composable(Screen.Export.route) {
            ExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroups = { navController.navigate(Screen.Groups.route) }
            )
        }
        
        composable(Screen.Groups.route) {
            GroupsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

