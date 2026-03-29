package com.avans.gymtracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Workout entity - lokale opslag via Room (equivalent van SwiftData Workout).
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdDate: Long = System.currentTimeMillis(),
    val lastPerformed: Long? = null,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val photoUri: String? = null  // Foto genomen via camera intent
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(createdDate))
        }

    val formattedLastPerformed: String?
        get() = lastPerformed?.let {
            val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
            sdf.format(Date(it))
        }
}

/**
 * WorkoutExercise entity - een oefening binnen een workout.
 * Gekoppeld aan Workout via foreign key.
 */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE  // Verwijder exercises als workout wordt verwijderd
        )
    ],
    indices = [Index("workoutId")]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val targetMuscle: String,
    val equipment: String,
    val sets: Int = 3,
    val reps: Int = 10,
    val weightKg: Double = 0.0,
    val notes: String = "",
    val orderIndex: Int = 0
) {
    companion object {
        fun fromExercise(exercise: Exercise, workoutId: Long, orderIndex: Int = 0): WorkoutExercise {
            return WorkoutExercise(
                workoutId = workoutId,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                targetMuscle = exercise.target,
                equipment = exercise.equipment,
                orderIndex = orderIndex
            )
        }
    }
}

/**
 * Data class voor Workout + bijbehorende exercises (voor Room JOIN query).
 */
data class WorkoutWithExercises(
    val workout: Workout,
    val exercises: List<WorkoutExercise>
) {
    val totalExercises: Int get() = exercises.size
}
