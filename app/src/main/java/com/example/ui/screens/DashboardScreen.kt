package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.navigation.NavController
import com.example.ui.LocalStrings
import com.example.Screen
import com.example.data.Task
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: com.example.ui.MainViewModel, navController: NavController) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val last7Stats by viewModel.last7DaysStats.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    
    val todayDateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val todayStat = last7Stats.find { it.date == todayDateString }
    var selectedMood by androidx.compose.runtime.remember(todayStat?.mood) { androidx.compose.runtime.mutableStateOf(todayStat?.mood ?: -1) }
    
    var showAddTaskDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (showAddTaskDialog) {
        com.example.ui.components.AddTaskDialog(
            onDismissRequest = { showAddTaskDialog = false },
            onTaskAdded = { title, desc, points, isFlexible, time ->
                viewModel.addTask(title, desc, points, isCritical = false, isFlexible = isFlexible, isShopping = false, scheduledTime = time)
                if (time != null && time.isNotBlank()) {
                    com.example.AlarmScheduler.scheduleTaskAlarm(context, title, time)
                }
            },
            strings = strings
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("TACO", style = Typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                actions = {
                    /* Removed Notification icon as requested */
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(strings.greeting(userName), style = Typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(strings.focusMessage, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Streak Card
                    Surface(modifier = Modifier.weight(1f).aspectRatio(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 2.dp) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                                if (selectedMood != -1) {
                                    val streakMoods = listOf(
                                        Icons.Default.SentimentDissatisfied,
                                        Icons.Default.SentimentNeutral,
                                        Icons.Default.SentimentSatisfied
                                    )
                                    Icon(streakMoods.getOrElse(selectedMood) { Icons.Default.SentimentSatisfied }, contentDescription = "Mood in Streak", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("0", style = Typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text(strings.streak, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    // Mood Card
                    Surface(modifier = Modifier.weight(1f).aspectRatio(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 2.dp) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(strings.mood, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val moods = listOf(
                                    Icons.Default.SentimentDissatisfied,
                                    Icons.Default.SentimentNeutral,
                                    Icons.Default.SentimentSatisfied
                                )
                                moods.forEachIndexed { index, icon ->
                                    val isSelected = index == selectedMood
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                            .clickable { 
                                                selectedMood = index
                                                viewModel.updateMood(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = "Mood", tint = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Productivity Chart
            item {
                Surface(modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.Stats.route) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.productivity, style = Typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text(strings.thisWeek, style = Typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        // Real Chart Data
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val cal = java.util.Calendar.getInstance()
                        val daysLabels = mutableListOf<String>()
                        val heightsList = mutableListOf<Float>()
                        
                        // Get max focus time to scale
                        val maxFocus = maxOf(last7Stats.maxOfOrNull { it.focusTimeMinutes } ?: 1, 60)
                        
                        for (i in 6 downTo 0) {
                            val c = cal.clone() as java.util.Calendar
                            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
                            val dStr = sdf.format(c.time)
                            val dayStat = last7Stats.find { it.date == dStr }
                            
                            val dayLabelFormatter = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                            daysLabels.add(dayLabelFormatter.format(c.time))
                            
                            val height = (dayStat?.focusTimeMinutes ?: 0).toFloat() / maxFocus.toFloat()
                            heightsList.add(height.coerceIn(0f, 1f))
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            heightsList.forEachIndexed { index, fl -> 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.width(32.dp).fillMaxHeight(fl)
                                        .background(if (index == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            daysLabels.forEachIndexed { index, day ->
                                Text(day, style = Typography.labelMedium, color = if (index == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            // Current Tasks
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.currentTasks, style = Typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { 
                                showAddTaskDialog = true
                            })
                        }
                        
                        val activeTasks = tasks.filter { !it.isCompleted }
                        if (activeTasks.isNotEmpty()) {
                            activeTasks.take(3).forEach { task ->
                                Spacer(modifier = Modifier.height(16.dp))
                                DashboardTaskItem(task.title, task.description, isCritical = task.isCritical, onComplete = { viewModel.toggleTask(task) })
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(strings.emptyDb, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun DashboardTaskItem(title: String, subtitle: String, isCritical: Boolean = false, onComplete: () -> Unit) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(20.dp).border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)).clickable { onComplete() })
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = Typography.bodyMedium, color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
