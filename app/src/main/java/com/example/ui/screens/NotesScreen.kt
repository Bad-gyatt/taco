package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.LocalStrings
import com.example.Screen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note
import com.example.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: com.example.ui.MainViewModel, navController: NavController) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    
    val context = LocalContext.current
    
    var cameraTempUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var cameraTempPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraTempPath != null) {
            viewModel.addNote(Note(title = strings.photo, body = "", tag = "Photo", hasImage = true, imageUri = cameraTempPath))
        }
    }
    
    var showAudioDialog by remember { mutableStateOf(false) }

    if (showAudioDialog) {
        com.example.ui.components.AudioRecorderDialog(
            onDismissRequest = { showAudioDialog = false },
            onAudioSaved = { path ->
                showAudioDialog = false
                viewModel.addNote(
                    Note(
                        title = strings.voiceMemo,
                        body = strings.recordedAudio,
                        tag = "Voice",
                        isVoiceMemo = true,
                        audioUri = path
                    )
                )
            }
        )
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.addNote(Note(title = strings.voiceMemo, body = strings.recordedAudio, tag = "Voice", isVoiceMemo = true, audioUri = uri.toString()))
            }
        }
    }

    var showAddNoteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
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
            // Expanded Quick Capture Bar in a Card
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.navigate(Screen.NoteEditor.route) }, modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)) {
                        Icon(Icons.Default.EditNote, contentDescription = "Text Note", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { 
                        showAudioDialog = true
                     }, modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Note", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { 
                        try {
                            val file = File(context.cacheDir, "photo_${java.util.UUID.randomUUID()}.jpg")
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            cameraTempPath = file.absolutePath
                            cameraTempUri = uri
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            viewModel.addNote(Note(title = strings.photo, body = "No camera found", tag = "Photo", hasImage = false))
                        }
                     }, modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera Note", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text(strings.searchNotes) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tags Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val tags = listOf(strings.allNotes to true, strings.work to false, strings.personal to false, strings.ideas to false, strings.journal to false)
                items(tags) { (tag, selected) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = tag,
                                style = Typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Masonry Grid Substitute (Staggered Column for simplicty here)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    notes.filterIndexed { index, _ -> index % 2 == 0 }.forEach { note ->
                        NoteCard(note, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                            navController.navigate(Screen.NoteEditor.route + "?noteId=${note.id}")
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    notes.filterIndexed { index, _ -> index % 2 != 0 }.forEach { note ->
                        NoteCard(note, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)) {
                            navController.navigate(Screen.NoteEditor.route + "?noteId=${note.id}")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun NoteCard(note: com.example.data.Note, tagColor: Color, tagBgColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (note.hasImage) {
                if (note.imageUri != null) {
                    coil.compose.AsyncImage(
                        model = note.imageUri,
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Text(note.title, style = Typography.headlineMedium.copy(fontSize = 16.sp, lineHeight = 22.sp), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Text(note.body, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 6, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = tagBgColor, shape = RoundedCornerShape(6.dp)) {
                Text(note.tag, style = Typography.labelMedium.copy(fontSize = 10.sp), color = tagColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun VoiceNoteCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voice Memo", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("\"Remember to ask Sarah about the Q3 budget...\"", style = Typography.bodyMedium.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 2)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(6.dp)) {
                Text("Ideas", style = Typography.labelMedium.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun GroceryListCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Grocery List", style = Typography.headlineMedium.copy(fontSize = 16.sp, lineHeight = 22.sp), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Almond milk", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Avocados", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckBox, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coffee beans", style = Typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough), color = MaterialTheme.colorScheme.outlineVariant)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp)) {
                Text("Personal", style = Typography.labelMedium.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
    }
}
