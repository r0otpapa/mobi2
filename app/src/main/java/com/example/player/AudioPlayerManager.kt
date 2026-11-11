package com.example.player

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentRecordingId: Long? = null,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0,
    val isPaused: Boolean = false
)

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(recordingId: Long, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        // If clicking same audio that is paused, resume it
        if (_playbackState.value.currentRecordingId == recordingId && _playbackState.value.isPaused) {
            mediaPlayer?.start()
            _playbackState.value = _playbackState.value.copy(isPlaying = true, isPaused = false)
            startProgressTracker()
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    stopProgressTracker()
                    _playbackState.value = PlaybackState()
                }
                setOnErrorListener { _, _, _ ->
                    stopProgressTracker()
                    _playbackState.value = PlaybackState()
                    true
                }
            }

            mediaPlayer = player
            player.start()

            _playbackState.value = PlaybackState(
                isPlaying = true,
                currentRecordingId = recordingId,
                currentPositionMs = 0,
                totalDurationMs = player.duration,
                isPaused = false
            )

            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
            _playbackState.value = PlaybackState()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    stopProgressTracker()
                    _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        stopProgressTracker()
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _playbackState.value = PlaybackState()
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _playbackState.value = _playbackState.value.copy(
                                currentPositionMs = player.currentPosition,
                                totalDurationMs = player.duration
                            )
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                delay(200L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
    }
}
