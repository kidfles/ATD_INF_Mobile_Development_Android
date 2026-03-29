package com.avans.gymtracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.avans.gymtracker.data.model.Workout
import com.avans.gymtracker.data.model.WorkoutExercise

/**
 * Room database configuratie.
 * Singleton patroon om meerdere instanties te voorkomen.
 */
@Database(
    entities = [Workout::class, WorkoutExercise::class],
    version = 1,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
