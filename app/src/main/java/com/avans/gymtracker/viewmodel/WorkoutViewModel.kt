package com.avans.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.avans.gymtracker.data.database.GymDatabase
import com.avans.gymtracker.data.model.Exercise
import com.avans.gymtracker.data.model.Workout
import com.avans.gymtracker.data.model.WorkoutExercise
import com.avans.gymtracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel voor workout CRUD operaties.
 * Alle database-aanroepen zijn asynchroon via viewModelScope.
 */
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(
        GymDatabase.getDatabase(application).workoutDao()
    )

    // ── Workouts lijst (Flow → StateFlow voor Compose) ─────────────────────────

    val workouts: StateFlow<List<Workout>> = repository.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── UI State ──────────────────────────────────────────────────────────────

    private val _currentWorkout = MutableStateFlow<Workout?>(null)
    val currentWorkout: StateFlow<Workout?> = _currentWorkout.asStateFlow()

    private val _currentExercises = MutableStateFlow<List<WorkoutExercise>>(emptyList())
    val currentExercises: StateFlow<List<WorkoutExercise>> = _currentExercises.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    // ── Workout laden ─────────────────────────────────────────────────────────

    fun loadWorkout(workoutId: Long) {
        viewModelScope.launch {
            _currentWorkout.value = repository.getWorkoutById(workoutId)
            repository.getExercisesForWorkout(workoutId).collect { exercises ->
                _currentExercises.value = exercises.sortedBy { it.orderIndex }
            }
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Maakt een nieuwe workout aan en voegt oefeningen toe.
     * @return het ID van de nieuwe workout
     */
    fun createWorkout(
        name: String,
        description: String,
        exercises: List<Exercise>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val workout = Workout(name = name, description = description)
                val workoutId = repository.insertWorkout(workout)

                val workoutExercises = exercises.mapIndexed { index, exercise ->
                    WorkoutExercise.fromExercise(exercise, workoutId, index)
                }
                repository.insertExercises(workoutExercises)

                onSuccess(workoutId)
            } finally {
                _isSaving.value = false
            }
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
            _currentWorkout.value = workout
        }
    }

    fun updateNotes(workoutId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateNotes(workoutId, notes)
        }
    }

    fun updateExercise(exercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.updateExercise(exercise)
        }
    }

    /**
     * Markeer workout als voltooid en sla tijdstip op.
     */
    fun markCompleted(workoutId: Long) {
        viewModelScope.launch {
            repository.markCompleted(workoutId)
            _currentWorkout.value = repository.getWorkoutById(workoutId)
        }
    }

    /**
     * Sla de URI op van de foto gemaakt met de camera intent.
     */
    fun savePhotoUri(workoutId: Long, uri: String?) {
        viewModelScope.launch {
            repository.updatePhotoUri(workoutId, uri)
            _currentWorkout.value = repository.getWorkoutById(workoutId)
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun deleteExerciseFromWorkout(exercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    // ── Hulp ──────────────────────────────────────────────────────────────────

    fun resetSaveSuccess() { _saveSuccess.value = false }
}
