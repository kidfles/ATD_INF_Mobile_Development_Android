package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.data.model.Exercise
import com.avans.gymtracker.viewmodel.ExerciseViewModel

/**
 * Detail scherm voor een individuele oefening.
 * Toont naam, spiergroep, equipment en instructies.
 * Aangepaste layout voor landscape modus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    exerciseViewModel: ExerciseViewModel = viewModel()
) {
    val exercises by exerciseViewModel.exercises.collectAsStateWithLifecycle()
    val exercise = exercises.firstOrNull { it.id == exerciseId }
    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name?.replaceFirstChar { it.uppercase() } ?: "Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
                    }
                }
            )
        }
    ) { padding ->
        if (exercise == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (isLandscape) {
            // ── Landscape: twee kolommen ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Links: icoon + eigenschappen
                Column(
                    modifier = Modifier.weight(0.4f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExerciseHeroCard(exercise)
                    ExercisePropertiesCard(exercise)
                }
                // Rechts: instructies
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ExerciseInstructionsCard(exercise)
                    if (exercise.secondaryMuscles.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        SecondaryMusclesCard(exercise)
                    }
                }
            }
        } else {
            // ── Portrait: gescrold ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExerciseHeroCard(exercise)
                ExercisePropertiesCard(exercise)
                ExerciseInstructionsCard(exercise)
                if (exercise.secondaryMuscles.isNotEmpty()) {
                    SecondaryMusclesCard(exercise)
                }
            }
        }
    }
}

@Composable
private fun ExerciseHeroCard(exercise: Exercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                exercise.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExercisePropertiesCard(exercise: Exercise) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Eigenschappen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            PropertyRow("🎯 Spiergroep", exercise.target.replaceFirstChar { it.uppercase() })
            PropertyRow("🏋️ Lichaamsdeel", exercise.bodyPart.replaceFirstChar { it.uppercase() })
            PropertyRow("⚙️ Equipment", exercise.equipment.replaceFirstChar { it.uppercase() })
        }
    }
}

@Composable
private fun PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ExerciseInstructionsCard(exercise: Exercise) {
    if (exercise.instructions.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Instructies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            exercise.instructions.forEachIndexed { index, instruction ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            "${index + 1}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text(instruction, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SecondaryMusclesCard(exercise: Exercise) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Secundaire spieren", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                exercise.secondaryMuscles.forEach { muscle ->
                    AssistChip(onClick = {}, label = { Text(muscle.replaceFirstChar { it.uppercase() }) })
                }
            }
        }
    }
}
