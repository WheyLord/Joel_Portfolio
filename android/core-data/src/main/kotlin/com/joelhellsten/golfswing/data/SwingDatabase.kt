package com.joelhellsten.golfswing.data

import androidx.room.*

@Dao
interface SwingSessionDao {
    @Insert
    suspend fun insert(session: SwingSession): Long

    @Query("SELECT * FROM swing_sessions ORDER BY recordedAt DESC")
    suspend fun getAllSessions(): List<SwingSession>

    @Query("SELECT * FROM swing_sessions WHERE id = :id")
    suspend fun getSession(id: Long): SwingSession?

    @Update
    suspend fun update(session: SwingSession)
}

@Database(entities = [SwingSession::class], version = 1, exportSchema = false)
abstract class SwingDatabase : RoomDatabase() {
    abstract fun swingSessionDao(): SwingSessionDao

    companion object {
        @Volatile private var INSTANCE: SwingDatabase? = null

        fun getInstance(context: android.content.Context): SwingDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, SwingDatabase::class.java, "golf_swing.db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
