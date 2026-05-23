package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.Note
import com.example.ui.MainViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(viewModel: MainViewModel, navController: NavController, noteId: Int? = null) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var currentNote by remember { mutableStateOf<Note?>(null) }

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Work") }
    
    var isDrawingMode by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.DarkGray)
    
    // For drawing
    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    
    // For text styling
    var selectedFontFamily by remember { mutableStateOf(FontFamily.Default) }
    // Just simple pseudo font picking
    val fonts = listOf(FontFamily.Default, FontFamily.Serif, FontFamily.Monospace, FontFamily.Cursive)

    LaunchedEffect(noteId, notes) {
        if (noteId != null && currentNote == null) {
            val found = notes.find { it.id == noteId }
            if (found != null) {
                currentNote = found
                title = found.title
                body = found.body
                tag = found.tag
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId != null) "View / Edit Note" else "Create Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isDrawingMode = !isDrawingMode }) {
                        Icon(Icons.Default.Brush, contentDescription = "Draw", tint = if (isDrawingMode) MaterialTheme.colorScheme.primary else LocalContentColor.current)
                    }
                    IconButton(onClick = {
                        val noteToSave = currentNote?.copy(
                            title = title,
                            body = body,
                            tag = tag
                        ) ?: Note(title = title, body = body, tag = tag)
                        viewModel.addNote(noteToSave)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (currentNote?.hasImage == true && currentNote?.imageUri != null) {
                coil.compose.AsyncImage(
                    model = currentNote?.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (currentNote?.isVoiceMemo == true && currentNote?.audioUri != null) {
                var isPlaying by remember { mutableStateOf(false) }
                val mediaPlayer = remember { android.media.MediaPlayer() }
                
                DisposableEffect(Unit) {
                    onDispose {
                        if (isPlaying) {
                            try { mediaPlayer.stop() } catch(e:Exception){}
                        }
                        mediaPlayer.release()
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Голосова нотатка", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = {
                        if (isPlaying) {
                            try {
                                mediaPlayer.stop()
                            } catch (e: Exception) {}
                            isPlaying = false
                        } else {
                            try {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(currentNote!!.audioUri!!)
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlaying = true
                                mediaPlayer.setOnCompletionListener {
                                    isPlaying = false
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }) {
                        Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = "Play/Stop")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it },
                label = { Text("Tag") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Format toolbar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colors.forEach { color ->
                    Box(modifier = Modifier
                        .size(32.dp)
                        .background(color, CircleShape)
                        .border(2.dp, if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        .clickable { selectedColor = color }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Font family toggles pseudo
                fonts.forEachIndexed { index, font ->
                   FilterChip(
                        selected = selectedFontFamily == font,
                        onClick = { selectedFontFamily = font },
                        label = { Text("Font ${index + 1}", fontFamily = font) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isDrawingMode) {
                // Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPath = newPath
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                    // Trigger recomposition by re-assigning
                                    val temp = currentPath
                                    currentPath = null
                                    currentPath = temp
                                },
                                onDragEnd = {
                                    currentPath?.let { paths.add(Pair(it, selectedColor)) }
                                    currentPath = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        paths.forEach { (path, color) ->
                            drawPath(
                                path = path,
                                color = color,
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                        currentPath?.let { path ->
                            drawPath(
                                path = path,
                                color = selectedColor,
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            } else {
                // Text Area
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Note content") },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontFamily = selectedFontFamily, color = selectedColor)
                )
            }
        }
    }
}
