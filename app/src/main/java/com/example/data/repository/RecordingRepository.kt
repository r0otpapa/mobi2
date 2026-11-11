package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import com.example.data.model.EmergencyRecording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class RecordingRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).recordingDao()

    val allRecordings: Flow<List<EmergencyRecording>> = dao.getAllRecordings()

    suspend fun saveRecording(recording: EmergencyRecording): Long = withContext(Dispatchers.IO) {
        dao.insertRecording(recording)
    }

    suspend fun updateRecording(recording: EmergencyRecording) = withContext(Dispatchers.IO) {
        dao.updateRecording(recording)
    }

    suspend fun deleteRecording(recording: EmergencyRecording) = withContext(Dispatchers.IO) {
        // Delete audio file on disk
        val file = File(recording.filePath)
        if (file.exists()) {
            file.delete()
        }
        dao.deleteRecording(recording)
    }

    fun getShareUri(recording: EmergencyRecording): Uri? {
        val file = File(recording.filePath)
        if (!file.exists()) return null
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getRecordingsDirectory(): File {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    companion object {
        @Volatile
        private var INSTANCE: RecordingRepository? = null

        fun getInstance(context: Context): RecordingRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RecordingRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
