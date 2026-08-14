package com.example.data.model

import com.example.localization.AppLanguage
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LineStatus {
    NORMAL,
    DELAYED,
    SUSPENDED
}

enum class CrowdingLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class LandmarkType {
    AIRPORT,
    RAILWAY_STATION,
    BUSINESS_DISTRICT,
    TOURIST_DESTINATION,
    SHOPPING_AREA,
    HOSPITAL,
    UNIVERSITY,
    STADIUM
}

data class MetroLine(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val colorHex: String,
    val status: LineStatus = LineStatus.NORMAL,
    val statusDetailKey: String = "connecting_live_feed"
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> nameEn
        AppLanguage.HINDI -> nameHi
        AppLanguage.MARATHI -> nameMr
    }
}

data class MetroStation(
    val id: String,
    val code: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val lineId: String,
    val lat: Double,
    val lon: Double,
    val hasLift: Boolean = true,
    val hasEscalator: Boolean = true,
    val stepFreeAccess: Boolean = true,
    val entrances: List<String> = emptyList(),
    val exits: List<String> = emptyList(),
    val facilities: List<String> = emptyList(),
    val nearbyBuses: List<String> = emptyList(),
    val landmarks: List<String> = emptyList()
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> nameEn
        AppLanguage.HINDI -> nameHi
        AppLanguage.MARATHI -> nameMr
    }
}

data class LiveTrain(
    val id: String,
    val lineId: String,
    val currentStationId: String,
    val nextStationId: String,
    val progress: Float, // 0.0f (at currentStation) to 1.0f (at nextStation)
    val speedKmh: Int,
    val crowding: CrowdingLevel,
    val delayMinutes: Int,
    val directionUp: Boolean // true: e.g. Versova -> Ghatkopar, false: Ghatkopar -> Versova
)

@Entity(tableName = "saved_journeys")
data class SavedJourneyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromStationId: String,
    val toStationId: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: String,
    val originStationId: String,
    val destinationStationId: String,
    val fare: Int,
    val ticketType: String, // "SINGLE", "RETURN", "PASS"
    val qrData: String,
    val isUsed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class TransitAlert(
    val id: String,
    val titleKey: String,
    val descriptionKey: String,
    val severity: String, // "INFO", "WARNING", "CRITICAL"
    val affectedLineId: String?,
    val timestamp: Long = System.currentTimeMillis()
)

data class Landmark(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val type: LandmarkType,
    val lat: Double,
    val lon: Double,
    val nearestStationId: String
) {
    fun localizedName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ENGLISH -> nameEn
        AppLanguage.HINDI -> nameHi
        AppLanguage.MARATHI -> nameMr
    }
}

data class JourneyRouteStep(
    val fromStationId: String,
    val toStationId: String,
    val lineId: String?, // null if walking/auto
    val stepType: String, // "METRO", "WALK", "AUTO", "BUS", "TAXI"
    val durationMinutes: Int,
    val distanceMetres: Int
)

data class JourneyPlannerOption(
    val id: String,
    val steps: List<JourneyRouteStep>,
    val totalDurationMinutes: Int,
    val totalWalkingMinutes: Int,
    val totalFare: Int,
    val interchangeCount: Int,
    val isStepFree: Boolean,
    val relativeCrowding: CrowdingLevel,
    val type: String // "FASTEST", "CHEAPEST", "FEWEST_CHANGES", "MOST_ACCESSIBLE"
)
