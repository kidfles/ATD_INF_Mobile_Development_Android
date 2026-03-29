package com.avans.gymtracker.ui.screens

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avans.gymtracker.GymTrackerApplication
import com.avans.gymtracker.data.model.Workout
import com.avans.gymtracker.data.model.WorkoutExercise
import com.avans.gymtracker.viewmodel.WorkoutViewModel
import java.io.File

/**
 * Workout detail scherm — UPDATE en bewerking.
 *
 * Requirements gedekt:
 * - UPDATE (sets/reps, notities, naam) ✓
 * - Implicit Intent: camera → workout foto ophalen ✓
 * - Dangerous permission: CAMERA runtime verzoek ✓
 * - Notificatie bij voltooien van workout ✓
 * - Bewerking: oefening verwijderen, sets/reps aanpassen ✓
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onBack: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    val workout by workoutViewModel.currentWorkout.collectAsStateWithLifecycle()
    val exercises by workoutViewModel.currentExercises.collectAsStateWithLifecycle()

    var notes by remember { mutableStateOf("") }
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }

    // Foto URI voor camera intent
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // ── Camera Implicit Intent ────────────────────────────────────────────────
    // Maakt een tijdelijk bestand aan en haalt foto op uit de camera-app
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            workoutViewModel.savePhotoUri(workoutId, photoUri.toString())
        }
    }

    // Gallerij als alternatief (ook Implicit Intent)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { workoutViewModel.savePhotoUri(workoutId, it.toString()) }
    }

    // ── CAMERA dangerous permission launcher ──────────────────────────────────
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Toestemming verleend → open camera
            val tmpFile = createImageFile(context)
            photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tmpFile)
            cameraLauncher.launch(photoUri!!)
        } else {
            showCameraPermissionDialog = true
        }
    }

    fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                // Toestemming al verleend
                val tmpFile = createImageFile(context)
                photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tmpFile)
                cameraLauncher.launch(photoUri!!)
            }
            else -> {
                // Vraag toestemming — runtime dangerous permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Laad workout data
    LaunchedEffect(workoutId) {
        workoutViewModel.loadWorkout(workoutId)
    }

    // Synchroniseer notities met workout
    LaunchedEffect(workout) {
        workout?.let {
            notes = it.notes
            editedName = it.name
            editedDescription = it.description
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(workout?.name ?: "Workout")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
                    }
                },
                actions = {
                    if (isEditingName) {
                        IconButton(onClick = {
                            workout?.let {
                                workoutViewModel.updateWorkout(
                                    it.copy(name = editedName.trim(), description = editedDescription.trim())
                                )
                            }
                            isEditingName = false
                        }) { Icon(Icons.Default.Check, "Opslaan") }
                    } else {
                        IconButton(onClick = { isEditingName = true }) {
                            Icon(Icons.Default.Edit, "Bewerken")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (workout == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val w = workout!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Workout info kaart ─────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("📅 Aangemaakt: ${w.formattedDate}", style = MaterialTheme.typography.labelMedium)
                            w.formattedLastPerformed?.let {
                                Text("⏱ Laatste keer: $it", style = MaterialTheme.typography.labelMedium)
                            }
                            Text(
                                "🏋️ ${exercises.size} oefeningen",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (w.isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    if (w.description.isNotBlank()) {
                        Text(w.description, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Actieknoppen ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Workout starten / voltooien
                Button(
                    onClick = { showCompletionDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = if (w.isCompleted)
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    else
                        ButtonDefaults.buttonColors()
                ) {
                    Icon(
                        if (w.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        null
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (w.isCompleted) "Voltooid!" else "Start workout")
                }

                // Camera knop — IMPLICIT INTENT + DANGEROUS PERMISSION
                OutlinedButton(onClick = ::launchCamera) {
                    Icon(Icons.Default.CameraAlt, "Foto maken")
                }

                // Gallerij knop — IMPLICIT INTENT (GET_CONTENT)
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Photo, "Gallerij")
                }
            }

            // Toon foto als die aanwezig is
            if (w.photoUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Foto opgeslagen",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { workoutViewModel.savePhotoUri(workoutId, null) }) {
                            Text("Verwijder")
                        }
                    }
                }
            }

            // ── Oefeningen ────────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Oefeningen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()

                    if (exercises.isEmpty()) {
                        Text(
                            "Geen oefeningen toegevoegd",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        exercises.forEach { exercise ->
                            WorkoutExerciseItem(
                                exercise = exercise,
                                onUpdate = { workoutViewModel.updateExercise(it) },
                                onDelete = { workoutViewModel.deleteExerciseFromWorkout(it) }
                            )
                        }
                    }
                }
            }

            // ── Notities ──────────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Notities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                            workoutViewModel.updateNotes(workoutId, it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("Voeg notities toe...") }
                    )
                }
            }
        }
    }

    // ── Voltooiingsdialoog ──────────────────────────────────────────────────
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            icon = { Icon(Icons.Default.EmojiEvents, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Workout voltooien?") },
            text = { Text("Wil je deze workout markeren als voltooid?") },
            confirmButton = {
                Button(onClick = {
                    workoutViewModel.markCompleted(workoutId)
                    sendCompletionNotification(context, workout?.name ?: "")
                    showCompletionDialog = false
                }) { Text("Voltooien 🎉") }
            },
            dismissButton = {
                TextButton(onClick = { showCompletionDialog = false }) { Text("Annuleer") }
            }
        )
    }

    // ── Camera-permissie geweigerd dialoog ───────────────────────────────────
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera toestemming nodig") },
            text = { Text("Ga naar Instellingen > Apps > GymTracker > Toestemmingen om camera toegang in te schakelen.") },
            confirmButton = {
                Button(onClick = {
                    showCameraPermissionDialog = false
                    // Open app settings via implicit intent
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) { Text("Instellingen openen") }
            },
            dismissButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) { Text("Annuleer") }
            }
        )
    }
}

/**
 * Rij voor één oefening binnen de workout detail.
 * Bewerking: sets, reps en gewicht aanpassen + verwijderen.
 */
@Composable
private fun WorkoutExerciseItem(
    exercise: WorkoutExercise,
    onUpdate: (WorkoutExercise) -> Unit,
    onDelete: (WorkoutExercise) -> Unit
) {
    var sets by remember(exercise.id) { mutableIntStateOf(exercise.sets) }
    var reps by remember(exercise.id) { mutableIntStateOf(exercise.reps) }
    var weight by remember(exercise.id) { mutableDoubleStateOf(exercise.weightKg) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${exercise.orderIndex + 1}.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.exerciseName.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        exercise.targetMuscle.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "Details"
                    )
                }
                IconButton(onClick = { onDelete(exercise) }) {
                    Icon(Icons.Default.Delete, "Verwijderen", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Compacte weergave van sets × reps
            Text(
                "$sets sets × $reps reps${if (weight > 0) " @ ${"%.1f".format(weight)} kg" else ""}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp)
            )

            // Uitklapbare bewerkingssectie — BEWERKING op lijstitem
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Sets stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sets:", modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { if (sets > 1) { sets--; onUpdate(exercise.copy(sets = sets)) } }) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text("$sets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (sets < 10) { sets++; onUpdate(exercise.copy(sets = sets)) } }) {
                        Icon(Icons.Default.Add, null)
                    }
                }

                // Reps stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reps:", modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { if (reps > 1) { reps--; onUpdate(exercise.copy(reps = reps)) } }) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text("$reps", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (reps < 50) { reps++; onUpdate(exercise.copy(reps = reps)) } }) {
                        Icon(Icons.Default.Add, null)
                    }
                }

                // Gewicht stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Gewicht:", modifier = Modifier.width(72.dp), style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { if (weight >= 2.5) { weight -= 2.5; onUpdate(exercise.copy(weightKg = weight)) } }) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text("${"%.1f".format(weight)} kg", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { weight += 2.5; onUpdate(exercise.copy(weightKg = weight)) }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        }
    }
}

/** Maakt een tijdelijk afbeeldingsbestand aan voor de camera intent. */
private fun createImageFile(context: Context): File {
    val storageDir = context.externalCacheDir ?: context.cacheDir
    return File.createTempFile("workout_", ".jpg", storageDir)
}

/** Stuurt een lokale notificatie na voltooiing van de workout. */
private fun sendCompletionNotification(context: Context, workoutName: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notification = NotificationCompat.Builder(context, GymTrackerApplication.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("🎉 Workout voltooid!")
        .setContentText("Goed gedaan! Je hebt '$workoutName' afgerond.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(workoutName.hashCode(), notification)
}
