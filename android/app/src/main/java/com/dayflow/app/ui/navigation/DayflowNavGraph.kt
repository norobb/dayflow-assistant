package com.dayflow.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dayflow.app.DayflowApplication
import com.dayflow.app.ui.components.DayflowBottomBar
import com.dayflow.app.ui.components.OmniInputSheet
import com.dayflow.app.ui.intelligence.IntelligenceScreen
import com.dayflow.app.ui.reminders.RemindersScreen
import com.dayflow.app.ui.reminders.RemindersViewModel
import com.dayflow.app.ui.settings.SettingsScreen
import com.dayflow.app.ui.settings.SettingsViewModel
import com.dayflow.app.ui.today.TodayScreen
import com.dayflow.app.ui.today.TodayViewModel
import com.dayflow.app.ui.tasks.TasksScreen
import com.dayflow.app.ui.tasks.TasksViewModel

@Composable
fun DayflowApp(
    app: DayflowApplication,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

    var showInputSheet by remember { mutableStateOf(false) }

    // ViewModels with Application service locator
    val todayViewModel: TodayViewModel = viewModel(
        factory = TodayViewModel.Factory(
            app.repository,
            app.preferencesRepository,
            app.calendarService,
            app.notificationHelper,
            app.soundAndHaptics,
            app.analyzer
        )
    )

    val tasksViewModel: TasksViewModel = viewModel(
        factory = TasksViewModel.Factory(
            app.repository,
            app.soundAndHaptics
        )
    )

    val remindersViewModel: RemindersViewModel = viewModel(
        factory = RemindersViewModel.Factory(
            app.repository,
            app.notificationHelper,
            app.soundAndHaptics
        )
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            app.repository,
            app.preferencesRepository,
            app.soundAndHaptics
        )
    )

    val showBottomBar = currentRoute != Screen.Settings.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DayflowBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Today.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenInput = { showInputSheet = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Today.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Today.route) {
                    TodayScreen(
                        viewModel = todayViewModel,
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Tasks.route) {
                    TasksScreen(
                        viewModel = tasksViewModel,
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Reminders.route) {
                    RemindersScreen(
                        viewModel = remindersViewModel,
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Intelligence.route) {
                    IntelligenceScreen(
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            if (showInputSheet) {
                OmniInputSheet(
                    onDismiss = { showInputSheet = false },
                    onAnalyze = { type, text ->
                        showInputSheet = false
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Today.route) { inclusive = false }
                        }
                        todayViewModel.startAnalysis(type, text)
                    },
                    isAnalyzing = false
                )
            }
        }
    }
}
