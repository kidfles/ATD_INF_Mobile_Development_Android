package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.data.model.Workout
import com.avans.gymtracker.viewmodel.WorkoutViewModel

/**
 * Workout lijstscherm met volledige CRUD:
 * - CREATE: FAB → AddWorkoutScreen
 * - READ: LazyColumn met alle lokale workouts
 * - UPDATE/DELETE: via WorkoutDetailScreen of swipe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    onWorkoutClick: (Long) -> Unit,
    onAddWorkout: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onBack: () -> Unit = {}, // Navigeer terug naar het beginscherm
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val workouts by workoutViewModel.workouts.collectAsStateWithLifecycle()
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mijn Workouts") },
                navigationIcon = {
                    // Terug-pijl naar het beginscherm
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Terug")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToExercises) {
                        Icon(Icons.Default.Search, "Oefeningen")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Instellingen")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddWorkout,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nieuwe workout") }
            )
        }
    ) { padding ->
        if (workouts.isEmpty()) {
            // ── Lege staat ────────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Nog geen workouts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Druk op + om je eerste workout aan te maken",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutListItem(
                        workout = workout,
                        onClick = { onWorkoutClick(workout.id) },
                        onDelete = { workoutToDelete = workout }
                    )
                }
                // Ruimte voor FAB
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Bevestigingsdialoog verwijderen ────────────────────────────────────────
    if (workoutToDelete != null) {
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Workout verwijderen?") },
            text = { Text("\"${workoutToDelete!!.name}\" wordt permanent verwijderd.") },
            confirmButton = {
                Button(
                    onClick = {
                        workoutViewModel.deleteWorkout(workoutToDelete!!)
                        workoutToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Verwijderen") }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) { Text("Annuleer") }
            }
        )
    }
}

@Composable
private fun WorkoutListItem(
    workout: Workout,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icoon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (workout.isCompleted)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        if (workout.isCompleted) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = if (workout.isCompleted)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (workout.description.isNotBlank()) {
                    Text(
                        workout.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "📅 ${workout.formattedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    workout.formattedLastPerformed?.let {
                        Text(
                            "⏱ $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Verwijderen",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
