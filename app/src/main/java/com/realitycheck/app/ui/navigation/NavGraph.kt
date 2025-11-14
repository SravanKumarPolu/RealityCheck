package com.realitycheck.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.realitycheck.app.ui.screens.AnalyticsScreen
import com.realitycheck.app.ui.screens.CreateDecisionScreen
import com.realitycheck.app.ui.screens.DecisionDetailScreen
import com.realitycheck.app.ui.screens.ExportScreen
import com.realitycheck.app.ui.screens.InsightsScreen
import com.realitycheck.app.ui.screens.MainScreen
import com.realitycheck.app.ui.screens.RealityCheckScreen
import com.realitycheck.app.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
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
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

