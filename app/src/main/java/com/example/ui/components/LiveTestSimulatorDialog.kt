package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TriggerConfig
import com.example.data.model.TriggerType
import com.example.ui.MainUiState
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenActive
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun LiveTestSimulatorDialog(
    config: TriggerConfig,
    uiState: MainUiState,
    currentShakeG: Float,
    onDismiss: () -> Unit,
    onSimulateTrigger: (TriggerType) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CyanPrimary, RoundedCornerShape(24.dp)),
            color = Slate900
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRIGGER TEST SIMULATOR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Safe sandbox: Test hardware gestures live",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Last trigger banner
                val lastTrigger = uiState.lastTriggerDetected
                if (lastTrigger != null) {
                    val isRecent = (System.currentTimeMillis() - uiState.lastTriggerTimestamp) < 3000L
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRecent) GreenActive.copy(alpha = 0.2f) else Slate850)
                            .border(
                                1.dp,
                                if (isRecent) GreenActive else Slate700,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isRecent) GreenActive else Slate400,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TRIGGER RECOGNIZED!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = if (isRecent) GreenActive else Slate200,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Gesture: ${lastTrigger.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Test card 1: Flip Gesture
                SimulatorTriggerRow(
                    title = "1. Flip Phone Face-Down",
                    subtitle = "Turn phone face down on desk or palm",
                    icon = Icons.Default.ScreenRotation,
                    isArmed = config.flipEnabled,
                    onSimulate = { onSimulateTrigger(TriggerType.FLIP) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test card 2: Shake Force
                SimulatorTriggerRow(
                    title = "2. Shake Device",
                    subtitle = "Live force: ${String.format("%.1f", currentShakeG)} G (Target: ${config.shakeSensitivity.toInt()} G)",
                    icon = Icons.Default.Speed,
                    isArmed = config.shakeEnabled,
                    onSimulate = { onSimulateTrigger(TriggerType.SHAKE) },
                    extraContent = {
                        val progress = (currentShakeG / 30f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (currentShakeG > config.shakeSensitivity) GreenActive else CyanPrimary,
                            trackColor = Slate700
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test card 3: Volume Up 3x
                SimulatorTriggerRow(
                    title = "3. Volume Up (3x Rapid Press)",
                    subtitle = if (uiState.isAccessibilityEnabled) "Press hardware volume up button 3 times" else "Requires Accessibility Permission",
                    icon = Icons.Default.VolumeUp,
                    isArmed = config.volumeUp3xEnabled && uiState.isAccessibilityEnabled,
                    onSimulate = { onSimulateTrigger(TriggerType.VOLUME_UP_3X) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test card 4: Volume Down 3x
                SimulatorTriggerRow(
                    title = "4. Volume Down (3x Rapid Press)",
                    subtitle = if (uiState.isAccessibilityEnabled) "Press hardware volume down button 3 times" else "Requires Accessibility Permission",
                    icon = Icons.Default.VolumeDown,
                    isArmed = config.volumeDown3xEnabled && uiState.isAccessibilityEnabled,
                    onSimulate = { onSimulateTrigger(TriggerType.VOLUME_DOWN_3X) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DONE TESTING",
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorTriggerRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isArmed: Boolean,
    onSimulate: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isArmed) CyanPrimary.copy(alpha = 0.15f) else Slate800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isArmed) CyanPrimary else Slate600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (isArmed) Slate400 else Color(0xFFF87171)
                            )
                        )
                    }
                }

                Button(
                    onClick = onSimulate,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text("Trigger", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                }
            }

            if (extraContent != null) {
                Spacer(modifier = Modifier.height(8.dp))
                extraContent()
            }
        }
    }
}
