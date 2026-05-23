package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.LocalStrings
import com.example.ui.MainViewModel
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MainViewModel, navController: NavController) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val sessionCount by viewModel.focusSessionsData.collectAsStateWithLifecycle()
    val last7Stats by viewModel.last7DaysStats.collectAsStateWithLifecycle()
    val strings = LocalStrings.current

    val completedTasksCount = tasks.count { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.detailedStats, style = Typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }
            
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            val dayLabelFormatter = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())

            for (i in 0..6) {
                val c = cal.clone() as java.util.Calendar
                c.add(java.util.Calendar.DAY_OF_YEAR, -i)
                val dStr = sdf.format(c.time)
                val dayStat = last7Stats.find { it.date == dStr }
                val dayLabel = if (i == 0) "Today" else if (i == 1) "Yesterday" else dayLabelFormatter.format(c.time)
                
                 item {
                     Surface(
                         modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                         shape = RoundedCornerShape(16.dp),
                         color = MaterialTheme.colorScheme.surfaceContainerLowest,
                         shadowElevation = 2.dp
                     ) {
                         Column(modifier = Modifier.padding(16.dp)) {
                             Text(dayLabel, style = Typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                             Spacer(modifier = Modifier.height(8.dp))
                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                 Column {
                                     Text("Час фокусу", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     Text("${dayStat?.focusTimeMinutes ?: 0} хв", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                 }
                                 Column {
                                     Text("Настрій", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     val moodStr = when (dayStat?.mood) {
                                         0 -> "🙁"
                                         1 -> "😐"
                                         2 -> "🙂"
                                         else -> "—"
                                     }
                                     Text(moodStr, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                 }
                                 Column {
                                     Text("Завдання", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     Text("${dayStat?.tasksCompleted ?: 0}", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                 }
                                 Column {
                                     Text("Бали", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     Text("${dayStat?.pointsEarned ?: 0}", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                 }
                             }
                         }
                     }
                 }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
