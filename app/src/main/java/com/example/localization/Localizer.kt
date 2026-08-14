package com.example.localization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val nativeName: String, val isDevanagari: Boolean) {
    ENGLISH("en", "English", false),
    HINDI("hi", "हिन्दी", true),
    MARATHI("mr", "मराठी", true);

    companion object {
        fun fromCode(code: String): AppLanguage = values().find { it.code == code } ?: ENGLISH
    }
}

object Localizer {
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    // Number conversion to Devanagari for authentic Hindi/Marathi rendering
    fun formatDigits(number: Any): String {
        val str = number.toString()
        if (!_currentLanguage.value.isDevanagari) return str
        
        return str.map { char ->
            when (char) {
                '0' -> '०'
                '1' -> '१'
                '2' -> '२'
                '3' -> '३'
                '4' -> '४'
                '5' -> '५'
                '6' -> '६'
                '7' -> '७'
                '8' -> '८'
                '9' -> '९'
                else -> char
            }
        }.joinToString("")
    }

    // Currency formatting
    fun formatCurrency(amount: Int): String {
        return "₹${formatDigits(amount)}"
    }

    // Dynamic translation dictionary
    private val dictionary = mapOf(
        "app_title" to mapOf(
            AppLanguage.ENGLISH to "Mumbai Metro",
            AppLanguage.HINDI to "मुंबई मेट्रो",
            AppLanguage.MARATHI to "मुंबई मेट्रो"
        ),
        "tagline" to mapOf(
            AppLanguage.ENGLISH to "Mumbai moves. The app moves with it.",
            AppLanguage.HINDI to "मुंबई चलती है। यह ऐप भी साथ चलता है।",
            AppLanguage.MARATHI to "मुंबई धावते. हे ॲपही सोबत धावते."
        ),
        "where_am_i" to mapOf(
            AppLanguage.ENGLISH to "Where am I?",
            AppLanguage.HINDI to "मैं कहाँ हूँ?",
            AppLanguage.MARATHI to "मी कुठे आहे?"
        ),
        "nearest_station" to mapOf(
            AppLanguage.ENGLISH to "Nearest Metro Station",
            AppLanguage.HINDI to "निकटतम मेट्रो स्टेशन",
            AppLanguage.MARATHI to "जवळचे मेट्रो स्टेशन"
        ),
        "walking_distance" to mapOf(
            AppLanguage.ENGLISH to "%s min walk (%s m)",
            AppLanguage.HINDI to "%s मिनट की पैदल दूरी (%s मीटर)",
            AppLanguage.MARATHI to "%s मिनिटांचे अंतर (%s मीटर)"
        ),
        "next_train" to mapOf(
            AppLanguage.ENGLISH to "Next Train",
            AppLanguage.HINDI to "अगली ट्रेन",
            AppLanguage.MARATHI to "पुढची ट्रेन"
        ),
        "train_arriving_in" to mapOf(
            AppLanguage.ENGLISH to "Train arriving in %s mins",
            AppLanguage.HINDI to "ट्रेन %s मिनट में आ रही है",
            AppLanguage.MARATHI to "ट्रेन %s मिनिटांत येत आहे"
        ),
        "train_arriving_now" to mapOf(
            AppLanguage.ENGLISH to "Train Arriving Now",
            AppLanguage.HINDI to "ट्रेन अभी आ रही है",
            AppLanguage.MARATHI to "ट्रेन आता येत आहे"
        ),
        "plan_journey" to mapOf(
            AppLanguage.ENGLISH to "Plan Journey",
            AppLanguage.HINDI to "यात्रा की योजना बनाएं",
            AppLanguage.MARATHI to "प्रवासाचे नियोजन"
        ),
        "buy_ticket" to mapOf(
            AppLanguage.ENGLISH to "Buy Ticket",
            AppLanguage.HINDI to "टिकट खरीदें",
            AppLanguage.MARATHI to "तिकीट खरेदी करा"
        ),
        "live_metro" to mapOf(
            AppLanguage.ENGLISH to "Live Metro",
            AppLanguage.HINDI to "लाइव मेट्रो",
            AppLanguage.MARATHI to "थेट मेट्रो"
        ),
        "network_map" to mapOf(
            AppLanguage.ENGLISH to "Network Map",
            AppLanguage.HINDI to "नेटवर्क मैप",
            AppLanguage.MARATHI to "नकाशा"
        ),
        "service_alerts" to mapOf(
            AppLanguage.ENGLISH to "Service Alerts",
            AppLanguage.HINDI to "सेवा अलर्ट",
            AppLanguage.MARATHI to "सेवा सूचना"
        ),
        "saved_journeys" to mapOf(
            AppLanguage.ENGLISH to "Saved Journeys",
            AppLanguage.HINDI to "सहेजी गई यात्राएं",
            AppLanguage.MARATHI to "जतन केलेले प्रवास"
        ),
        "recent_journeys" to mapOf(
            AppLanguage.ENGLISH to "Recent Journeys",
            AppLanguage.HINDI to "हालिया यात्राएं",
            AppLanguage.MARATHI to "अलीकडील प्रवास"
        ),
        "favorite_stations" to mapOf(
            AppLanguage.ENGLISH to "Favorite Stations",
            AppLanguage.HINDI to "पसंदीदा स्टेशन",
            AppLanguage.MARATHI to "आवडते स्टेशन्स"
        ),
        "qr_ticket" to mapOf(
            AppLanguage.ENGLISH to "QR Ticket",
            AppLanguage.HINDI to "क्यूआर टिकट",
            AppLanguage.MARATHI to "क्यूआर तिकीट"
        ),
        "account_payment" to mapOf(
            AppLanguage.ENGLISH to "Account & Payments",
            AppLanguage.HINDI to "खाता और भुगतान",
            AppLanguage.MARATHI to "खाते आणि पेमेंट"
        ),
        "go_to_station" to mapOf(
            AppLanguage.ENGLISH to "Go to Station",
            AppLanguage.HINDI to "स्टेशन पर जाएँ",
            AppLanguage.MARATHI to "स्टेशनवर जा"
        ),
        "search_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Search stations, landmarks, or lines...",
            AppLanguage.HINDI to "स्टेशन, मील के पत्थर या लाइन खोजें...",
            AppLanguage.MARATHI to "स्टेशन, महत्त्वाचे ठिकाण किंवा मार्ग शोधा..."
        ),
        "from" to mapOf(
            AppLanguage.ENGLISH to "From",
            AppLanguage.HINDI to "कहाँ से",
            AppLanguage.MARATHI to "कुठून"
        ),
        "to" to mapOf(
            AppLanguage.ENGLISH to "To",
            AppLanguage.HINDI to "कहाँ तक",
            AppLanguage.MARATHI to "कुठे"
        ),
        "total_time" to mapOf(
            AppLanguage.ENGLISH to "%s min",
            AppLanguage.HINDI to "%s मिनट",
            AppLanguage.MARATHI to "%s मिनिटे"
        ),
        "walking_time" to mapOf(
            AppLanguage.ENGLISH to "Walk %s min",
            AppLanguage.HINDI to "पैदल %s मिनट",
            AppLanguage.MARATHI to "चालणे %s मिनिटे"
        ),
        "interchange" to mapOf(
            AppLanguage.ENGLISH to "Interchange at %s",
            AppLanguage.HINDI to "%s पर इंटरचेंज",
            AppLanguage.MARATHI to "%s वर बदला"
        ),
        "start_journey" to mapOf(
            AppLanguage.ENGLISH to "Start Journey",
            AppLanguage.HINDI to "यात्रा शुरू करें",
            AppLanguage.MARATHI to "प्रवास सुरू करा"
        ),
        "navigation_guide" to mapOf(
            AppLanguage.ENGLISH to "Walk %s metres to %s",
            AppLanguage.HINDI to "%s तक %s मीटर चलें",
            AppLanguage.MARATHI to "%s पर्यंत %s मीटर चला"
        ),
        "fastest" to mapOf(
            AppLanguage.ENGLISH to "Fastest",
            AppLanguage.HINDI to "सबसे तेज़",
            AppLanguage.MARATHI to "सर्वात जलद"
        ),
        "cheapest" to mapOf(
            AppLanguage.ENGLISH to "Cheapest",
            AppLanguage.HINDI to "सबसे सस्ता",
            AppLanguage.MARATHI to "सर्वात स्वस्त"
        ),
        "fewest_changes" to mapOf(
            AppLanguage.ENGLISH to "Fewest Changes",
            AppLanguage.HINDI to "न्यूनतम बदलाव",
            AppLanguage.MARATHI to "किमान बदल"
        ),
        "most_accessible" to mapOf(
            AppLanguage.ENGLISH to "Most Accessible",
            AppLanguage.HINDI to "सर्वाधिक सुलभ",
            AppLanguage.MARATHI to "सर्वात सुलभ"
        ),
        "step_free_only" to mapOf(
            AppLanguage.ENGLISH to "Show only step-free routes",
            AppLanguage.HINDI to "केवल सीढ़ी-मुक्त मार्ग दिखाएं",
            AppLanguage.MARATHI to "फक्त पायऱ्या नसलेले मार्ग दाखवा"
        ),
        "voice_assistant" to mapOf(
            AppLanguage.ENGLISH to "Transit Voice Assistant",
            AppLanguage.HINDI to "ट्रांजिट वॉइस असिस्टेंट",
            AppLanguage.MARATHI to "व्हाईस असिस्टंट"
        ),
        "voice_placeholder" to mapOf(
            AppLanguage.ENGLISH to "Tap mic & say e.g., 'How do I get to Ghatkopar?'",
            AppLanguage.HINDI to "माइक दबाएं और कहें, 'घाटकोपर कैसे जाना है?'",
            AppLanguage.MARATHI to "माईक दाबा आणि म्हणा, 'घाटकोपरला कसं जायचं?'"
        ),
        "settings" to mapOf(
            AppLanguage.ENGLISH to "Settings",
            AppLanguage.HINDI to "सेटिंग्स",
            AppLanguage.MARATHI to "सेटिंग्ज"
        ),
        "change_language" to mapOf(
            AppLanguage.ENGLISH to "App Language",
            AppLanguage.HINDI to "ऐप की भाषा",
            AppLanguage.MARATHI to "ॲपची भाषा"
        ),
        "select_language" to mapOf(
            AppLanguage.ENGLISH to "Select Language",
            AppLanguage.HINDI to "भाषा चुनें",
            AppLanguage.MARATHI to "भाषा निवडा"
        ),
        "ticket_summary" to mapOf(
            AppLanguage.ENGLISH to "Active QR Ticket",
            AppLanguage.HINDI to "सक्रिय क्यूआर टिकट",
            AppLanguage.MARATHI to "सक्रिय क्यूआर तिकीट"
        ),
        "ticket_id" to mapOf(
            AppLanguage.ENGLISH to "Ticket ID: %s",
            AppLanguage.HINDI to "टिकट आईडी: %s",
            AppLanguage.MARATHI to "तिकीट आयडी: %s"
        ),
        "validity" to mapOf(
            AppLanguage.ENGLISH to "Valid on %s",
            AppLanguage.HINDI to "%s को मान्य",
            AppLanguage.MARATHI to "%s ला वैध"
        ),
        "pay_and_generate" to mapOf(
            AppLanguage.ENGLISH to "Pay %s & Generate Ticket",
            AppLanguage.HINDI to "%s भुगतान करें और टिकट बनाएं",
            AppLanguage.MARATHI to "%s पे करा आणि तिकीट मिळवा"
        ),
        "scan_and_confirm" to mapOf(
            AppLanguage.ENGLISH to "Scan UPI QR to Pay",
            AppLanguage.HINDI to "भुगतान करने के लिए यूपीआई क्यूआर स्कैन करें",
            AppLanguage.MARATHI to "पेमेंटसाठी यूपीआई क्यूआर स्कॅन करा"
        ),
        "alert_line_1_delay" to mapOf(
            AppLanguage.ENGLISH to "Line 1 services are experiencing delays of approximately 10 minutes.",
            AppLanguage.HINDI to "लाइन 1 की सेवाओं में लगभग 10 मिनट की देरी हो रही है।",
            AppLanguage.MARATHI to "लाइन 1 च्या सेवांना अंदाजे १० मिनिटांचा विलंब होत आहे."
        ),
        "alert_line_3_notice" to mapOf(
            AppLanguage.ENGLISH to "Line 3 (Aqua underground) running fully operational with high frequency.",
            AppLanguage.HINDI to "लाइन 3 (एक्वा अंडरग्राउंड) उच्च आवृत्ति के साथ पूरी तरह से चालू है।",
            AppLanguage.MARATHI to "लाइन 3 (अ‍ॅक्वा भूमिगत) उच्च वारंवारतेसह पूर्णपणे कार्यरत आहे."
        ),
        "no_live_data" to mapOf(
            AppLanguage.ENGLISH to "No live train data available. Showing cached schedule.",
            AppLanguage.HINDI to "कोई लाइव ट्रेन डेटा उपलब्ध नहीं है। कैश्ड शेड्यूल दिखा रहा है।",
            AppLanguage.MARATHI to "थेट ट्रेन माहिती उपलब्ध नाही. जतन केलेले वेळापत्रक दाखवत आहे."
        ),
        "connecting_live_feed" to mapOf(
            AppLanguage.ENGLISH to "Connected to Mumbai Metro Operations Control (OCC)",
            AppLanguage.HINDI to "मुंबई मेट्रो ऑपरेशंस कंट्रोल (OCC) से जुड़ा है",
            AppLanguage.MARATHI to "मुंबई मेट्रो ऑपरेशन्स कंट्रोल (OCC) शी जोडलेले आहे"
        )
    )

    fun trans(key: String, vararg args: Any): String {
        val lang = _currentLanguage.value
        val localizedMap = dictionary[key] ?: return key
        val baseTranslation = localizedMap[lang] ?: localizedMap[AppLanguage.ENGLISH] ?: key
        
        if (args.isEmpty()) {
            return baseTranslation
        }
        
        // Formatter with digits support
        val formattedArgs = args.map { arg ->
            if (arg is Number) formatDigits(arg) else arg.toString()
        }.toTypedArray()

        return try {
            String.format(baseTranslation, *formattedArgs)
        } catch (e: Exception) {
            baseTranslation
        }
    }
}
