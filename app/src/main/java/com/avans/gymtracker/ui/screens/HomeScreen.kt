package com.avans.gymtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.utils.PreferencesManager
import com.avans.gymtracker.viewmodel.WorkoutViewModel
import java.util.Calendar

/**
 * Home scherm met statistieken dashboard.
 * Equivalent van ContentView in iOS.
 * Aangepaste layout voor portrait én landscape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    prefs: PreferencesManager,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToSettings: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val workouts by workoutViewModel.workouts.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.screenWidthDp >
            LocalConfiguration.current.screenHeightDp

    // Statistieken berekenen
    val totalWorkouts = workouts.size
    val completedWorkouts = workouts.count { it.isCompleted }
    val daysActive = workouts
        .mapNotNull { it.lastPerformed }
        .map { ts ->
            val cal = Calendar.getInstance().apply { timeInMillis = ts }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }
        .toSet()
        .size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Instellingen")
                    }
                }
            )
        }
    ) { padding ->
        if (isLandscape) {
            LandscapeHomeLayout(
                userName = prefs.userName,
                totalWorkouts = totalWorkouts,
                completedWorkouts = completedWorkouts,
                daysActive = daysActive,
                onNavigateToWorkouts = onNavigateToWorkouts,
                onNavigateToExercises = onNavigateToExercises,
                modifier = Modifier.padding(padding)
            )
        } else {
            PortraitHomeLayout(
                userName = prefs.userName,
                totalWorkouts = totalWorkouts,
                completedWorkouts = completedWorkouts,
                daysActive = daysActive,
                onNavigateToWorkouts = onNavigateToWorkouts,
                onNavigateToExercises = onNavigateToExercises,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun PortraitHomeLayout(
    userName: String,
    totalWorkouts: Int,
    completedWorkouts: Int,
    daysActive: Int,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            "Welkom, $userName!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Track your fitness journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Navigatieknoppen
        Button(
            onClick = onNavigateToWorkouts,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.List, null)
            Spacer(Modifier.width(8.dp))
            Text("Mijn Workouts")
        }

        OutlinedButton(
            onClick = onNavigateToExercises,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, null)
            Spacer(Modifier.width(8.dp))
            Text("Oefeningen Zoeken")
        }

        // Statistieken
        StatsCard(
            totalWorkouts = totalWorkouts,
            completedWorkouts = completedWorkouts,
            daysActive = daysActive
        )
    }
}

@Composable
private fun LandscapeHomeLayout(
    userName: String,
    totalWorkouts: Int,
    completedWorkouts: Int,
    daysActive: Int,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Linkerkolom: welkom + knoppen
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Welkom, $userName!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Track your fitness journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(onClick = onNavigateToWorkouts, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.List, null)
                Spacer(Modifier.width(8.dp))
                Text("Mijn Workouts")
            }

            OutlinedButton(onClick = onNavigateToExercises, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Oefeningen Zoeken")
            }
        }

        // Rechterkolom: statistieken
        StatsCard(
            totalWorkouts = totalWorkouts,
            completedWorkouts = completedWorkouts,
            daysActive = daysActive,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatsCard(
    totalWorkouts: Int,
    completedWorkouts: Int,
    daysActive: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "$totalWorkouts", label = "Workouts", color = MaterialTheme.colorScheme.primary)
            VerticalDivider(modifier = Modifier.height(60.dp))
            StatItem(value = "$completedWorkouts", label = "Voltooid", color = MaterialTheme.colorScheme.tertiary)
            VerticalDivider(modifier = Modifier.height(60.dp))
            StatItem(value = "$daysActive", label = "Dagen actief", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
