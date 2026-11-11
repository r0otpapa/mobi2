package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyRecording
import com.example.player.PlaybackState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedEmergency
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsVaultView(
    recordings: List<EmergencyRecording>,
    playbackState: PlaybackState,
    onPlay: (EmergencyRecording) -> Unit,
    onPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onStop: () -> Unit,
    onDelete: (EmergencyRecording) -> Unit,
    onUpdateNotes: (EmergencyRecording, String) -> Unit,
    onGetShareUri: (EmergencyRecording) -> Uri?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recordingToDelete by remember { mutableStateOf<EmergencyRecording?>(null) }
    var recordingToEdit by remember { mutableStateOf<EmergencyRecording?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RECORDINGS VAULT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "${recordings.size} emergency audio evidence files",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate850)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "SECURE LOCAL STORAGE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Slate850)
                            .border(1.dp, Slate700, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Recordings Captured Yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "When an emergency trigger (Flip, Shake, or Volume buttons) is fired, high-quality audio files will be silently captured and stored securely in this vault.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate400,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recordings, key = { it.id }) { recording ->
                    val isCurrentPlaying = playbackState.currentRecordingId == recording.id

                    RecordingItemCard(
                        recording = recording,
                        isPlaying = isCurrentPlaying && playbackState.isPlaying,
                        isCurrentTrack = isCurrentPlaying,
                        currentPositionMs = if (isCurrentPlaying) playbackState.currentPositionMs else 0,
                        totalDurationMs = if (isCurrentPlaying) playbackState.totalDurationMs else (recording.durationSeconds * 1000).toInt(),
                        onPlayClick = {
                            if (isCurrentPlaying && playbackState.isPlaying) {
                                onPause()
                            } else {
                                onPlay(recording)
                            }
                        },
                        onSeek = onSeek,
                        onShareClick = {
                            val uri = onGetShareUri(recording)
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "SilentGuard Emergency Recording")
                                    putExtra(Intent.EXTRA_TEXT, "Emergency recording captured via ${recording.triggerSource}")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Emergency Audio"))
                            }
                        },
                        onEditNotesClick = { recordingToEdit = recording },
                        onDeleteClick = { recordingToDelete = recording }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    recordingToDelete?.let { recording ->
        AlertDialog(
            onDismissRequest = { recordingToDelete = null },
            title = {
                Text(text = "Delete Emergency Audio?", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete this recording (${recording.fileName})? This action cannot be undone.",
                    color = Slate400
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(recording)
                        recordingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedEmergency)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordingToDelete = null }) {
                    Text("Cancel", color = Slate200)
                }
            },
            containerColor = Slate900,
            textContentColor = Slate200
        )
    }

    // Edit Notes Dialog
    recordingToEdit?.let { recording ->
        var noteText by remember { mutableStateOf(recording.notes) }

        AlertDialog(
            onDismissRequest = { recordingToEdit = null },
            title = {
                Text(text = "Edit Incident Notes", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    Text(
                        text = "Add context, names, or location details for this emergency file:",
                        fontSize = 12.sp,
                        color = Slate400,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Near metro station, stranger encounter...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Slate200
                        ),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNotes(recording, noteText)
                        recordingToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save Notes", color = Slate950, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordingToEdit = null }) {
                    Text("Cancel", color = Slate200)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
private fun RecordingItemCard(
    recording: EmergencyRecording,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    currentPositionMs: Int,
    totalDurationMs: Int,
    onPlayClick: () -> Unit,
    onSeek: (Int) -> Unit,
    onShareClick: () -> Unit,
    onEditNotesClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(recording.timestamp))

    val durationSeconds = recording.durationSeconds
    val durationFormatted = String.format(Locale.getDefault(), "%02d:%02d", durationSeconds / 60, durationSeconds % 60)
    val sizeMb = String.format(Locale.getDefault(), "%.1f MB", recording.fileSizeBytes / (1024f * 1024f))

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPlaying) CyanPrimary else Slate800,
                RoundedCornerShape(16.dp)
            )
            .testTag("recording_item_${recording.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Trigger Badge & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                recording.triggerSource.contains("Flip") -> CyanPrimary.copy(alpha = 0.15f)
                                recording.triggerSource.contains("Shake") -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                recording.triggerSource.contains("Volume") -> Color(0xFF10B981).copy(alpha = 0.15f)
                                else -> RedEmergency.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = recording.triggerSource,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            recording.triggerSource.contains("Flip") -> CyanPrimary
                            recording.triggerSource.contains("Shake") -> Color(0xFFF59E0B)
                            recording.triggerSource.contains("Volume") -> Color(0xFF10B981)
                            else -> RedEmergency
                        }
                    )
                }

                Text(
                    text = "$durationFormatted • $sizeMb",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate200
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )
            )

            if (recording.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "“${recording.notes}”",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyanPrimary,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate850)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = CyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                val curMs = if (isCurrentTrack) currentPositionMs else 0
                val totMs = if (isCurrentTrack && totalDurationMs > 0) totalDurationMs else (recording.durationSeconds * 1000).toInt().coerceAtLeast(1)

                val elapsedStr = String.format(Locale.getDefault(), "%02d:%02d", (curMs / 1000) / 60, (curMs / 1000) % 60)
                val totalStr = String.format(Locale.getDefault(), "%02d:%02d", (totMs / 1000) / 60, (totMs / 1000) % 60)

                Column(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = curMs.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..totMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary,
                            inactiveTrackColor = Slate700
                        ),
                        modifier = Modifier.height(20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = elapsedStr, fontSize = 9.sp, color = Slate400)
                        Text(text = totalStr, fontSize = 9.sp, color = Slate400)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEditNotesClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Notes",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = RedEmergency,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
