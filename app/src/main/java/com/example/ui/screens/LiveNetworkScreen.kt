package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MetroLine
import com.example.data.model.MetroStation
import com.example.data.repository.TransitRepository
import com.example.localization.Localizer
import com.example.ui.components.LineBadge
import com.example.ui.components.StatusBanner
import com.example.ui.viewmodel.TransitViewModel

@Composable
fun LiveNetworkScreen(
    viewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val liveTrains by viewModel.liveTrains.collectAsState()
    val alerts by viewModel.activeAlerts.collectAsState()

    var activeTab by remember { mutableStateOf("map") } // "map", "status"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Dynamic Title Header
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = Localizer.trans("live_metro"),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Operations Control Centre (OCC) Live Stream",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = if (activeTab == "map") 0 else 1,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == "map",
                onClick = { activeTab = "map" },
                text = { Text("Topological Map", style = MaterialTheme.typography.labelMedium) },
                icon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeTab == "status",
                onClick = { activeTab = "status" },
                text = { Text("System Status", style = MaterialTheme.typography.labelMedium) },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeTab == "map") {
            // Interactive Network Map Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Draw Metro Map Topological Canvas
                    NetworkMapCanvas(
                        stations = TransitRepository.stations,
                        lines = TransitRepository.lines,
                        liveTrains = liveTrains,
                        onStationClick = { station ->
                            viewModel.selectedStation.value = station
                            viewModel.currentScreen.value = "station"
                        }
                    )

                    // Overlay Map Legend Indicator
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Legend",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF005A9C), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Line 1 (Blue)", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFC72C), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Line 2A (Yellow)", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFF00A598), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Line 3 (Aqua)", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(Color(0xFFD01C1F), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Line 7 (Red)", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Live Train Unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        } else {
            // Live Status Logs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Line Status overview
                item {
                    Text(
                        text = "Active Metro Lines",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(TransitRepository.lines) { line ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(android.graphics.Color.parseColor(line.colorHex)), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = line.localizedName(currentLang),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            val isAlert = line.status != com.example.data.model.LineStatus.NORMAL
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isAlert) MaterialTheme.colorScheme.errorContainer
                                        else Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isAlert) "Delayed" else "Normal",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAlert) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Active Alerts
                if (alerts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Service Notifications",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(alerts) { alert ->
                        StatusBanner(
                            statusText = Localizer.trans(alert.descriptionKey),
                            isNormal = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkMapCanvas(
    stations: List<MetroStation>,
    lines: List<MetroLine>,
    liveTrains: List<com.example.data.model.LiveTrain>,
    onStationClick: (MetroStation) -> Unit
) {
    // Basic dynamic projection to map coordinates to Canvas pixel coordinates
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
            .clickable { /* Handle general map clicks */ }
    ) {
        val width = size.width
        val height = size.height

        // Mumbai geographic boundaries for metro stations:
        // Lat: 19.01 to 19.26
        // Lon: 72.80 to 72.92
        val minLat = 19.01
        val maxLat = 19.26
        val minLon = 72.80
        val maxLon = 72.92

        fun project(lat: Double, lon: Double): Offset {
            val x = ((lon - minLon) / (maxLon - minLon)) * (width - 100) + 50
            // Invert Y coordinate since Canvas top is 0
            val y = (1.0 - (lat - minLat) / (maxLat - minLat)) * (height - 100) + 50
            return Offset(x.toFloat(), y.toFloat())
        }

        // 1. Draw Network Connections (Lines)
        lines.forEach { line ->
            val lineColor = Color(android.graphics.Color.parseColor(line.colorHex))
            val lineStations = stations.filter { it.lineId == line.id }.sortedBy { it.lat }

            for (i in 0 until lineStations.size - 1) {
                val startOffset = project(lineStations[i].lat, lineStations[i].lon)
                val endOffset = project(lineStations[i + 1].lat, lineStations[i + 1].lon)

                drawLine(
                    color = lineColor,
                    start = startOffset,
                    end = endOffset,
                    strokeWidth = 6f
                )
            }
        }

        // Draw walking link (WEH to Gundavali)
        val weh = stations.find { it.id == "weh" }
        val gundavali = stations.find { it.id == "gundavali" }
        if (weh != null && gundavali != null) {
            drawLine(
                color = Color.Gray,
                start = project(weh.lat, weh.lon),
                end = project(gundavali.lat, gundavali.lon),
                strokeWidth = 3f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // 2. Draw Station Nodes
        stations.forEach { station ->
            val offset = project(station.lat, station.lon)
            val isInterchange = station.id == "weh" || station.id == "gundavali" || station.id == "dn_nagar" || station.id == "marol_naka" || station.id == "csmia_t2"

            drawCircle(
                color = if (isInterchange) Color.White else Color(android.graphics.Color.parseColor(lines.first { it.id == station.lineId }.colorHex)),
                radius = if (isInterchange) 12f else 8f,
                center = offset
            )

            if (isInterchange) {
                drawCircle(
                    color = Color.Black,
                    radius = 12f,
                    center = offset,
                    style = Stroke(width = 2f)
                )
            }
        }

        // 3. Draw Live Train Indicators (glowing red points)
        liveTrains.forEach { train ->
            val currentSt = stations.find { it.id == train.currentStationId }
            val nextSt = stations.find { it.id == train.nextStationId }

            if (currentSt != null && nextSt != null) {
                val startPos = project(currentSt.lat, currentSt.lon)
                val endPos = project(nextSt.lat, nextSt.lon)

                // Sub-second Linear Interpolation
                val x = startPos.x + (endPos.x - startPos.x) * train.progress
                val y = startPos.y + (endPos.y - startPos.y) * train.progress

                drawCircle(
                    color = Color(0xFFD01C1F),
                    radius = 10f,
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color(0xFFD01C1F).copy(alpha = 0.3f),
                    radius = 20f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
