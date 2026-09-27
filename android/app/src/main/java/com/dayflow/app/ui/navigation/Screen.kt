package com.dayflow.app.ui.navigation

sealed class Screen(val route: String) {
    object Tutorial : Screen("tutorial")
    object Today : Screen("today")
    object Tasks : Screen("tasks")
    object Reminders : Screen("reminders")
    object Intelligence : Screen("intelligence")
    object Settings : Screen("settings")
}
