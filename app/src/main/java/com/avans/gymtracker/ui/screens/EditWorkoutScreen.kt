package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.viewmodel.WorkoutViewModel

/**
 * Scherm voor het bewerken van een bestaande workout (UPDATE).
 * Equivalent van EditWorkoutView in iOS.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    workoutId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val workout by workoutViewModel.currentWorkout.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId) {
        workoutViewModel.loadWorkout(workoutId)
    }

    // Vul velden in zodra workout geladen is
    LaunchedEffect(workout) {
        if (workout != null && !initialized) {
            name = workout!!.name
            description = workout!!.description
            isCompleted = workout!!.isCompleted
            initialized = true
        }
    }

    fun saveWorkout() {
        if (name.isBlank()) { nameError = true; return }
        workout?.let {
            workoutViewModel.updateWorkout(
                it.copy(
                    name = name.trim(),
                    description = description.trim(),
                    isCompleted = isCompleted
                )
            )
        }
        onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout bewerken") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
                    }
                },
                actions = {
                    IconButton(onClick = ::saveWorkout) {
                        Icon(Icons.Default.Check, "Opslaan")
                    }
                }
            )
        }
    ) { padding ->
        if (workout == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Workout details ────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Workout details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = false },
                        label = { Text("Naam*") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Naam is verplicht") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Beschrijving") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            // ── Status ─────────────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Markeer als voltooid", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Workout is afgerond",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isCompleted,
                            onCheckedChange = { isCompleted = it }
                        )
                    }
                }
            }

            // ── Opslaan knop ──────────────────────────────────────────────────
            Button(
                onClick = ::saveWorkout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Wijzigingen opslaan")
            }
        }
    }
}
