package com.avans.gymtracker.data.repository

import com.avans.gymtracker.data.database.WorkoutDao
import com.avans.gymtracker.data.model.Workout
import com.avans.gymtracker.data.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

/**
 * Repository laag tussen DAO en ViewModels.
 * Centraliseert alle data-operaties en abstracteert de database-details weg.
 */
class WorkoutRepository(private val dao: WorkoutDao) {

    // ── Workouts ──────────────────────────────────────────────────────────────

    fun getAllWorkouts(): Flow<List<Workout>> = dao.getAllWorkouts()

    suspend fun getWorkoutById(id: Long): Workout? = dao.getWorkoutById(id)

    suspend fun insertWorkout(workout: Workout): Long = dao.insertWorkout(workout)

    suspend fun updateWorkout(workout: Workout) = dao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: Workout) = dao.deleteWorkout(workout)

    suspend fun updateNotes(workoutId: Long, notes: String) =
        dao.updateNotes(workoutId, notes)

    suspend fun markCompleted(workoutId: Long) =
        dao.markWorkoutCompleted(workoutId, true, System.currentTimeMillis())

    suspend fun updatePhotoUri(workoutId: Long, uri: String?) =
        dao.updatePhotoUri(workoutId, uri)

    // ── WorkoutExercises ──────────────────────────────────────────────────────

    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExercise>> =
        dao.getExercisesForWorkout(workoutId)

    suspend fun getExercisesOnce(workoutId: Long): List<WorkoutExercise> =
        dao.getExercisesForWorkoutOnce(workoutId)

    suspend fun insertExercises(exercises: List<WorkoutExercise>) =
        dao.insertExercises(exercises)

    suspend fun updateExercise(exercise: WorkoutExercise) =
        dao.updateExercise(exercise)

    suspend fun deleteExercise(exercise: WorkoutExercise) =
        dao.deleteExercise(exercise)

    suspend fun deleteAllExercisesForWorkout(workoutId: Long) =
        dao.deleteAllExercisesForWorkout(workoutId)
}
