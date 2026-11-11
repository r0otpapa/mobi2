package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TriggerConfig
import com.example.service.RecorderState
import com.example.ui.MainUiState
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanSecondary
import com.example.ui.theme.GreenActive
import com.example.ui.theme.RedEmergency
import com.example.ui.theme.RedGlow
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.util.Locale

@Composable
fun DashboardView(
    config: TriggerConfig,
    recorderState: RecorderState,
    guardActive: Boolean,
    currentShakeG: Float,
    uiState: MainUiState,
    onToggleGuard: (Boolean) -> Unit,
    onTogglePanicRecord: () -> Unit,
    onOpenTestSimulator: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onRequestMicPermission: () -> Unit,
    onRequestNotifPermission: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecordings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SILENT GUARD",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "Stealth Emergency Trigger System",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate400
                    )
                )
            }

            // Quick live status chip
            val chipBg = if (recorderState.isRecording) RedEmergency.copy(alpha = 0.2f)
            else if (guardActive && config.isGuardEnabled) GreenActive.copy(alpha = 0.2f)
            else Slate800

            val chipBorder = if (recorderState.isRecording) RedEmergency
            else if (guardActive && config.isGuardEnabled) GreenActive
            else Slate700

            val chipText = if (recorderState.isRecording) "RECORDING"
            else if (guardActive && config.isGuardEnabled) "ARMED"
            else "STANDBY"

            val chipColor = if (recorderState.isRecording) RedEmergency
            else if (guardActive && config.isGuardEnabled) GreenActive
            else Slate400

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = chipText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = chipColor,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recording in progress banner
        AnimatedVisibility(visible = recorderState.isRecording) {
            RecordingActiveBanner(
                recorderState = recorderState,
                onStop = onTogglePanicRecord
            )
        }

        if (recorderState.isRecording) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Master Guard Switch Hero Card
        MasterGuardHeroCard(
            isGuardEnabled = config.isGuardEnabled,
            guardActive = guardActive,
            onToggle = onToggleGuard
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Discreet Panic Record Button
        DiscreetPanicButton(
            isRecording = recorderState.isRecording,
            onToggle = onTogglePanicRecord
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Active Hardware Triggers Summary Grid
        Text(
            text = "TRIGGER SENSORS STATUS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Slate400
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TriggerStatusBadge(
                icon = Icons.Default.ScreenRotation,
                title = "Flip Sensor",
                status = if (config.flipEnabled) "Armed" else "Off",
                isActive = config.flipEnabled && config.isGuardEnabled,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings
            )

            TriggerStatusBadge(
                icon = Icons.Default.Speed,
                title = "Shake Sensor",
                status = if (config.shakeEnabled) "Armed" else "Off",
                isActive = config.shakeEnabled && config.isGuardEnabled,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings
            )

            TriggerStatusBadge(
                icon = Icons.Default.VolumeUp,
                title = "Volume Keys",
                status = if (config.volumeUp3xEnabled || config.volumeDown3xEnabled) {
                    if (uiState.isAccessibilityEnabled) "Active" else "Setup Req"
                } else "Off",
                isActive = (config.volumeUp3xEnabled || config.volumeDown3xEnabled) && uiState.isAccessibilityEnabled,
                modifier = Modifier.weight(1f),
                onClick = if (!uiState.isAccessibilityEnabled) onOpenAccessibilitySettings else onNavigateToSettings
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Test Simulator Button
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                .clickable { onOpenTestSimulator() }
                .testTag("test_simulator_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Test",
                            tint = CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Test Trigger Simulator",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Test flip, shake & volume keys safely",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate400
                            )
                        )
                    }
                }

                Button(
                    onClick = onOpenTestSimulator,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding
                ) {
                    Text(
                        text = "TEST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Required Permissions Checklist Card
        PermissionsChecklistCard(
            uiState = uiState,
            onRequestMic = onRequestMicPermission,
            onRequestNotif = onRequestNotifPermission,
            onOpenAccessibility = onOpenAccessibilitySettings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MasterGuardHeroCard(
    isGuardEnabled: Boolean,
    guardActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val cardBorder = if (isGuardEnabled) CyanPrimary.copy(alpha = 0.5f) else Slate800
    val glowBrush = if (isGuardEnabled) {
        Brush.radialGradient(
            colors = listOf(CyanGlow, Color.Transparent),
            radius = 350f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, cardBorder, RoundedCornerShape(20.dp))
            .background(glowBrush)
            .testTag("master_guard_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isGuardEnabled) GreenActive else Slate600)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGuardEnabled) "BACKGROUND GUARD ACTIVE" else "GUARD IS OFF",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isGuardEnabled) GreenActive else Slate400,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Switch(
                    checked = isGuardEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Slate950,
                        checkedTrackColor = CyanPrimary,
                        uncheckedThumbColor = Slate400,
                        uncheckedTrackColor = Slate800
                    ),
                    modifier = Modifier.testTag("master_guard_switch")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Central Radar / Shield Visual
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(if (isGuardEnabled) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        if (isGuardEnabled) CyanPrimary.copy(alpha = 0.12f)
                        else Slate800
                    )
                    .border(
                        2.dp,
                        if (isGuardEnabled) CyanPrimary else Slate700,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Guard",
                    tint = if (isGuardEnabled) CyanPrimary else Slate600,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isGuardEnabled) "Device Guard Running" else "Emergency Sensors Paused",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = if (isGuardEnabled)
                    "Phone is actively listening for flip, shake & volume buttons even when screen is locked or in pocket."
                else
                    "Turn on guard to enable background emergency detection.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun DiscreetPanicButton(
    isRecording: Boolean,
    onToggle: () -> Unit
) {
    val bgBrush = if (isRecording) {
        Brush.horizontalGradient(listOf(RedEmergency, Color(0xFFB91C1C)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    }

    val borderCol = if (isRecording) RedEmergency else Slate700

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(1.5.dp, borderCol, RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .testTag("panic_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color.White.copy(alpha = 0.2f) else RedEmergency.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Panic Trigger",
                        tint = if (isRecording) Color.White else RedEmergency,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (isRecording) "STOP SILENT RECORDING" else "QUICK DISCREET RECORD",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    )
                    Text(
                        text = if (isRecording) "Tap to save & end emergency audio" else "One-tap manual stealth recording",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isRecording) Slate200 else Slate400
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isRecording) Color.White else RedEmergency)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isRecording) "STOP" else "PANIC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isRecording) RedEmergency else Color.White
                )
            }
        }
    }
}

@Composable
private fun RecordingActiveBanner(
    recorderState: RecorderState,
    onStop: () -> Unit
) {
    val durationFormatted = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        recorderState.durationSeconds / 60,
        recorderState.durationSeconds % 60
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = RedEmergency.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, RedEmergency, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(RedEmergency)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "EMERGENCY MIC RECORDING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = RedEmergency,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Duration: $durationFormatted • via ${recorderState.triggerSource}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = RedEmergency),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Stop", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TriggerStatusBadge(
    icon: ImageVector,
    title: String,
    status: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .border(
                1.dp,
                if (isActive) CyanPrimary.copy(alpha = 0.4f) else Slate800,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) CyanPrimary else Slate600,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                ),
                maxLines = 1
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = if (isActive) GreenActive else Slate400,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun PermissionsChecklistCard(
    uiState: MainUiState,
    onRequestMic: () -> Unit,
    onRequestNotif: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SYSTEM PERMISSIONS HUB",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Slate400
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            PermissionRow(
                title = "Microphone Access",
                subtitle = "Required for emergency audio capture",
                isGranted = uiState.isMicPermissionGranted,
                onClick = onRequestMic
            )

            Spacer(modifier = Modifier.height(10.dp))

            PermissionRow(
                title = "Notification Permission",
                subtitle = "Required for background stealth service",
                isGranted = uiState.isNotificationPermissionGranted,
                onClick = onRequestNotif
            )

            Spacer(modifier = Modifier.height(10.dp))

            PermissionRow(
                title = "Hardware Volume Key Service",
                subtitle = "Accessibility service for 2x/3x key sequence",
                isGranted = uiState.isAccessibilityEnabled,
                onClick = onOpenAccessibility
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate850)
            .clickable { if (!isGranted) onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
                    color = Slate400
                )
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = GreenActive,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyanPrimary)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ENABLE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate950
                )
            }
        }
    }
}
