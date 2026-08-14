package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JourneyPlannerOption
import com.example.data.model.MetroStation
import com.example.data.repository.TransitRepository
import com.example.localization.Localizer
import com.example.ui.components.AccessibilityBadge
import com.example.ui.components.LineBadge
import com.example.ui.components.MetroButton
import com.example.ui.viewmodel.TransitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyPlannerScreen(
    viewModel: TransitViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val fromSt by viewModel.fromStation.collectAsState()
    val toSt by viewModel.toStation.collectAsState()
    val plannedRoutes by viewModel.plannedRoutes.collectAsState()
    val selectedOption by viewModel.selectedRouteOption.collectAsState()
    val isStepFree by viewModel.isStepFreeOnly.collectAsState()
    val isNavigating by viewModel.isNavigating.collectAsState()
    val activeStepIndex by viewModel.activeNavigationStepIndex.collectAsState()
    val savedJourneys by viewModel.savedJourneys.collectAsState()

    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }

    val stations = TransitRepository.stations

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (isNavigating && selectedOption != null) {
            // --- ACTIVE STEP-BY-STEP NAVIGATION GUIDANCE ---
            val option = selectedOption!!
            val step = option.steps.getOrNull(activeStepIndex)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACTIVE NAVIGATION",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live GPS Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (step != null) {
                            val stepFromSt = TransitRepository.stations.find { it.id == step.fromStationId }
                            val stepToSt = TransitRepository.stations.find { it.id == step.toStationId }
                            val line = TransitRepository.lines.find { it.id == step.lineId }

                            // Big directional prompt in Devanagari/English
                            val guideText = if (step.stepType == "WALK") {
                                Localizer.trans("navigation_guide", step.distanceMetres.toString(), stepToSt?.localizedName(currentLang) ?: "")
                            } else {
                                val action = if (currentLang.code == "hi") "ट्रेन लें" else if (currentLang.code == "mr") "ट्रेन पकडा" else "Take Train"
                                "$action: ${line?.localizedName(currentLang)} -> ${stepToSt?.localizedName(currentLang)}"
                            }

                            Text(
                                text = guideText,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                lineHeight = 34.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (step.stepType == "WALK") Icons.Default.DirectionsWalk else Icons.Default.DirectionsTransit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${Localizer.formatDigits(step.durationMinutes)} mins (${Localizer.formatDigits(step.distanceMetres)} m)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // Node Visual Timeline Diagram
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(option.steps) { index, routeStep ->
                            val st1 = TransitRepository.stations.find { it.id == routeStep.fromStationId }!!
                            val st2 = TransitRepository.stations.find { it.id == routeStep.toStationId }!!
                            val isCompleted = index < activeStepIndex
                            val isActive = index == activeStepIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isActive) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Line Bullet Indicator
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (isCompleted) Color(0xFF4CAF50)
                                                else if (isActive) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCompleted) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = Localizer.formatDigits(index + 1),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${st1.localizedName(currentLang)} → ${st2.localizedName(currentLang)}",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                                            ),
                                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${routeStep.stepType} • ${Localizer.formatDigits(routeStep.durationMinutes)} mins",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Control panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.stopJourneyNavigation() },
                        modifier = Modifier
                            .weight(1.0f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Exit", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = { viewModel.advanceNavigationStep() },
                        modifier = Modifier
                            .weight(1.0f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (activeStepIndex == option.steps.size - 1) "Arrived" else "Next Step",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

        } else {
            // --- JOURNEY INPUT SEARCH AND PLANNER PANEL ---
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = Localizer.trans("plan_journey"),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Origin Selection Input
                        Box {
                            OutlinedTextField(
                                value = fromSt?.localizedName(currentLang) ?: "",
                                onValueChange = {},
                                label = { Text(Localizer.trans("from")) },
                                leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Color(0xFF4CAF50)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showFromDropdown = true },
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            DropdownMenu(
                                expanded = showFromDropdown,
                                onDismissRequest = { showFromDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                stations.forEach { station ->
                                    DropdownMenuItem(
                                        text = { Text(station.localizedName(currentLang)) },
                                        onClick = {
                                            viewModel.fromStation.value = station
                                            showFromDropdown = false
                                            viewModel.triggerJourneyPlanning()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reverse and Link Node Visual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 24.dp)
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            IconButton(
                                onClick = { viewModel.reverseJourney() },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Reverse Journey",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Destination Selection Input
                        Box {
                            OutlinedTextField(
                                value = toSt?.localizedName(currentLang) ?: "",
                                onValueChange = {},
                                label = { Text(Localizer.trans("to")) },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showToDropdown = true },
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            DropdownMenu(
                                expanded = showToDropdown,
                                onDismissRequest = { showToDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                stations.forEach { station ->
                                    DropdownMenuItem(
                                        text = { Text(station.localizedName(currentLang)) },
                                        onClick = {
                                            viewModel.toStation.value = station
                                            showToDropdown = false
                                            viewModel.triggerJourneyPlanning()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Accessibility Option toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Accessible,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Localizer.trans("step_free_only"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = isStepFree,
                                onCheckedChange = {
                                    viewModel.isStepFreeOnly.value = it
                                    viewModel.triggerJourneyPlanning()
                                }
                            )
                        }
                    }
                }
            }

            // Results and alternatives
            if (fromSt == null || toSt == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.0f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select origin and destination stations to plan route.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                val isSaved = savedJourneys.any { it.fromStationId == fromSt!!.id && it.toStationId == toSt!!.id }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Routes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            if (isSaved) {
                                viewModel.deleteSavedJourneyLocally(fromSt!!.id, toSt!!.id)
                            } else {
                                viewModel.saveJourneyLocally(fromSt!!.id, toSt!!.id)
                            }
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Journey",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (plannedRoutes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No routes found matching your criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.0f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(plannedRoutes) { index, option ->
                            val isSelected = selectedOption?.id == option.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectedRouteOption.value = option },
                                shape = RoundedCornerShape(16.dp),
                                border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (option.type) {
                                                            "FASTEST" -> MaterialTheme.colorScheme.primary
                                                            "CHEAPEST" -> Color(0xFF4CAF50)
                                                            else -> MaterialTheme.colorScheme.secondary
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = Localizer.trans(option.type.lowercase()),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                        Text(
                                            text = Localizer.formatCurrency(option.totalFare),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Display step timeline horizontally
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        option.steps.forEachIndexed { stepIndex, step ->
                                            val stepFrom = stations.find { it.id == step.fromStationId }
                                            val stepTo = stations.find { it.id == step.toStationId }

                                            if (stepIndex == 0) {
                                                Text(
                                                    text = stepFrom?.localizedName(currentLang) ?: "",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }

                                            Icon(
                                                imageVector = if (step.stepType == "WALK") Icons.Default.DirectionsWalk else Icons.Default.DirectionsTransit,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .size(14.dp),
                                                tint = MaterialTheme.colorScheme.outline
                                            )

                                            if (stepIndex == option.steps.size - 1) {
                                                Text(
                                                    text = stepTo?.localizedName(currentLang) ?: "",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = Localizer.trans("total_time", option.totalDurationMinutes.toString()),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsWalk,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = Localizer.trans("walking_time", option.totalWalkingMinutes.toString()),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        AccessibilityBadge(
                                            hasLift = true,
                                            hasEscalator = true,
                                            stepFree = option.isStepFree
                                        )
                                    }

                                    if (isSelected) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.buyTicket(fromSt!!.id, toSt!!.id, option.totalFare, "SINGLE")
                                                },
                                                modifier = Modifier.weight(1.0f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Get Pass")
                                            }

                                            Button(
                                                onClick = { viewModel.startJourneyNavigation(option) },
                                                modifier = Modifier.weight(1.0f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(Localizer.trans("start_journey"))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
