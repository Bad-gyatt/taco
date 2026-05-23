package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.LocalStrings
import com.example.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: com.example.ui.MainViewModel, navController: NavController) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    
    var showAddTaskDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var isCalendarView by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    // Hoist selected day to top for calendar filtering
    var selectedDayIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
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
                    /* Notifications removed */
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
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
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.scheduled, style = Typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                    
                    // Toggle
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (!isCalendarView) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { isCalendarView = false }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(strings.list, style = Typography.labelMedium, color = if (!isCalendarView) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                             modifier = Modifier
                                .background(if (isCalendarView) MaterialTheme.colorScheme.surface else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { isCalendarView = true }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(strings.calendar, style = Typography.labelMedium, color = if (isCalendarView) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            
            if (isCalendarView) {
                 item {
                     // Simple horizontal calendar mock for scheduled tasks
                     Row(
                         modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         val cal = java.util.Calendar.getInstance()
                         val currentLocale = java.util.Locale(if (strings.dashboard == "Головна") "uk" else "en")
                         val sdfDayName = java.text.SimpleDateFormat("EEE", currentLocale)
                         val sdfDayNum = java.text.SimpleDateFormat("d", currentLocale)

                         for (i in 0..6) {
                             val c = cal.clone() as java.util.Calendar
                             c.add(java.util.Calendar.DAY_OF_YEAR, i)
                             
                             val dayName = sdfDayName.format(c.time)
                             val dayNum = sdfDayNum.format(c.time)
                             
                             val isSelected = selectedDayIndex == i
                             Column(
                                 horizontalAlignment = Alignment.CenterHorizontally, 
                                 modifier = Modifier
                                     .clip(RoundedCornerShape(8.dp))
                                     .clickable { selectedDayIndex = i }
                                     .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                     .padding(8.dp)
                             ) {
                                 Text(dayName, style = Typography.labelSmall, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                                 Spacer(modifier = Modifier.height(4.dp))
                                 Text(dayNum, style = Typography.bodyLarge, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                             }
                         }
                     }
                 }
            }
            
            val displayedTasks = if (isCalendarView) {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, selectedDayIndex)
                val targetDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
                
                val myDay = when(cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                    java.util.Calendar.MONDAY -> 1
                    java.util.Calendar.TUESDAY -> 2
                    java.util.Calendar.WEDNESDAY -> 3
                    java.util.Calendar.THURSDAY -> 4
                    java.util.Calendar.FRIDAY -> 5
                    java.util.Calendar.SATURDAY -> 6
                    java.util.Calendar.SUNDAY -> 7
                    else -> 1
                }
                
                tasks.filter { task ->
                    val st = task.scheduledTime
                    if (st == null) false else {
                        st.contains(targetDateStr) || 
                        st.contains("|DAILY") ||
                        st.contains("|WEEKLY_$myDay")
                    }
                }
            } else {
                tasks
            }
            
            displayedTasks.forEach { task ->
                item {
                    TaskCard(
                        title = task.title,
                        desc = task.description,
                        isCritical = task.isCritical,
                        isFlexible = task.isFlexible,
                        time = task.scheduledTime,
                        isChecked = task.isCompleted,
                        onCheckedChange = { viewModel.toggleTask(task) }
                    )
                }
            }
            
            if (displayedTasks.isEmpty()) {
                 item {
                     Text(strings.emptyDb, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                 }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TaskCard(title: String, desc: String, isCritical: Boolean = false, isFlexible: Boolean = false, time: String? = null, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(20.dp).border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)).background(if (isChecked) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { onCheckedChange(!isChecked) },
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isCritical) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(6.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Critical", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                    if (isFlexible) {
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(6.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Flexible", style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                    if (time != null) {
                        val displayTime = time.replace("|DAILY", ", Every Day")
                            .replace("|WEEKLY_1", ", Every Monday")
                            .replace("|WEEKLY_2", ", Every Tuesday")
                            .replace("|WEEKLY_3", ", Every Wednesday")
                            .replace("|WEEKLY_4", ", Every Thursday")
                            .replace("|WEEKLY_5", ", Every Friday")
                            .replace("|WEEKLY_6", ", Every Saturday")
                            .replace("|WEEKLY_7", ", Every Sunday")
                            .trim()

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(displayTime, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// Removed ShoppingListCard mock

@Composable
fun ShoppingItem(name: String, checked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (checked) {
            Box(
                modifier = Modifier.size(20.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(name, style = Typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Box(modifier = Modifier.size(20.dp).border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Text(name, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
