package com.example.data.database

import androidx.room.*
import com.example.data.model.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE isUsed = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getActiveTicket(): Flow<TicketEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("UPDATE tickets SET isUsed = 1 WHERE id = :ticketId")
    suspend fun markAsUsed(ticketId: String)
}
