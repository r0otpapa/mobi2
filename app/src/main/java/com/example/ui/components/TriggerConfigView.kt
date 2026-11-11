package com.example.ui.components

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StealthNotificationStyle
import com.example.data.model.TriggerAction
import com.example.data.model.TriggerConfig
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
fun TriggerConfigView(
    config: TriggerConfig,
    currentShakeG: Float,
    isAccessibilityEnabled: Boolean,
    onUpdateFlip: (Boolean) -> Unit,
    onUpdateShake: (Boolean) -> Unit,
    onUpdateShakeSensitivity: (Float) -> Unit,
    onUpdateVolUp2x: (Boolean) -> Unit,
    onUpdateVolUp3x: (Boolean) -> Unit,
    onUpdateVolDown2x: (Boolean) -> Unit,
    onUpdateVolDown3x: (Boolean) -> Unit,
    onUpdateAction: (TriggerAction) -> Unit,
    onUpdateStealthNotif: (StealthNotificationStyle) -> Unit,
    onUpdateHaptic: (Boolean) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {

        Text(
            text = "TRIGGER CONFIGURATION",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = Color.White
            )
        )
        Text(
            text = "Customize gestures & hardware button sequence outputs",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate400
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: FLIP SENSOR
        TriggerSectionCard(
            title = "Flip Face-Down Sensor",
            icon = Icons.Default.ScreenRotation,
            enabled = config.flipEnabled,
            onToggle = onUpdateFlip,
            testTag = "flip_trigger_card"
        ) {
            Text(
                text = "Turn the phone face-down (or flip over on a desk/pocket) to immediately start silent background mic recording without lighting up the screen.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate400,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Requires keeping device face down for 400ms to avoid false triggers.",
                        fontSize = 11.sp,
                        color = Slate200
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: SHAKE SENSOR
        TriggerSectionCard(
            title = "Device Shake Sensor",
            icon = Icons.Default.Speed,
            enabled = config.shakeEnabled,
            onToggle = onUpdateShake,
            testTag = "shake_trigger_card"
        ) {
            Text(
                text = "Firmly shake your device to initiate discreet recording.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sensitivity slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shake Sensitivity",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = when {
                        config.shakeSensitivity <= 14f -> "High (Light Shake)"
                        config.shakeSensitivity <= 19f -> "Medium (Normal)"
                        else -> "Firm (Heavy Shake)"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Slider(
                value = config.shakeSensitivity,
                onValueChange = onUpdateShakeSensitivity,
                valueRange = 12f..26f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = CyanPrimary,
                    activeTrackColor = CyanPrimary,
                    inactiveTrackColor = Slate700
                ),
                modifier = Modifier.testTag("shake_sensitivity_slider")
            )

            // Live G-Meter indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Live Shake Force:",
                    fontSize = 11.sp,
                    color = Slate400
                )

                val progress = (currentShakeG / 30f).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(130.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (currentShakeG > config.shakeSensitivity) GreenActive else CyanPrimary,
                    trackColor = Slate700
                )

                Text(
                    text = String.format("%.1f G", currentShakeG),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentShakeG > config.shakeSensitivity) GreenActive else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: HARDWARE VOLUME BUTTONS
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                .testTag("volume_keys_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hardware Volume Buttons",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Screen locked / Screen off key press",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Accessibility Status Warning if not enabled
                if (!isAccessibilityEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF78350F).copy(alpha = 0.4f))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                            .clickable { onOpenAccessibilitySettings() }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Accessibility Permission Required",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFFDE68A)
                            )
                            Text(
                                text = "Android requires Accessibility Service to detect hardware volume keys while the screen is locked or app is closed. Tap here to enable it in Settings.",
                                fontSize = 11.sp,
                                color = Slate200,
                                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF59E0B))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "OPEN ACCESSIBILITY SETTINGS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate950
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Volume sequence switches
                VolumeKeyRow(
                    label = "Volume Up — 3x Rapid Press",
                    subtitle = "Recommended trigger for emergencies",
                    checked = config.volumeUp3xEnabled,
                    onCheckedChange = onUpdateVolUp3x
                )

                HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                VolumeKeyRow(
                    label = "Volume Down — 3x Rapid Press",
                    subtitle = "3 rapid taps on Volume Down button",
                    checked = config.volumeDown3xEnabled,
                    onCheckedChange = onUpdateVolDown3x
                )

                HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                VolumeKeyRow(
                    label = "Volume Up — 2x Double Press",
                    subtitle = "2 quick taps on Volume Up",
                    checked = config.volumeUp2xEnabled,
                    onCheckedChange = onUpdateVolUp2x
                )

                HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 8.dp))

                VolumeKeyRow(
                    label = "Volume Down — 2x Double Press",
                    subtitle = "2 quick taps on Volume Down",
                    checked = config.volumeDown2xEnabled,
                    onCheckedChange = onUpdateVolDown2x
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: CUSTOM OUTPUT ACTION
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OUTPUT EMERGENCY ACTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Slate400
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TriggerAction.values().forEach { action ->
                    val isSelected = config.selectedAction == action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Slate800 else Slate850)
                            .border(
                                1.dp,
                                if (isSelected) CyanPrimary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onUpdateAction(action) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isSelected) CyanPrimary else Slate600, CircleShape)
                                .background(if (isSelected) CyanPrimary else Color.Transparent)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = action.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = action.description,
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 5: STEALTH & NOTIFICATION DISGUISE
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STEALTH DISGUISE & BEHAVIOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Slate400
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Notification Disguise (on status bar):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate200
                )

                Spacer(modifier = Modifier.height(8.dp))

                StealthNotificationStyle.values().forEach { style ->
                    val isSelected = config.stealthNotification == style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Slate800 else Slate850)
                            .border(
                                1.dp,
                                if (isSelected) CyanPrimary else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onUpdateStealthNotif(style) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isSelected) CyanPrimary else Slate600, CircleShape)
                                .background(if (isSelected) CyanPrimary else Color.Transparent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = style.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "\"${style.title}\" — ${style.subtitle}",
                                fontSize = 10.sp,
                                color = Slate400
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 10.dp))

                // Pocket Haptic Confirmation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pocket Haptic Confirmation",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "2 subtle vibration pulses so you feel in pocket that recording has started",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Slate400)
                        )
                    }
                    Switch(
                        checked = config.hapticPulseOnStart,
                        onCheckedChange = onUpdateHaptic,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate950,
                            checkedTrackColor = CyanPrimary
                        )
                    )
                }

                HorizontalDivider(color = Slate800, modifier = Modifier.padding(vertical = 10.dp))

                // Max Duration
                Text(
                    text = "Auto-Stop Recording Duration:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate200
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 5, 10, 30).forEach { mins ->
                        val isSelected = config.maxRecordingDurationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanPrimary else Slate850)
                                .clickable { onUpdateDuration(mins) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Slate950 else Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TriggerSectionCard(
    title: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (enabled) CyanPrimary.copy(alpha = 0.15f) else Slate800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) CyanPrimary else Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Slate950,
                        checkedTrackColor = CyanPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun VolumeKeyRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Slate950,
                checkedTrackColor = CyanPrimary
            )
        )
    }
}
