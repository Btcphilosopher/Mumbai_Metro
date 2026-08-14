package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.localization.Localizer
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TransitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(this)[TransitViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: TransitViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isVoiceOpen by viewModel.isVoiceAssistantOpen.collectAsState()
    val voiceQuery by viewModel.voiceQuery.collectAsState()
    val voiceReply by viewModel.voiceReply.collectAsState()

    var textInputState by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { viewModel.currentScreen.value = "home" },
                    label = { Text(Localizer.trans("home")) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                )
                NavigationBarItem(
                    selected = currentScreen == "planner",
                    onClick = { viewModel.currentScreen.value = "planner" },
                    label = { Text(Localizer.trans("plan_journey")) },
                    icon = { Icon(Icons.Default.Route, contentDescription = "Planner") }
                )
                NavigationBarItem(
                    selected = currentScreen == "live",
                    onClick = { viewModel.currentScreen.value = "live" },
                    label = { Text(Localizer.trans("live_metro")) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Live OCC") }
                )
                NavigationBarItem(
                    selected = currentScreen == "ticket",
                    onClick = { viewModel.currentScreen.value = "ticket" },
                    label = { Text(Localizer.trans("buy_ticket")) },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = "Tickets") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.isVoiceAssistantOpen.value = true
                    viewModel.voiceReply.value = "Ask me any metro route or query!"
                    viewModel.voiceQuery.value = ""
                    textInputState = ""
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Assistant")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            when (currentScreen) {
                "home" -> HomeScreen(viewModel = viewModel)
                "planner" -> JourneyPlannerScreen(viewModel = viewModel)
                "live" -> LiveNetworkScreen(viewModel = viewModel)
                "ticket" -> TicketScreen(viewModel = viewModel)
                "station" -> StationDetailScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
                else -> HomeScreen(viewModel = viewModel)
            }

            // --- PREMIUM VOICE ASSISTANT SHEET OVERLAY ---
            if (isVoiceOpen) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Mumbai Metro Voice AI",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.isVoiceAssistantOpen.value = false }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close")
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // AI Response bubble
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        if (voiceQuery.isNotEmpty()) {
                                            Text(
                                                text = "You: \"$voiceQuery\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Text(
                                            text = voiceReply,
                                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick suggestion templates
                                Text(
                                    text = "Tap to simulate standard queries:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SuggestionChip(
                                        label = "Andheri to Ghatkopar",
                                        onClick = {
                                            viewModel.executeVoiceQuery("Route from Andheri to Ghatkopar")
                                        }
                                    )
                                    SuggestionChip(
                                        label = "Where is Versova?",
                                        onClick = {
                                            viewModel.executeVoiceQuery("Take me to Versova")
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Text entry box fallback
                                OutlinedTextField(
                                    value = textInputState,
                                    onValueChange = { textInputState = it },
                                    label = { Text("Speak or type transit question...") },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                viewModel.executeVoiceQuery(textInputState)
                                                textInputState = ""
                                            }
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send")
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(
                                        onSend = {
                                            viewModel.executeVoiceQuery(textInputState)
                                            textInputState = ""
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
