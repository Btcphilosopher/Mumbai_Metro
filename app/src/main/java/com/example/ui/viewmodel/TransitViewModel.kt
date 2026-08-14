package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DatabaseRepository
import com.example.data.repository.TransitRepository
import com.example.localization.AppLanguage
import com.example.localization.Localizer
import com.example.voice.VoiceAssistant
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val databaseRepository = DatabaseRepository(
        database.savedJourneyDao(),
        database.ticketDao()
    )

    // --- Localization ---
    val currentLanguage = Localizer.currentLanguage

    // --- Persisted State (Flows from Room) ---
    val savedJourneys = databaseRepository.allSavedJourneys.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tickets = databaseRepository.allTickets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeTicket = databaseRepository.activeTicket.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // --- Live Network Streams ---
    val liveTrains = TransitRepository.liveTrains.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activeAlerts = TransitRepository.alerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- UI Local Inputs ---
    val searchQuery = MutableStateFlow("")
    val isStepFreeOnly = MutableStateFlow(false)

    val fromStation = MutableStateFlow<MetroStation?>(null)
    val toStation = MutableStateFlow<MetroStation?>(null)

    val plannedRoutes = MutableStateFlow<List<JourneyPlannerOption>>(emptyList())
    val selectedRouteOption = MutableStateFlow<JourneyPlannerOption?>(null)

    // --- Active Location Mock ---
    val currentGPS = MutableStateFlow(Pair(19.1176, 72.8562))
    val currentLocationName = MutableStateFlow("Andheri East, Saki Vihar Road")
    val nearestStation = MutableStateFlow<MetroStation?>(null)
    val nearestStationWalkMinutes = MutableStateFlow(8)
    val nearestStationDistanceM = MutableStateFlow(550)

    // --- Active Station Mode ---
    val selectedStation = MutableStateFlow<MetroStation?>(null)

    // --- Active Journey Navigation ---
    val isNavigating = MutableStateFlow(false)
    val activeNavigationStepIndex = MutableStateFlow(0)

    // --- Voice Assistant State ---
    val isVoiceAssistantOpen = MutableStateFlow(false)
    val voiceQuery = MutableStateFlow("")
    val voiceReply = MutableStateFlow("")

    // --- Navigation Screen Identifier ---
    val currentScreen = MutableStateFlow("home") // "home", "planner", "live", "ticket", "station", "alerts", "settings"

    init {
        // Automatically default nearest station to Saki Naka based on mock location
        nearestStation.value = TransitRepository.stations.find { it.id == "saki_naka" }
        selectedStation.value = TransitRepository.stations.find { it.id == "saki_naka" }
        
        // Default journey stations to facilitate quick user interactions
        fromStation.value = TransitRepository.stations.find { it.id == "andheri" }
        toStation.value = TransitRepository.stations.find { it.id == "ghatkopar" }
        triggerJourneyPlanning()
    }

    // --- Action Methods ---

    fun changeLanguage(language: AppLanguage) {
        viewModelScope.launch {
            Localizer.setLanguage(language)
        }
    }

    fun triggerJourneyPlanning() {
        val from = fromStation.value
        val to = toStation.value
        if (from != null && to != null) {
            var options = TransitRepository.planJourney(from.id, to.id)
            if (isStepFreeOnly.value) {
                options = options.filter { it.isStepFree }
            }
            plannedRoutes.value = options
            selectedRouteOption.value = options.firstOrNull()
        } else {
            plannedRoutes.value = emptyList()
            selectedRouteOption.value = null
        }
    }

    fun reverseJourney() {
        val temp = fromStation.value
        fromStation.value = toStation.value
        toStation.value = temp
        triggerJourneyPlanning()
    }

    fun startJourneyNavigation(option: JourneyPlannerOption) {
        selectedRouteOption.value = option
        isNavigating.value = true
        activeNavigationStepIndex.value = 0
        currentScreen.value = "planner"
    }

    fun stopJourneyNavigation() {
        isNavigating.value = false
        activeNavigationStepIndex.value = 0
    }

    fun advanceNavigationStep() {
        val option = selectedRouteOption.value ?: return
        if (activeNavigationStepIndex.value < option.steps.size - 1) {
            activeNavigationStepIndex.value += 1
        } else {
            stopJourneyNavigation()
        }
    }

    fun saveJourneyLocally(fromId: String, toId: String) {
        viewModelScope.launch {
            databaseRepository.saveJourney(fromId, toId)
        }
    }

    fun deleteSavedJourneyLocally(fromId: String, toId: String) {
        viewModelScope.launch {
            databaseRepository.deleteSavedJourney(fromId, toId)
        }
    }

    fun buyTicket(originId: String, destId: String, fare: Int, type: String) {
        viewModelScope.launch {
            databaseRepository.purchaseTicket(originId, destId, fare, type)
            currentScreen.value = "ticket"
        }
    }

    fun markActiveTicketAsUsed(ticketId: String) {
        viewModelScope.launch {
            databaseRepository.markTicketAsUsed(ticketId)
        }
    }

    fun executeVoiceQuery(query: String) {
        if (query.isBlank()) return
        voiceQuery.value = query
        viewModelScope.launch {
            // First run local parsing (very fast & offline)
            val result = VoiceAssistant.parseVoiceQueryLocal(query)
            
            if (result.isRouteQuery) {
                if (result.detectedSourceStation != null) {
                    fromStation.value = result.detectedSourceStation
                }
                if (result.detectedDestinationStation != null) {
                    toStation.value = result.detectedDestinationStation
                }
                triggerJourneyPlanning()
                
                voiceReply.value = result.replyText
                delaySimulatedAssistantClose()
            } else {
                // If local parsing is generic, try fallback to Gemini if API key available
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isNotEmpty() && !apiKey.startsWith("MY_GEMINI_API")) {
                    voiceReply.value = "Consulting Mumbai Metro Operations..."
                    val reply = VoiceAssistant.queryGemini(query, apiKey)
                    voiceReply.value = reply
                } else {
                    voiceReply.value = result.replyText
                }
            }
        }
    }

    private suspend fun delaySimulatedAssistantClose() {
        kotlinx.coroutines.delay(2000)
        isVoiceAssistantOpen.value = false
        currentScreen.value = "planner"
    }

    fun setLanguage(code: String) {
        val appLanguage = AppLanguage.fromCode(code)
        changeLanguage(appLanguage)
    }

    // Custom Mock Location Updater
    fun updateMockLocation(stationId: String, walkingMins: Int, distanceM: Int) {
        val station = TransitRepository.stations.find { it.id == stationId }
        if (station != null) {
            nearestStation.value = station
            selectedStation.value = station
            nearestStationWalkMinutes.value = walkingMins
            nearestStationDistanceM.value = distanceM
            currentLocationName.value = "${station.nameEn} Neighborhood"
        }
    }

    fun updateGPSLocation(lat: Double, lon: Double) {
        currentGPS.value = Pair(lat, lon)
        var minDistance = Double.MAX_VALUE
        var closestStation: MetroStation? = null
        for (st in TransitRepository.stations) {
            val dist = TransitRepository.calculateDistance(lat, lon, st.lat, st.lon)
            if (dist < minDistance) {
                minDistance = dist
                closestStation = st
            }
        }
        if (closestStation != null) {
            nearestStation.value = closestStation
            selectedStation.value = closestStation
            val walkTime = (minDistance * 12).toInt().coerceAtLeast(1)
            nearestStationWalkMinutes.value = walkTime
            nearestStationDistanceM.value = (minDistance * 1000).toInt()
            currentLocationName.value = "${closestStation.nameEn} (Simulated)"
        }
    }
}
