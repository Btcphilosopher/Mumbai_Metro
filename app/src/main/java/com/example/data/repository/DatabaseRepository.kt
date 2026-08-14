package com.example.data.repository

import com.example.data.database.SavedJourneyDao
import com.example.data.database.TicketDao
import com.example.data.model.SavedJourneyEntity
import com.example.data.model.TicketEntity
import kotlinx.coroutines.flow.Flow

class DatabaseRepository(
    private val savedJourneyDao: SavedJourneyDao,
    private val ticketDao: TicketDao
) {
    val allSavedJourneys: Flow<List<SavedJourneyEntity>> = savedJourneyDao.getAllSavedJourneys()
    val allTickets: Flow<List<TicketEntity>> = ticketDao.getAllTickets()
    val activeTicket: Flow<TicketEntity?> = ticketDao.getActiveTicket()

    suspend fun saveJourney(fromStationId: String, toStationId: String) {
        savedJourneyDao.insertJourney(
            SavedJourneyEntity(
                fromStationId = fromStationId,
                toStationId = toStationId,
                isFavorite = false
            )
        )
    }

    suspend fun deleteSavedJourney(fromStationId: String, toStationId: String) {
        savedJourneyDao.deleteByRoute(fromStationId, toStationId)
    }

    suspend fun purchaseTicket(originStationId: String, destinationStationId: String, fare: Int, type: String) {
        val ticketId = "MM-${(100000..999999).random()}"
        val qrData = "MUMBAI_METRO_TICKET:$ticketId:$originStationId:$destinationStationId:$fare"
        val ticket = TicketEntity(
            id = ticketId,
            originStationId = originStationId,
            destinationStationId = destinationStationId,
            fare = fare,
            ticketType = type,
            qrData = qrData
        )
        ticketDao.insertTicket(ticket)
    }

    suspend fun markTicketAsUsed(ticketId: String) {
        ticketDao.markAsUsed(ticketId)
    }
}
