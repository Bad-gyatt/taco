package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderDialog(
    onDismissRequest: () -> Unit,
    onAudioSaved: (String) -> Unit
) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasRecordedFile by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        val file = File(context.cacheDir, "audio_note_${System.currentTimeMillis()}.3gp")
        outputFile = file
    }

    fun startRecording() {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        mediaRecorder = recorder
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder.setOutputFile(outputFile?.absolutePath)
            recorder.prepare()
            recorder.start()
            isRecording = true
            hasRecordedFile = false
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
            hasRecordedFile = true
        }
    }

    fun startPlaying() {
        if (outputFile?.exists() == true) {
            val player = MediaPlayer()
            mediaPlayer = player
            try {
                player.setDataSource(outputFile?.absolutePath)
                player.prepare()
                player.start()
                isPlaying = true
                player.setOnCompletionListener {
                    isPlaying = false
                    player.release()
                    mediaPlayer = null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                player.release()
                mediaPlayer = null
                isPlaying = false
            }
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaPlayer?.release()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (isRecording) stopRecording()
            if (isPlaying) stopPlaying()
            onDismissRequest()
        },
        title = { Text("Запис аудіо") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!hasPermission) {
                    Text("Потрібен дозвіл на мікрофон")
                } else {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                if (isRecording) stopRecording() else startRecording()
                            },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop" else "Record",
                                modifier = Modifier.size(48.dp),
                                tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        if (hasRecordedFile && !isRecording) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) stopPlaying() else startPlaying()
                                },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop playing" else "Play",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    if (isRecording) {
                        Text("Запис...", color = MaterialTheme.colorScheme.error)
                    } else if (hasRecordedFile) {
                        Text("Готово", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isRecording) stopRecording()
                    if (isPlaying) stopPlaying()
                    outputFile?.absolutePath?.let { path ->
                        onAudioSaved(path)
                    }
                    onDismissRequest()
                },
                enabled = hasRecordedFile && !isRecording
            ) {
                Text("Зберегти")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (isRecording) stopRecording()
                if (isPlaying) stopPlaying()
                onDismissRequest()
            }) {
                Text("Скасувати")
            }
        }
    )
}
