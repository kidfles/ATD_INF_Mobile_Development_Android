package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.avans.gymtracker.data.model.BodyPart
import com.avans.gymtracker.utils.PreferencesManager

/**
 * Instellingenscherm.
 * Alle voorkeuren worden opgeslagen via SharedPreferences (PreferencesManager).
 *
 * Requirements gedekt:
 * - Instelling bewaard met SharedPreferences ✓
 * - Meerdere instellingen (body part, kg/lbs, notificaties, pagina-grootte) ✓
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    // Lokale state gespiegeld vanuit SharedPreferences
    var preferredBodyPart by remember { mutableStateOf(prefs.preferredBodyPart) }
    var useKilograms by remember { mutableStateOf(prefs.useKilograms) }
    var notificationsEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }
    var exercisesPerPage by remember { mutableIntStateOf(prefs.exercisesPerPage) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showBodyPartMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instellingen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Gebruikersprofiel ──────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profiel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(prefs.userName, style = MaterialTheme.typography.bodyLarge)
                            if (prefs.userEmail.isNotBlank()) {
                                Text(prefs.userEmail, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ── Oefening instellingen ──────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Oefeningen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()

                    // Voorkeur body part
                    SettingsRow(
                        icon = Icons.Default.FitnessCenter,
                        title = "Standaard spiergroep",
                        subtitle = "${preferredBodyPart.emoji} ${preferredBodyPart.displayName}"
                    ) {
                        Box {
                            TextButton(onClick = { showBodyPartMenu = true }) {
                                Text(preferredBodyPart.displayName)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(
                                expanded = showBodyPartMenu,
                                onDismissRequest = { showBodyPartMenu = false }
                            ) {
                                BodyPart.entries.forEach { part ->
                                    DropdownMenuItem(
                                        text = { Text("${part.emoji} ${part.displayName}") },
                                        onClick = {
                                            preferredBodyPart = part
                                            prefs.preferredBodyPart = part
                                            showBodyPartMenu = false
                                        },
                                        leadingIcon = if (preferredBodyPart == part) {
                                            { Icon(Icons.Default.Check, null) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    // Oefeningen per pagina
                    SettingsRow(
                        icon = Icons.Default.Numbers,
                        title = "Oefeningen per pagina",
                        subtitle = "$exercisesPerPage resultaten per laadactie"
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (exercisesPerPage > 10) {
                                    exercisesPerPage -= 10
                                    prefs.exercisesPerPage = exercisesPerPage
                                }
                            }) { Icon(Icons.Default.Remove, null) }
                            Text("$exercisesPerPage", fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                if (exercisesPerPage < 50) {
                                    exercisesPerPage += 10
                                    prefs.exercisesPerPage = exercisesPerPage
                                }
                            }) { Icon(Icons.Default.Add, null) }
                        }
                    }
                }
            }

            // ── Eenheden ──────────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Eenheden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()

                    SettingsRow(
                        icon = Icons.Default.Scale,
                        title = "Gewichtseenheid",
                        subtitle = if (useKilograms) "Kilogram (kg)" else "Pounds (lbs)"
                    ) {
                        Switch(
                            checked = useKilograms,
                            onCheckedChange = {
                                useKilograms = it
                                prefs.useKilograms = it
                            }
                        )
                    }
                }
            }

            // ── Notificaties ──────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Notificaties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()

                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        title = "Workout notificaties",
                        subtitle = if (notificationsEnabled) "Ingeschakeld" else "Uitgeschakeld"
                    ) {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                prefs.notificationsEnabled = it
                            }
                        )
                    }
                }
            }

            // ── Over de app ───────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Over", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Text("GymTracker Android v1.0", style = MaterialTheme.typography.bodySmall)
                    Text("API: ExerciseDB via RapidAPI", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Koray Yilmaz & Daan Hoeksema — AVANS MBDA",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── Uitloggen ──────────────────────────────────────────────────────
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Uitloggen")
            }
        }
    }

    // ── Uitlog bevestigingsdialoog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Uitloggen?") },
            text = { Text("Weet je zeker dat je wilt uitloggen? Je workout data blijft bewaard.") },
            confirmButton = {
                Button(
                    onClick = { prefs.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Uitloggen") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuleer") }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
