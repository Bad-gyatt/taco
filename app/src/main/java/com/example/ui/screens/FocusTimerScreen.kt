package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.ui.LocalStrings
import com.example.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(viewModel: com.example.ui.MainViewModel, navController: NavController) {
    val sessionCount by viewModel.focusSessionsData.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    
    var selectedDurationMinutes by androidx.compose.runtime.remember { mutableIntStateOf(25) }
    var timeLeft by androidx.compose.runtime.remember { mutableIntStateOf(25 * 60) }
    var isRunning by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val pendingTasks = tasks.filter { !it.isCompleted }
    var expandedTaskDropdown by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var selectedTaskForSession by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.data.Task?>(null) }
    var selectedSound by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("None") }

    val mediaPlayer = androidx.compose.runtime.remember { android.media.MediaPlayer() }

    androidx.compose.runtime.DisposableEffect(isRunning, selectedSound) {
        if (isRunning && selectedSound != "None") {
            try {
                mediaPlayer.reset()
                val url = when(selectedSound) {
                    "Rain" -> "https://dight310.byu.edu/media/audio/FreeLoops.com/1/1/Rain%20Drops.wav"
                    "Cafe" -> "https://dight310.byu.edu/media/audio/FreeLoops.com/2/2/Crowd%20Talking%204.wav"
                    "Fire" -> "https://dight310.byu.edu/media/audio/FreeLoops.com/3/3/Fire%20Burning.wav"
                    else -> ""
                }
                if (url.isNotEmpty()) {
                    mediaPlayer.setDataSource(url)
                    mediaPlayer.prepareAsync()
                    mediaPlayer.setOnPreparedListener { 
                        it.isLooping = true
                        it.start() 
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            } catch (e: Exception) {}
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while(timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isRunning = false
            viewModel.finishFocusSession(selectedDurationMinutes)
            selectedTaskForSession?.let { task ->
                if (!task.isCompleted) viewModel.toggleTask(task)
                selectedTaskForSession = null
            }
            timeLeft = selectedDurationMinutes * 60
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

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
                    /* Removed Notifications */
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(strings.timerMessage, style = Typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Timer Visual
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.3f), CircleShape)
                    .border(16.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = timeString,
                        style = Typography.displayLarge.copy(fontSize = 48.sp, lineHeight = 48.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            
            // Duration Selection if not running
            if (!isRunning) {
                // Task Select
                if (pendingTasks.isNotEmpty()) {
                    Box {
                        OutlinedButton(onClick = { expandedTaskDropdown = true }) {
                            Text(selectedTaskForSession?.title ?: "Select Task to Focus")
                        }
                        DropdownMenu(expanded = expandedTaskDropdown, onDismissRequest = { expandedTaskDropdown = false }) {
                            DropdownMenuItem(text = { Text("None") }, onClick = { selectedTaskForSession = null; expandedTaskDropdown = false })
                            pendingTasks.forEach { task ->
                                DropdownMenuItem(
                                    text = { Text(task.title) },
                                    onClick = { selectedTaskForSession = task; expandedTaskDropdown = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Sound Select
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val soundMap = mapOf(
                        "None" to strings.none,
                        "Rain" to strings.rain,
                        "Cafe" to strings.cafe,
                        "Fire" to strings.fire
                    )
                    listOf("None", "Rain", "Cafe", "Fire").forEach { sound ->
                        FilterChip(
                            selected = selectedSound == sound,
                            onClick = { selectedSound = sound },
                            label = { Text(soundMap[sound] ?: sound) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 15, 25, 45, 60).forEach { min ->
                        FilterChip(
                            selected = selectedDurationMinutes == min,
                            onClick = { 
                                selectedDurationMinutes = min
                                timeLeft = min * 60
                            },
                            label = { Text("$min ${strings.min}") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                if (selectedTaskForSession != null) {
                    Text("Focusing on: ${selectedTaskForSession?.title}", style = Typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
            
            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { 
                        isRunning = false
                        if (timeLeft < selectedDurationMinutes * 60) {
                            viewModel.finishFocusSession((selectedDurationMinutes * 60 - timeLeft) / 60)
                            timeLeft = selectedDurationMinutes * 60
                        }
                    },
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = strings.stop, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                IconButton(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    val icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow
                    Icon(icon, contentDescription = strings.start, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                }
                
                IconButton(
                    onClick = {
                        isRunning = false
                        timeLeft = selectedDurationMinutes * 60
                    },
                    modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = strings.reset, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Statistics Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.today, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$sessionCount ${strings.sessions}", style = Typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 12.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.nextGoal, style = Typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("15 ${strings.min}", style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
