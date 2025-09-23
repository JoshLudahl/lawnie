package com.softklass.lawnie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FormatListBulleted

@Immutable
enum class LawnieTab(val label: String, val icon: ImageVector) {
    Dashboard(label = "Dashboard", icon = Icons.Filled.Home),
    Calendar(label = "Calendar", icon = Icons.Filled.DateRange),
    Tasks(label = "Tasks", icon = Icons.Filled.FormatListBulleted),
    Plants(label = "Plants", icon = Icons.Filled.Grass),
    Settings(label = "Settings", icon = Icons.Filled.Settings),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LawnieTopAppBar(tab: LawnieTab) {
    TopAppBar(
        title = { Text("Lawnie • ${tab.label}") }
    )
}

@Composable
fun LawnieBottomBar(selected: LawnieTab, onSelect: (LawnieTab) -> Unit) {
    NavigationBar {
        LawnieTab.values().forEach { tab ->
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
fun LawnieNavHost(
    tab: LawnieTab,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    when (tab) {
        LawnieTab.Dashboard -> DashboardScreen(modifier.padding(contentPadding))
        LawnieTab.Calendar -> CalendarScreen(modifier.padding(contentPadding))
        LawnieTab.Tasks -> TasksScreen(modifier.padding(contentPadding))
        LawnieTab.Plants -> PlantsScreen(modifier.padding(contentPadding))
        LawnieTab.Settings -> SettingsScreen(modifier.padding(contentPadding))
    }
}

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Welcome to Lawnie",
        message = "Track mowing, fertilizing, watering, and overseeding. Your upcoming items will appear here.",
        modifier = modifier
    )
}

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Calendar",
        message = "A month view of all lawn and garden events will go here.",
        modifier = modifier
    )
}

@Composable
fun TasksScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Tasks",
        message = "See and manage upcoming tasks like mowing schedule, fertilizing, and watering reminders.",
        modifier = modifier
    )
}

@Composable
fun PlantsScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Lawns & Plants",
        message = "Define your lawn zones, grass type, and plants to tailor care schedules.",
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    // Zone selection UI with server-backed data
    var zones by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<Zone>>(emptyList()) }
    var loading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    var error by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var selected by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Zone?>(null) }
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var savedMsg by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            loading = true
            error = null
            zones = apiGetZones()
        } catch (t: Throwable) {
            error = "Failed to load zones: ${'$'}{t.message}"
        } finally {
            loading = false
        }
    }

    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val deviceId = "${'$'}{getPlatform().name}-device"

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Select your agriculture zone (USDA Plant Hardiness, 2023 update)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            Button(onClick = {
                scope.launch {
                    try {
                        loading = true
                        error = null
                        zones = apiGetZones()
                    } catch (t: Throwable) {
                        error = "Failed to load zones: ${'$'}{t.message}"
                    } finally {
                        loading = false
                    }
                }
            }, modifier = Modifier.padding(top = 8.dp)) { Text("Retry") }
        } else {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.padding(top = 16.dp)) {
                OutlinedTextField(
                    readOnly = true,
                    value = selected?.name ?: "Select zone",
                    onValueChange = {},
                    label = { Text("Zone") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    zones.forEach { zone ->
                        DropdownMenuItem(
                            text = { Text(zone.name) },
                            onClick = {
                                selected = zone
                                expanded = false
                                savedMsg = null
                            }
                        )
                    }
                }
            }

            Button(onClick = {
                val z = selected ?: return@Button
                scope.launch {
                    savedMsg = null
                    val ok = apiSaveUserZone(deviceId = deviceId, zoneCode = z.code)
                    savedMsg = if (ok) "Saved zone ${'$'}{z.code} to server" else "Failed to save"
                }
            }, enabled = selected != null, modifier = Modifier.padding(top = 16.dp)) {
                Text("Save to Server")
            }

            if (savedMsg != null) {
                Text(text = savedMsg!!, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text(text = message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
        Text(text = "(Initial placeholder UI)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 24.dp))
    }
}
