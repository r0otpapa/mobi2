package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.DashboardView
import com.example.ui.components.LiveTestSimulatorDialog
import com.example.ui.components.RecordingsVaultView
import com.example.ui.components.TriggerConfigView
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RedEmergency
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissionsAndServices()
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val config by viewModel.config.collectAsStateWithLifecycle()
    val recorderState by viewModel.recorderState.collectAsStateWithLifecycle()
    val guardActive by viewModel.guardActive.collectAsStateWithLifecycle()
    val currentShakeG by viewModel.currentShakeG.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher for Microphone and Notification
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val micGranted = results[Manifest.permission.RECORD_AUDIO] ?: (
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            results[Manifest.permission.POST_NOTIFICATIONS] ?: (
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            )
        } else true

        viewModel.updatePermissionStatus(micGranted, notifGranted)
    }

    LaunchedEffect(Unit) {
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        viewModel.updatePermissionStatus(hasMic, hasNotif)

        if (!hasMic || !hasNotif) {
            val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    BackHandler(enabled = uiState.selectedTab != 0) {
        viewModel.setSelectedTab(0)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Slate950,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Slate800)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 0: Dashboard Guard
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == 0) Icons.Filled.Security else Icons.Outlined.Security,
                            contentDescription = "Guard Dashboard",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Guard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_guard")
                )

                // Tab 1: Trigger Config
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.selectedTab == 1) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Triggers Config",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Triggers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_triggers")
                )

                // Tab 2: Vault Recordings
                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        BadgedBox(badge = {
                            if (recordings.isNotEmpty()) {
                                Badge(
                                    containerColor = CyanPrimary,
                                    contentColor = Slate950
                                ) {
                                    Text("${recordings.size}")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (uiState.selectedTab == 2) Icons.Filled.Mic else Icons.Outlined.Mic,
                                contentDescription = "Recordings Vault",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = { Text("Vault", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Slate950,
                        selectedTextColor = CyanPrimary,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_vault")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(innerPadding)
        ) {
            Crossfade(targetState = uiState.selectedTab, label = "tabs") { tab ->
                when (tab) {
                    0 -> DashboardView(
                        config = config,
                        recorderState = recorderState,
                        guardActive = guardActive,
                        currentShakeG = currentShakeG,
                        uiState = uiState,
                        onToggleGuard = { viewModel.toggleMasterGuard(it) },
                        onTogglePanicRecord = { viewModel.togglePanicRecording() },
                        onOpenTestSimulator = { viewModel.setTestSimulatorOpen(true) },
                        onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() },
                        onRequestMicPermission = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                        },
                        onRequestNotifPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                            }
                        },
                        onNavigateToSettings = { viewModel.setSelectedTab(1) },
                        onNavigateToRecordings = { viewModel.setSelectedTab(2) }
                    )

                    1 -> TriggerConfigView(
                        config = config,
                        currentShakeG = currentShakeG,
                        isAccessibilityEnabled = uiState.isAccessibilityEnabled,
                        onUpdateFlip = { viewModel.updateFlipEnabled(it) },
                        onUpdateShake = { viewModel.updateShakeEnabled(it) },
                        onUpdateShakeSensitivity = { viewModel.updateShakeSensitivity(it) },
                        onUpdateVolUp2x = { viewModel.updateVolumeUp2x(it) },
                        onUpdateVolUp3x = { viewModel.updateVolumeUp3x(it) },
                        onUpdateVolDown2x = { viewModel.updateVolumeDown2x(it) },
                        onUpdateVolDown3x = { viewModel.updateVolumeDown3x(it) },
                        onUpdateAction = { viewModel.updateTriggerAction(it) },
                        onUpdateStealthNotif = { viewModel.updateStealthNotification(it) },
                        onUpdateHaptic = { viewModel.updateHapticFeedback(it) },
                        onUpdateDuration = { viewModel.updateMaxDuration(it) },
                        onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() }
                    )

                    2 -> RecordingsVaultView(
                        recordings = recordings,
                        playbackState = playbackState,
                        onPlay = { viewModel.playRecording(it) },
                        onPause = { viewModel.pausePlayback() },
                        onSeek = { viewModel.seekPlayback(it) },
                        onStop = { viewModel.stopPlayback() },
                        onDelete = { viewModel.deleteRecording(it) },
                        onUpdateNotes = { rec, note -> viewModel.updateRecordingNotes(rec, note) },
                        onGetShareUri = { viewModel.getShareUri(it) }
                    )
                }
            }

            // Live Test Simulator Dialog
            if (uiState.isTestSimulatorOpen) {
                LiveTestSimulatorDialog(
                    config = config,
                    uiState = uiState,
                    currentShakeG = currentShakeG,
                    onDismiss = { viewModel.setTestSimulatorOpen(false) },
                    onSimulateTrigger = { viewModel.testSimulateTrigger(it) }
                )
            }
        }
    }
}
