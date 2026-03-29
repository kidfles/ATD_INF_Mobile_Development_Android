package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.data.model.BodyPart
import com.avans.gymtracker.data.model.Exercise
import com.avans.gymtracker.viewmodel.ExerciseViewModel
import com.avans.gymtracker.viewmodel.WorkoutViewModel

/**
 * Scherm voor het aanmaken van een nieuwe workout (CREATE).
 * Oefeningen worden geselecteerd uit de API-lijst.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    onWorkoutCreated: (Long) -> Unit,
    onBack: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel(),
    exerciseViewModel: ExerciseViewModel = viewModel()
) {
    var workoutName by remember { mutableStateOf("") }
    var workoutDescription by remember { mutableStateOf("") }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    var showExercisePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val isSaving by workoutViewModel.isSaving.collectAsStateWithLifecycle()

    if (showExercisePicker) {
        ExercisePickerSheet(
            exerciseViewModel = exerciseViewModel,
            selectedExercises = selectedExercises,
            onDismiss = { showExercisePicker = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nieuwe workout") },
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
            // ── Workout details ────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Workout details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = workoutName,
                        onValueChange = { workoutName = it; nameError = false },
                        label = { Text("Naam*") },
                        isError = nameError,
                        supportingText = { if (nameError) Text("Naam is verplicht") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = workoutDescription,
                        onValueChange = { workoutDescription = it },
                        label = { Text("Beschrijving (optioneel)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            // ── Geselecteerde oefeningen ───────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Oefeningen (${selectedExercises.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showExercisePicker = true }) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Toevoegen")
                        }
                    }

                    if (selectedExercises.isEmpty()) {
                        Text(
                            "Nog geen oefeningen geselecteerd",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        selectedExercises.forEachIndexed { index, exercise ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${index + 1}.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        exercise.name.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        exercise.target.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { selectedExercises.removeAt(index) }) {
                                    Icon(
                                        Icons.Default.RemoveCircle,
                                        contentDescription = "Verwijderen",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Opslaan knop ──────────────────────────────────────────────────
            Button(
                onClick = {
                    if (workoutName.isBlank()) { nameError = true; return@Button }
                    workoutViewModel.createWorkout(
                        name = workoutName.trim(),
                        description = workoutDescription.trim(),
                        exercises = selectedExercises.toList()
                    ) { newId -> onWorkoutCreated(newId) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Workout opslaan")
            }
        }
    }
}

/**
 * Modal bottom-sheet stijl picker voor oefeningen.
 * Toont LazyColumn met API-oefeningen gefilterd op body part.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    exerciseViewModel: ExerciseViewModel,
    selectedExercises: MutableList<Exercise>,
    onDismiss: () -> Unit
) {
    val exercises by exerciseViewModel.exercises.collectAsStateWithLifecycle()
    val isLoading by exerciseViewModel.isLoading.collectAsStateWithLifecycle()
    val selectedBodyPart by exerciseViewModel.selectedBodyPart.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oefening kiezen") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Sluiten")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Body part tabs
            ScrollableTabRow(
                selectedTabIndex = BodyPart.entries.indexOf(selectedBodyPart)
            ) {
                BodyPart.entries.forEachIndexed { index, bodyPart ->
                    Tab(
                        selected = selectedBodyPart == bodyPart,
                        onClick = { exerciseViewModel.selectBodyPart(bodyPart) },
                        text = { Text("${bodyPart.emoji} ${bodyPart.displayName}") }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercises, key = { it.id }) { exercise ->
                        val isSelected = selectedExercises.any { it.id == exercise.id }
                        Card(
                            onClick = {
                                if (isSelected) selectedExercises.removeIf { it.id == exercise.id }
                                else selectedExercises.add(exercise)
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        exercise.name.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        exercise.target.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Geselecteerd",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
