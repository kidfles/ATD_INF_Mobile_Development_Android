package com.avans.gymtracker.data.database

import androidx.room.*
import com.avans.gymtracker.data.model.Workout
import com.avans.gymtracker.data.model.WorkoutExercise
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object voor workout CRUD operaties.
 * Alle database operaties zijn suspend functies (asynchroon, multithreaded).
 */
@Dao
interface WorkoutDao {

    // ── Workout queries ──────────────────────────────────────────────────────

    @Query("SELECT * FROM workouts ORDER BY createdDate DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): Workout?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("UPDATE workouts SET notes = :notes WHERE id = :workoutId")
    suspend fun updateNotes(workoutId: Long, notes: String)

    @Query("UPDATE workouts SET isCompleted = :completed, lastPerformed = :timestamp WHERE id = :workoutId")
    suspend fun markWorkoutCompleted(workoutId: Long, completed: Boolean, timestamp: Long)

    @Query("UPDATE workouts SET photoUri = :uri WHERE id = :workoutId")
    suspend fun updatePhotoUri(workoutId: Long, uri: String?)

    // ── WorkoutExercise queries ──────────────────────────────────────────────

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    fun getExercisesForWorkout(workoutId: Long): Flow<List<WorkoutExercise>>

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex ASC")
    suspend fun getExercisesForWorkoutOnce(workoutId: Long): List<WorkoutExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: WorkoutExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<WorkoutExercise>)

    @Update
    suspend fun updateExercise(exercise: WorkoutExercise)

    @Delete
    suspend fun deleteExercise(exercise: WorkoutExercise)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteAllExercisesForWorkout(workoutId: Long)

    @Query("SELECT COUNT(*) FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun getExerciseCount(workoutId: Long): Int
}
