package com.example.data.database

import androidx.room.*
import com.example.data.model.SavedJourneyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedJourneyDao {
    @Query("SELECT * FROM saved_journeys ORDER BY timestamp DESC")
    fun getAllSavedJourneys(): Flow<List<SavedJourneyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJourney(journey: SavedJourneyEntity)

    @Update
    suspend fun updateJourney(journey: SavedJourneyEntity)

    @Delete
    suspend fun deleteJourney(journey: SavedJourneyEntity)

    @Query("DELETE FROM saved_journeys WHERE fromStationId = :fromId AND toStationId = :toId")
    suspend fun deleteByRoute(fromId: String, toId: String)
}
