package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EmergencyRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM emergency_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<EmergencyRecording>>

    @Query("SELECT * FROM emergency_recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): EmergencyRecording?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: EmergencyRecording): Long

    @Update
    suspend fun updateRecording(recording: EmergencyRecording)

    @Delete
    suspend fun deleteRecording(recording: EmergencyRecording)

    @Query("DELETE FROM emergency_recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM emergency_recordings")
    fun getRecordingCount(): Flow<Int>
}
