package com.scttech.android.kotlin.speedreader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scttech.android.kotlin.speedreader.ui.drills.DrillsRoute
import com.scttech.android.kotlin.speedreader.ui.drills.PacedDrillRoute
import com.scttech.android.kotlin.speedreader.ui.drills.SpanDrillRoute
import com.scttech.android.kotlin.speedreader.ui.baseline.BaselineRoute
import com.scttech.android.kotlin.speedreader.ui.home.HomeRoute
import com.scttech.android.kotlin.speedreader.ui.practice.PracticeRoute
import com.scttech.android.kotlin.speedreader.ui.progress.ProgressRoute
import com.scttech.android.kotlin.speedreader.ui.scan.ScanRoute
import com.scttech.android.kotlin.speedreader.ui.settings.SettingsRoute
import com.scttech.android.kotlin.speedreader.ui.tips.TipsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val BASELINE = "baseline"
    const val PRACTICE = "practice"
    const val DRILLS = "drills"
    const val SCAN = "scan"
    const val PROGRESS = "progress"
    const val TIPS = "tips"
    const val DRILL_SPAN = "drill_span"
    const val DRILL_PACED = "drill_paced/{kind}"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeRoute(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onStartBaseline = { navController.navigate(Routes.BASELINE) },
                onStartPractice = { navController.navigate(Routes.PRACTICE) },
                onStartDrills = { navController.navigate(Routes.DRILLS) },
                onStartScan = { navController.navigate(Routes.SCAN) },
                onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                onOpenTips = { navController.navigate(Routes.TIPS) },
            )
        }
        composable(Routes.BASELINE) {
            BaselineRoute(onExit = { navController.popBackStack() })
        }
        composable(Routes.SCAN) {
            ScanRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.TIPS) {
            TipsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROGRESS) {
            ProgressRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.DRILLS) {
            DrillsRoute(onBack = { navController.popBackStack() }, onOpen = { navController.navigate(it) })
        }
        composable(Routes.DRILL_SPAN) {
            SpanDrillRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.DRILL_PACED, arguments = listOf(navArgument("kind") { type = NavType.StringType })) {
            PacedDrillRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRACTICE) {
            PracticeRoute(onExit = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
