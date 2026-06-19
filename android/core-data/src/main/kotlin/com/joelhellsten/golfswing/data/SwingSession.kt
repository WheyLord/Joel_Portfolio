package com.joelhellsten.golfswing.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One recorded swing session with its fault analysis results.
 * The faultRecordsJson column stores the serialised List<FaultRecord> so
 * the schema stays simple while the record structure evolves freely.
 */
@Entity(tableName = "swing_sessions")
data class SwingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordedAt: Long = System.currentTimeMillis(),
    val videoPath: String,
    val cameraView: String,    // "DTL" or "FACE_ON"
    val clubCode: String = "7I",
    val faultRecordsJson: String = "[]",  // JSON-serialised List<FaultRecord>
    val backendResponseJson: String = "", // reasoning layer response, cached
)
