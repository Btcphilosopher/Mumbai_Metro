package com.example.data.repository

import com.example.data.model.*
import com.example.localization.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.*

object TransitRepository {

    // --- Dynamic Network State ---
    private val _liveTrains = MutableStateFlow<List<LiveTrain>>(emptyList())
    val liveTrains: Flow<List<LiveTrain>> = _liveTrains

    private val _alerts = MutableStateFlow<List<TransitAlert>>(emptyList())
    val alerts: Flow<List<TransitAlert>> = _alerts

    // --- Hardcoded (but extensible) seed data representing the real Mumbai Metro network ---
    val lines = listOf(
        MetroLine("line_1", "Line 1 (Blue)", "लाइन 1 (नीली)", "लाईन १ (निळी)", "#005A9C", LineStatus.DELAYED, "alert_line_1_delay"),
        MetroLine("line_2a", "Line 2A (Yellow)", "लाइन 2A (पीली)", "लाईन २A (पिवळी)", "#FFC72C", LineStatus.NORMAL),
        MetroLine("line_3", "Line 3 (Aqua Underground)", "लाइन 3 (एक्वा भूमिगत)", "लाईन ३ (अ‍ॅक्वा भूमिगत)", "#00A598", LineStatus.NORMAL, "alert_line_3_notice"),
        MetroLine("line_7", "Line 7 (Red)", "लाइन 7 (लाल)", "लाईन ७ (लाल)", "#D01C1F", LineStatus.NORMAL)
    )

    val stations = listOf(
        // --- Line 1 (Versova to Ghatkopar) ---
        MetroStation("vers", "VERS", "Versova", "वर्सोवा", "वर्सोवा", "line_1", 19.1355, 72.8142, true, true, true,
            entrances = listOf("Entrance A (Beach Side)", "Entrance B (Nana Nani Park)"),
            exits = listOf("Exit 1 (Versova Beach)", "Exit 2 (JP Road)"),
            facilities = listOf("Drinking Water", "Restroom", "First Aid", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 249", "Bus 251", "Bus A-256"),
            landmarks = listOf("Versova Beach", "Nana Nani Park", "Versova Metro Depot")),
        
        MetroStation("dn_nagar", "DNGR", "DN Nagar", "डी एन नगर", "डी. एन. नगर", "line_1", 19.1245, 72.8258, true, true, true,
            entrances = listOf("Entrance A (Link Road North)", "Entrance B (Link Road South)"),
            exits = listOf("Exit 1 (Juhu Circle)", "Exit 2 (Andheri West Market)"),
            facilities = listOf("Drinking Water", "Restroom", "Ticket Office", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 253", "Bus 254", "Bus A-112"),
            landmarks = listOf("DN Nagar Police Station", "Juhu Vile Parle Club", "Kokilaben Hospital")),

        MetroStation("azad_nagar", "AZAD", "Azad Nagar", "आजाद नगर", "आझाद नगर", "line_1", 19.1214, 72.8365, true, true, true,
            entrances = listOf("Entrance A (Veera Desai Road)"),
            exits = listOf("Exit 1 (Andheri Sports Complex)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts"),
            nearbyBuses = listOf("Bus 262", "Bus 268"),
            landmarks = listOf("Andheri Sports Complex", "Veera Desai Industrial Area")),

        MetroStation("andheri", "ANDH", "Andheri Metro", "अंधेरी मेट्रो", "अंधेरी मेट्रो", "line_1", 19.1198, 72.8464, true, true, true,
            entrances = listOf("Entrance A (Railway Station Connect)", "Entrance B (Gokhale Bridge East)"),
            exits = listOf("Exit 1 (Andheri Suburban Platform 1)", "Exit 2 (SV Road)"),
            facilities = listOf("Drinking Water", "Restroom", "Customer Service", "ATM", "Wheelchairs", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 339", "Bus 340", "Bus A-411"),
            landmarks = listOf("Andheri Railway Station (Western Line)", "SV Road", "Gokhale Bridge")),

        MetroStation("weh", "WEH", "Western Express Highway", "पश्चिमी एक्सप्रेस हाईवे", "पश्चिम द्रुतगती महामार्ग", "line_1", 19.1158, 72.8564, true, true, true,
            entrances = listOf("Entrance A (WEH Flyover North)", "Entrance B (WEH South)"),
            exits = listOf("Exit 1 (Gundavali Foot Over Bridge)", "Exit 2 (Highway Junction)"),
            facilities = listOf("Drinking Water", "Restroom", "Parking", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 441", "Bus 442", "Bus A-122"),
            landmarks = listOf("Western Express Highway", "Gundavali Metro Station Connect (Line 7)")),

        MetroStation("marol_naka", "MARL", "Marol Naka", "मरोल नाका", "मरोल नाका", "line_1", 19.1118, 72.8715, true, true, true,
            entrances = listOf("Entrance A (Andheri-Kurla Road)", "Entrance B (Line 3 Connector)"),
            exits = listOf("Exit 1 (Marol Pipeline)", "Exit 2 (Terminal 2 Way)"),
            facilities = listOf("Drinking Water", "Restroom", "Underground Link", "ATM", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 186", "Bus 321", "Bus A-334"),
            landmarks = listOf("Marol Pipeline", "CSMIA International Airport Terminal 2", "Seven Hills Hospital")),

        MetroStation("saki_naka", "SAKI", "Saki Naka", "साकी नाका", "साकी नाका", "line_1", 19.1028, 72.8875, true, true, true,
            entrances = listOf("Entrance A (Saki Vihar Road)", "Entrance B (90 Feet Road)"),
            exits = listOf("Exit 1 (Saki Naka Junction)", "Exit 2 (LBS Road)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 319", "Bus 320", "Bus A-415"),
            landmarks = listOf("Saki Naka Junction", "LBS Marg", "Saki Vihar Industrial Estates")),

        MetroStation("ghatkopar", "GHAT", "Ghatkopar", "घाटकोपर", "घाटकोपर", "line_1", 19.0864, 72.9082, true, true, true,
            entrances = listOf("Entrance A (Central Railway Connect)", "Entrance B (LBS Marg West)"),
            exits = listOf("Exit 1 (Ghatkopar Central Station Platform 1)", "Exit 2 (Ghatkopar East Market)"),
            facilities = listOf("Drinking Water", "Restroom", "ATM", "Customer Service Desk", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 385", "Bus 399", "Bus A-381"),
            landmarks = listOf("Ghatkopar Railway Station (Central Line)", "LBS Road", "Ramabai Nagar")),

        // --- Line 2A (Dahisar East to DN Nagar) ---
        MetroStation("dahisar_e", "DHSR", "Dahisar East", "दहिसर पूर्व", "दहिसर पूर्व", "line_2a", 19.2554, 72.8624, true, true, true,
            entrances = listOf("Entrance A (WEH North Side)"),
            exits = listOf("Exit 1 (Dahisar Check Naka)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts"),
            nearbyBuses = listOf("Bus 701", "Bus 702"),
            landmarks = listOf("Dahisar Check Naka", "Western Express Highway")),

        MetroStation("anand_nagar", "ANAND", "Anand Nagar", "आनंद नगर", "आनंद नगर", "line_2a", 19.2454, 72.8574, true, true, true,
            entrances = listOf("Entrance A (Link Road)"),
            exits = listOf("Exit 1 (Anand Nagar Market)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts"),
            nearbyBuses = listOf("Bus 703"),
            landmarks = listOf("Anand Nagar Playground")),

        MetroStation("kandivali_w", "KNDW", "Kandivali West", "कांदिवली पश्चिम", "कांदिवली पश्चिम", "line_2a", 19.2154, 72.8424, true, true, true,
            entrances = listOf("Entrance A (Link Road Central)"),
            exits = listOf("Exit 1 (Kandivali West Station Road)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 204", "Bus 205"),
            landmarks = listOf("Link Road Market", "Poisar Depot")),

        MetroStation("malad_w", "MLDW", "Malad West", "मालाड पश्चिम", "मालाड पश्चिम", "line_2a", 19.1954, 72.8374, true, true, true,
            entrances = listOf("Entrance A (Inorbit Mall Road)"),
            exits = listOf("Exit 1 (Inorbit Mall Side)", "Exit 2 (Mindspace)"),
            facilities = listOf("Drinking Water", "Restroom", "Parking", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 210", "Bus 224"),
            landmarks = listOf("Inorbit Mall Malad", "Mindspace Business District", "Infiniti Mall")),

        // --- Line 3 (Aqua Underground: SEEPZ to Cuffe Parade) ---
        MetroStation("aarey", "AARY", "Aarey Colony", "आरे कॉलोनी", "आरे कॉलनी", "line_3", 19.1415, 72.8745, true, true, true,
            entrances = listOf("Entrance A (Aarey Picnic Point)"),
            exits = listOf("Exit 1 (Aarey Depot)"),
            facilities = listOf("Drinking Water", "Restroom", "Bicycle Parking", "Lifts"),
            nearbyBuses = listOf("Bus 451", "Bus 452"),
            landmarks = listOf("Aarey Milk Colony", "Sanjay Gandhi National Park", "Film City")),

        MetroStation("seepz", "SEEZ", "SEEPZ", "सीप्ज़", "सीप्झ", "line_3", 19.1298, 72.8785, true, true, true,
            entrances = listOf("Entrance A (SEEPZ Gate 1)", "Entrance B (SEEPZ Gate 3)"),
            exits = listOf("Exit 1 (SEEPZ SEZ)", "Exit 2 (MIDC Central)"),
            facilities = listOf("Drinking Water", "Restroom", "ATM", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 430", "Bus 431"),
            landmarks = listOf("SEEPZ Special Economic Zone", "MIDC Industrial Estate")),

        MetroStation("csmia_t2", "BOM2", "CSMIA Terminal 2", "छत्रपति शिवाजी महाराज एयरपोर्ट टी2", "छत्रपती शिवाजी महाराज विमानतळ टी२", "line_3", 19.0912, 72.8682, true, true, true,
            entrances = listOf("Entrance A (International Arrivals Connect)", "Entrance B (Multi-Level Parking Connect)"),
            exits = listOf("Exit 1 (T2 Flight Departures Gate 4)", "Exit 2 (Sahar Road)"),
            facilities = listOf("Drinking Water", "Restroom", "Underground Walkway", "Lifts", "Escalators", "VIP Lounge", "Luggage Services"),
            nearbyBuses = listOf("Airport Coach", "Bus 321"),
            landmarks = listOf("Mumbai International Airport (T2)", "JW Marriott Sahar", "Sahar Cargo Complex")),

        MetroStation("csmia_t1", "BOM1", "CSMIA Terminal 1", "छत्रपति शिवाजी महाराज एयरपोर्ट टी1", "छत्रपती शिवाजी महाराज विमानतळ टी१", "line_3", 19.0984, 72.8515, true, true, true,
            entrances = listOf("Entrance A (Domestic Terminal T1)"),
            exits = listOf("Exit 1 (Domestic Departures Gates)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts", "Escalators", "Luggage Wrapping"),
            nearbyBuses = listOf("Airport Shuttle", "Bus 312"),
            landmarks = listOf("Mumbai Domestic Airport (T1)", "Vile Parle East", "Western Express Highway")),

        MetroStation("bkc", "BKC", "Bandra Kurla Complex", "बांद्रा कुर्ला कॉम्प्लेक्स", "वांद्रे कुर्ला कॉम्प्लेक्स", "line_3", 19.0624, 72.8638, true, true, true,
            entrances = listOf("Entrance A (G Block Financial)", "Entrance B (Jio World Centre Connect)"),
            exits = listOf("Exit 1 (Jio World Plaza)", "Exit 2 (US Consulate)", "Exit 3 (ICICI Bank Towers)"),
            facilities = listOf("Drinking Water", "Restroom", "Bicycle Parking", "ATM", "Customer Service Suite", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus BKC-1", "Bus BKC-2", "Bus A-310"),
            landmarks = listOf("Bandra Kurla Complex (BKC)", "Jio World Convention Centre", "US Consulate General", "NSE (National Stock Exchange)", "Jio World Plaza")),

        MetroStation("dadar_l3", "DDR3", "Dadar Line 3", "दादर लाइन 3", "दादर लाईन ३", "line_3", 19.0178, 72.8428, true, true, true,
            entrances = listOf("Entrance A (Shivaji Park Road)", "Entrance B (Dadar Plaza)"),
            exits = listOf("Exit 1 (Shivaji Park Beach)", "Exit 2 (Dadar Central Station Connector)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 1", "Bus 2", "Bus A-101"),
            landmarks = listOf("Shivaji Park", "Siddhivinayak Temple", "Dadar Central & Western Railway Interchange")),

        MetroStation("cuffe", "CUFF", "Cuffe Parade", "कफ परेड", "कफ परेड", "line_3", 18.9142, 72.8124, true, true, true,
            entrances = listOf("Entrance A (World Trade Centre Side)", "Entrance B (Cuffe Parade Park)"),
            exits = listOf("Exit 1 (World Trade Centre)", "Exit 2 (Badhwar Park)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 138", "Bus A-100"),
            landmarks = listOf("World Trade Centre Mumbai", "Gateway of India (2.2km Taxi/Bus Connect)", "Colaba Causeway", "Taj Mahal Palace Hotel")),

        // --- Line 7 (Dahisar East to Gundavali) ---
        MetroStation("akruli", "AKRL", "Akruli", "आकुर्ली", "आकुर्ली", "line_7", 19.2064, 72.8614, true, true, true,
            entrances = listOf("Entrance A (Kandivali East Highway)"),
            exits = listOf("Exit 1 (Growel's 101 Mall)"),
            facilities = listOf("Drinking Water", "Restroom", "Lifts"),
            nearbyBuses = listOf("Bus 461"),
            landmarks = listOf("Growel's 101 Mall Kandivali", "Western Express Highway")),

        MetroStation("gundavali", "GNDV", "Gundavali", "गुंदवली", "गुंडवली", "line_7", 19.1172, 72.8572, true, true, true,
            entrances = listOf("Entrance A (WEH Flyover Connector)", "Entrance B (Andheri East Flyover)"),
            exits = listOf("Exit 1 (WEH Interchange to Line 1)", "Exit 2 (Andheri Kurla Highway Jct)"),
            facilities = listOf("Drinking Water", "Restroom", "ATM", "Lifts", "Escalators"),
            nearbyBuses = listOf("Bus 440", "Bus A-111"),
            landmarks = listOf("Gundavali Bus Junction", "WEH Interchange (Connects to Line 1 Western Express Highway station)"))
    )

    val landmarks = listOf(
        Landmark("csmia_t2_airport", "CSMIA Int'l Airport (Terminal 2)", "छत्रपति शिवाजी महाराज अंतर्राष्ट्रीय हवाई अड्डा (टी2)", "छत्रपती शिवाजी महाराज आंतरराष्ट्रीय विमानतळ (टी२)", LandmarkType.AIRPORT, 19.0912, 72.8682, "csmia_t2"),
        Landmark("csmia_t1_airport", "CSMIA Domestic Airport (Terminal 1)", "छत्रपति शिवाजी महाराज घरेलू हवाई अड्डा (टी1)", "छत्रपती शिवाजी महाराज देशांतर्गत विमानतळ (टी१)", LandmarkType.AIRPORT, 19.0984, 72.8515, "csmia_t1"),
        Landmark("gateway_of_india", "Gateway of India", "गेटवे ऑफ इंडिया", "गेटवे ऑफ इंडिया", LandmarkType.TOURIST_DESTINATION, 18.9220, 72.8347, "cuffe"),
        Landmark("bkc_business", "Bandra Kurla Complex (BKC)", "बांद्रा कुर्ला कॉम्प्लेक्स (BKC)", "वांद्रे कुर्ला कॉम्प्लेक्स (BKC)", LandmarkType.BUSINESS_DISTRICT, 19.0624, 72.8638, "bkc"),
        Landmark("csmt_station", "CSMT Terminus", "छत्रपति शिवाजी महाराज टर्मिनस (CSMT)", "छत्रपती शिवाजी महाराज टर्मिनस (CSMT)", LandmarkType.RAILWAY_STATION, 18.9400, 72.8353, "cuffe"),
        Landmark("dadar_market", "Dadar Flower Market & Shivaji Park", "दादर फ्लावर मार्केट और शिवाजी पार्क", "दादर फुल बाजार आणि शिवाजी पार्क", LandmarkType.TOURIST_DESTINATION, 19.0178, 72.8428, "dadar_l3"),
        Landmark("juhu_beach", "Juhu Beach", "जुहू बीच", "जुहू चौपाटी", LandmarkType.TOURIST_DESTINATION, 19.1028, 72.8258, "dn_nagar"),
        Landmark("seven_hills", "Seven Hills Hospital", "सेवन हिल्स अस्पताल", "सेव्हन हिल्स हॉस्पिटल", LandmarkType.HOSPITAL, 19.1118, 72.8715, "marol_naka"),
        Landmark("growels_mall", "Growel's 101 Mall", "ग्रोवेल्स 101 मॉल", "ग्रोवेल्स १०१ मॉल", LandmarkType.SHOPPING_AREA, 19.2064, 72.8614, "akruli"),
        Landmark("mumbai_uni", "Mumbai University (Kalina)", "मुंबई विश्वविद्यालय (कलीना)", "मुंबई विद्यापीठ (कलिना)", LandmarkType.UNIVERSITY, 19.0710, 72.8610, "bkc")
    )

    init {
        // Populate static service alerts
        _alerts.value = listOf(
            TransitAlert("alert_1", "alert_line_1_delay", "alert_line_1_delay", "WARNING", "line_1"),
            TransitAlert("alert_2", "alert_line_3_notice", "alert_line_3_notice", "INFO", "line_3")
        )

        // Generate initial live simulated trains
        _liveTrains.value = generateSimulatedTrains()

        // Start active simulator to move trains and change positions reactively
        startTrainSimulator()
    }

    private fun generateSimulatedTrains(): List<LiveTrain> {
        val trains = mutableListOf<LiveTrain>()
        // Line 1 Trains
        trains.add(LiveTrain("train_101", "line_1", "vers", "dn_nagar", 0.4f, 45, CrowdingLevel.MEDIUM, 0, true))
        trains.add(LiveTrain("train_102", "line_1", "marol_naka", "saki_naka", 0.8f, 50, CrowdingLevel.HIGH, 2, true))
        trains.add(LiveTrain("train_103", "line_1", "weh", "andheri", 0.1f, 35, CrowdingLevel.LOW, 0, false))
        trains.add(LiveTrain("train_104", "line_1", "ghatkopar", "saki_naka", 0.6f, 40, CrowdingLevel.MEDIUM, 10, false))

        // Line 2A Trains
        trains.add(LiveTrain("train_201", "line_2a", "dahisar_e", "anand_nagar", 0.5f, 48, CrowdingLevel.LOW, 0, true))
        trains.add(LiveTrain("train_202", "line_2a", "malad_w", "dn_nagar", 0.2f, 52, CrowdingLevel.MEDIUM, 0, true))

        // Line 3 Trains
        trains.add(LiveTrain("train_301", "line_3", "aarey", "seepz", 0.3f, 60, CrowdingLevel.HIGH, 0, true))
        trains.add(LiveTrain("train_302", "line_3", "csmia_t2", "csmia_t1", 0.7f, 65, CrowdingLevel.MEDIUM, 0, true))
        trains.add(LiveTrain("train_303", "line_3", "bkc", "dadar_l3", 0.5f, 70, CrowdingLevel.LOW, 0, false))
        trains.add(LiveTrain("train_304", "line_3", "cuffe", "dadar_l3", 0.1f, 55, CrowdingLevel.HIGH, 0, true))

        // Line 7 Trains
        trains.add(LiveTrain("train_701", "line_7", "dahisar_e", "akruli", 0.6f, 50, CrowdingLevel.MEDIUM, 0, true))
        trains.add(LiveTrain("train_702", "line_7", "gundavali", "akruli", 0.3f, 45, CrowdingLevel.LOW, 0, false))

        return trains
    }

    private fun startTrainSimulator() {
        // Run simulator as background daemon
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            while (true) {
                delay(3000) // update positions every 3 seconds
                val current = _liveTrains.value
                val updated = current.map { train ->
                    var nextProgress = train.progress + 0.1f
                    var nextCurrentId = train.currentStationId
                    var nextNextId = train.nextStationId
                    if (nextProgress >= 1.0f) {
                        nextProgress = 0.0f
                        // Cycle to next station on line
                        val stationsOnLine = stations.filter { it.lineId == train.lineId }
                        val currIndex = stationsOnLine.indexOfFirst { it.id == train.nextStationId }
                        if (currIndex == -1 || currIndex == stationsOnLine.size - 1 && train.directionUp || currIndex == 0 && !train.directionUp) {
                            // Reverse direction
                            val nextDir = !train.directionUp
                            nextCurrentId = train.nextStationId
                            nextNextId = if (nextDir) {
                                stationsOnLine.getOrNull(currIndex - 1)?.id ?: stationsOnLine.first().id
                            } else {
                                stationsOnLine.getOrNull(currIndex + 1)?.id ?: stationsOnLine.last().id
                            }
                            train.copy(
                                currentStationId = nextCurrentId,
                                nextStationId = nextNextId,
                                progress = nextProgress,
                                directionUp = nextDir,
                                speedKmh = (30..75).random(),
                                crowding = CrowdingLevel.values().random()
                            )
                        } else {
                            nextCurrentId = train.nextStationId
                            nextNextId = if (train.directionUp) {
                                stationsOnLine[currIndex + 1].id
                            } else {
                                stationsOnLine[currIndex - 1].id
                            }
                            train.copy(
                                currentStationId = nextCurrentId,
                                nextStationId = nextNextId,
                                progress = nextProgress,
                                speedKmh = (30..75).random(),
                                crowding = CrowdingLevel.values().random()
                            )
                        }
                    } else {
                        train.copy(progress = nextProgress, speedKmh = (40..70).random())
                    }
                }
                _liveTrains.value = updated
            }
        }
    }


    // --- MULTIMODAL JOURNEY PLANNING ENGINE ---
    // Finds route paths connecting stations of different lines
    // Features BFS search + interchange nodes + accessibility & walking combinations
    fun planJourney(fromStationId: String, toStationId: String, options: Set<String> = emptySet()): List<JourneyPlannerOption> {
        if (fromStationId == toStationId) return emptyList()

        val results = mutableListOf<JourneyPlannerOption>()

        // Find standard path via Dijkstra/BFS
        val normalPath = findMetroRoute(fromStationId, toStationId)
        if (normalPath.isNotEmpty()) {
            val totalMinutes = normalPath.sumOf { it.durationMinutes } + (normalPath.size - 1) * 3 // 3 min per transfer/stop
            val walkingMinutes = normalPath.filter { it.stepType == "WALK" }.sumOf { it.durationMinutes }
            val fare = calculateFare(normalPath)
            val interchanges = normalPath.groupBy { it.lineId }.size - 1
            val isStepFree = normalPath.all { step ->
                val fromSt = stations.find { it.id == step.fromStationId }
                val toSt = stations.find { it.id == step.toStationId }
                (fromSt?.stepFreeAccess ?: true) && (toSt?.stepFreeAccess ?: true)
            }

            results.add(
                JourneyPlannerOption(
                    id = "fastest",
                    steps = normalPath,
                    totalDurationMinutes = totalMinutes,
                    totalWalkingMinutes = walkingMinutes + 3,
                    totalFare = fare,
                    interchangeCount = maxOf(0, interchanges),
                    isStepFree = isStepFree,
                    relativeCrowding = CrowdingLevel.MEDIUM,
                    type = "FASTEST"
                )
            )

            // Add alternative: Cheapest (e.g. fewer interchanges, or direct paths)
            results.add(
                JourneyPlannerOption(
                    id = "cheapest",
                    steps = normalPath,
                    totalDurationMinutes = totalMinutes + 5,
                    totalWalkingMinutes = walkingMinutes + 6,
                    totalFare = maxOf(20, fare - 10),
                    interchangeCount = maxOf(0, interchanges),
                    isStepFree = isStepFree,
                    relativeCrowding = CrowdingLevel.HIGH,
                    type = "CHEAPEST"
                )
            )

            // Add alternative: Most Accessible
            if (isStepFree) {
                results.add(
                    JourneyPlannerOption(
                        id = "accessible",
                        steps = normalPath,
                        totalDurationMinutes = totalMinutes,
                        totalWalkingMinutes = walkingMinutes,
                        totalFare = fare,
                        interchangeCount = maxOf(0, interchanges),
                        isStepFree = true,
                        relativeCrowding = CrowdingLevel.LOW,
                        type = "MOST_ACCESSIBLE"
                    )
                )
            }
        }

        // Add Multimodal Combination (e.g. Auto-Rickshaw or Walking overlay)
        val fromStation = stations.find { it.id == fromStationId }
        val toStation = stations.find { it.id == toStationId }
        if (fromStation != null && toStation != null) {
            val distanceKm = calculateDistance(fromStation.lat, fromStation.lon, toStation.lat, toStation.lon)
            if (distanceKm < 8.0) {
                val autoMinutes = (distanceKm * 4).roundToInt() + 5
                val autoSteps = listOf(
                    JourneyRouteStep(fromStationId, toStationId, null, "AUTO", autoMinutes, (distanceKm * 1000).roundToInt())
                )
                results.add(
                    JourneyPlannerOption(
                        id = "auto",
                        steps = autoSteps,
                        totalDurationMinutes = autoMinutes,
                        totalWalkingMinutes = 1,
                        totalFare = (distanceKm * 18).roundToInt() + 30, // base 30 + 18 per km auto fare
                        interchangeCount = 0,
                        isStepFree = true,
                        relativeCrowding = CrowdingLevel.LOW,
                        type = "FEWEST_CHANGES"
                    )
                )
            }
        }

        return results.sortedBy {
            when (it.type) {
                "FASTEST" -> 1
                "CHEAPEST" -> 2
                "FEWEST_CHANGES" -> 3
                "MOST_ACCESSIBLE" -> 4
                else -> 5
            }
        }
    }

    private fun findMetroRoute(startId: String, endId: String): List<JourneyRouteStep> {
        // Basic graph construction and BFS search to find shortest path in station nodes
        val queue = mutableListOf<List<String>>()
        queue.add(listOf(startId))
        val visited = mutableSetOf<String>()
        visited.add(startId)

        var pathResult: List<String>? = null
        while (queue.isNotEmpty()) {
            val currentPath = queue.removeFirst()
            val lastStationId = currentPath.last()

            if (lastStationId == endId) {
                pathResult = currentPath
                break
            }

            val adjacent = getAdjacentStations(lastStationId)
            for (adjId in adjacent) {
                if (adjId !in visited) {
                    visited.add(adjId)
                    val newPath = currentPath + adjId
                    queue.add(newPath)
                }
            }
        }

        if (pathResult == null || pathResult.size < 2) return emptyList()

        // Map stations list back to JourneyRouteSteps
        val steps = mutableListOf<JourneyRouteStep>()
        for (i in 0 until pathResult.size - 1) {
            val s1 = pathResult[i]
            val s2 = pathResult[i + 1]
            val st1 = stations.find { it.id == s1 }!!
            val st2 = stations.find { it.id == s2 }!!
            
            // Check if they are on same line
            var lineId: String? = st1.lineId
            var stepType = "METRO"
            var dist = (calculateDistance(st1.lat, st1.lon, st2.lat, st2.lon) * 1000).roundToInt()
            
            // Handle interchanges
            if (st1.lineId != st2.lineId) {
                // If walking interchange (e.g. Gundavali/WEH or DN Nagar L1/L2A or Marol Naka L1/L3)
                if (s1 == "weh" && s2 == "gundavali" || s1 == "gundavali" && s2 == "weh") {
                    stepType = "WALK"
                    lineId = null
                } else if (s1 == "marol_naka" && s2 == "csmia_t2" || s1 == "csmia_t2" && s2 == "marol_naka") {
                    stepType = "WALK"
                    lineId = null
                } else {
                    stepType = "WALK"
                    lineId = null
                }
            }

            val speedKmh = if (stepType == "WALK") 4 else 45
            val durationMin = maxOf(1, (dist.toDouble() / (speedKmh * 1000.0 / 60.0)).roundToInt())

            steps.add(
                JourneyRouteStep(
                    fromStationId = s1,
                    toStationId = s2,
                    lineId = lineId,
                    stepType = stepType,
                    durationMinutes = durationMin,
                    distanceMetres = dist
                )
            )
        }

        return steps
    }

    private fun getAdjacentStations(stationId: String): List<String> {
        val st = stations.find { it.id == stationId } ?: return emptyList()
        val adj = mutableListOf<String>()

        // Line connections (prev/next on same line)
        val lineStations = stations.filter { it.lineId == st.lineId }
        val index = lineStations.indexOf(st)
        if (index > 0) adj.add(lineStations[index - 1].id)
        if (index != -1 && index < lineStations.size - 1) adj.add(lineStations[index + 1].id)

        // Physical Walking Interchanges (Inter-line adapters)
        if (stationId == "weh") adj.add("gundavali")
        if (stationId == "gundavali") adj.add("weh")

        if (stationId == "marol_naka") adj.add("csmia_t2")
        if (stationId == "csmia_t2") adj.add("marol_naka")

        if (stationId == "dn_nagar") {
            // DN nagar L1 connects to Line 2A Malad/Kandivali
            adj.add("malad_w")
        }
        if (stationId == "malad_w") {
            adj.add("dn_nagar")
        }

        return adj.distinct()
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // earth radius km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun calculateFare(steps: List<JourneyRouteStep>): Int {
        val metroStepsCount = steps.filter { it.stepType == "METRO" }.size
        if (metroStepsCount == 0) return 10
        // Base fare ₹20 + ₹5 per station traversed
        return 20 + metroStepsCount * 5
    }
}
