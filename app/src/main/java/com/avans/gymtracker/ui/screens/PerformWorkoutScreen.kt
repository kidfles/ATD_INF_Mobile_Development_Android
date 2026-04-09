package com.avans.gymtracker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

/**
 * Live workout uitvoerscherm met timer.
 * Equivalent van PerformWorkoutView in iOS.
 *
 * Features:
 * - Stopwatch timer die elke seconde bijwerkt (asynchroon via LaunchedEffect)
 * - Oefening-voor-oefening voortgang
 * - Progressbar
 * - Voltooiing met notificatie
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformWorkoutScreen(
    workoutId: Long,
    onFinished: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    val workout by workoutViewModel.currentWorkout.collectAsStateWithLifecycle()
    val exercises by workoutViewModel.currentExercises.collectAsStateWithLifecycle()

    var currentIndex by remember { mutableIntStateOf(0) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    var pendingWorkoutName by remember { mutableStateOf("") }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            sendCompletionNotification(context, pendingWorkoutName)
        }
        onFinished()
    }

    fun sendNotificationWithPermission(workoutName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED -> {
                    sendCompletionNotification(context, workoutName)
                    onFinished()
                }
                else -> {
                    pendingWorkoutName = workoutName
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            sendCompletionNotification(context, workoutName)
            onFinished()
        }
    }

    // Timer — asynchroon, elke seconde
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Laad workout data
    LaunchedEffect(workoutId) {
        workoutViewModel.loadWorkout(workoutId)
    }

    val sortedExercises = remember(exercises) { exercises.sortedBy { it.orderIndex } }
    val currentExercise = sortedExercises.getOrNull(currentIndex)
    val progress = if (sortedExercises.isEmpty()) 0f
                   else (currentIndex + 1).toFloat() / sortedExercises.size.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout bezig") },
                navigationIcon = {
                    IconButton(onClick = { isRunning = false; onFinished() }) {
                        Icon(Icons.Default.Close, "Stoppen")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Timer ──────────────────────────────────────────────────────────
            Text(
                text = formatElapsedTime(elapsedSeconds),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // ── Voortgang ──────────────────────────────────────────────────────
            Text(
                "Oefening ${currentIndex + 1} van ${sortedExercises.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // ── Huidige oefening ───────────────────────────────────────────────
            if (currentExercise != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            currentExercise.exerciseName.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            currentExercise.targetMuscle.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Sets × reps info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${currentExercise.sets}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("sets", style = MaterialTheme.typography.labelMedium)
                        }
                        VerticalDivider(modifier = Modifier.height(48.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${currentExercise.reps}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("reps", style = MaterialTheme.typography.labelMedium)
                        }
                        if (currentExercise.weightKg > 0) {
                            VerticalDivider(modifier = Modifier.height(48.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${"%.1f".format(currentExercise.weightKg)}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("kg", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Navigatieknoppen ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vorige knop
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { currentIndex-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Vorige")
                    }
                }

                // Volgende / Voltooien knop
                Button(
                    onClick = {
                        if (currentIndex < sortedExercises.size - 1) {
                            currentIndex++
                        } else {
                            isRunning = false
                            showCompletionDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (currentIndex < sortedExercises.size - 1) "Volgende" else "Voltooien 🎉"
                    )
                    if (currentIndex < sortedExercises.size - 1) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            }
        }
    }

    // ── Voltooiingsdialoog ─────────────────────────────────────────────────────
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    Icons.Default.EmojiEvents,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Workout voltooid! 🎉") },
            text = {
                Column {
                    Text("Goed gedaan! Je hebt de workout afgerond.")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tijd: ${formatElapsedTime(elapsedSeconds)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    workoutViewModel.markCompleted(workoutId)
                    sendNotificationWithPermission(workout?.name ?: "")
                }) {
                    Text("Afronden")
                }
            }
        )
    }
}

/** Formatteert seconden naar mm:ss notatie */
private fun formatElapsedTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
