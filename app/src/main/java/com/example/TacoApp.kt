package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AppStrings
import com.example.ui.EnStrings
import com.example.ui.LocalStrings
import com.example.ui.UkStrings
import com.example.ui.MainViewModel
import com.example.ui.screens.*

sealed class Screen(val route: String, val titleKey: (AppStrings) -> String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", { it.dashboard }, Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
    object Focus : Screen("focus", { it.focus }, Icons.Outlined.Timer, Icons.Filled.Timer)
    object Tasks : Screen("tasks", { it.tasks }, Icons.Outlined.Checklist, Icons.Filled.Checklist)
    object Notes : Screen("notes", { it.notes }, Icons.Outlined.Notes, Icons.Filled.Notes)
    object Profile : Screen("profile", { it.profile }, Icons.Outlined.Person, Icons.Filled.Person)
    object Stats : Screen("stats", { it.detailedStats }, Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
    object NoteEditor : Screen("note_editor", { "Editor" }, Icons.Outlined.Notes, Icons.Filled.Notes)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Focus,
    Screen.Tasks,
    Screen.Notes
)

@Composable
fun TacoApp(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val strings = if (lang == "uk") UkStrings else EnStrings

    CompositionLocalProvider(LocalStrings provides strings) {
        if (!isLoggedIn) {
            AuthScreen(onLogin = { name -> viewModel.login(name) })
        } else {
            val navController = rememberNavController()
            Scaffold(
                bottomBar = { TacoBottomNavigation(navController = navController) }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Dashboard.route) { DashboardScreen(viewModel, navController) }
                    composable(Screen.Focus.route) { FocusTimerScreen(viewModel, navController) }
                    composable(Screen.Tasks.route) { TasksScreen(viewModel, navController) }
                    composable(Screen.Notes.route) { NotesScreen(viewModel, navController) }
                    composable(
                        route = Screen.NoteEditor.route + "?noteId={noteId}",
                        arguments = listOf(androidx.navigation.navArgument("noteId") { type = androidx.navigation.NavType.StringType; nullable = true })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                        NoteEditorScreen(viewModel, navController, noteId)
                    }
                    composable(Screen.Profile.route) { ProfileScreen(viewModel, navController) }
                    composable(Screen.Stats.route) { StatsScreen(viewModel, navController) }
                }
            }
        }
    }
}

@Composable
fun TacoBottomNavigation(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavItems.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.icon,
                        contentDescription = screen.titleKey(LocalStrings.current)
                    )
                },
                label = { Text(screen.titleKey(LocalStrings.current), style = MaterialTheme.typography.labelMedium) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
