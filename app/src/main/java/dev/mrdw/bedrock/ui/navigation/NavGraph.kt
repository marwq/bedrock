package dev.mrdw.bedrock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import androidx.navigation.compose.composable
import dev.mrdw.bedrock.ui.screens.habits.HabitsScreen
import dev.mrdw.bedrock.ui.screens.habits.HabitEditorScreen
import dev.mrdw.bedrock.ui.screens.tasks.TasksScreen
import dev.mrdw.bedrock.ui.screens.tasks.TaskEditorScreen
import dev.mrdw.bedrock.ui.screens.reminders.RemindersScreen
import dev.mrdw.bedrock.ui.screens.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    startDestination: String = Screen.Habits.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Habits.route) {
            HabitsScreen(
                onNavigateToHabitEditor = { habitId ->
                    if (habitId != null) {
                        navController.navigate("${Screen.HabitEditor.route}/$habitId")
                    } else {
                        navController.navigate("${Screen.HabitEditor.route}/new")
                    }
                }
            )
        }

        composable(
            route = "${Screen.HabitEditor.route}/{habitId}?duplicateFrom={duplicateFrom}",
            arguments = listOf(
                navArgument("habitId") { type = NavType.StringType },
                navArgument("duplicateFrom") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            val duplicateFrom = backStackEntry.arguments?.getString("duplicateFrom")
            HabitEditorScreen(
                habitId = if (habitId == "new") null else habitId?.toLongOrNull(),
                duplicateFromId = duplicateFrom?.toLongOrNull(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDuplicate = { id ->
                    navController.navigate("${Screen.HabitEditor.route}/new?duplicateFrom=$id")
                }
            )
        }

        composable(Screen.Tasks.route) {
            TasksScreen(
                onNavigateToTaskEditor = { taskId ->
                    if (taskId != null) {
                        navController.navigate("${Screen.TaskEditor.route}/$taskId")
                    } else {
                        navController.navigate("${Screen.TaskEditor.route}/new")
                    }
                }
            )
        }

        composable(
            route = "${Screen.TaskEditor.route}/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskEditorScreen(
                taskId = if (taskId == "new") null else taskId?.toLongOrNull(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reminders.route) {
            RemindersScreen()
        }
    }
}
