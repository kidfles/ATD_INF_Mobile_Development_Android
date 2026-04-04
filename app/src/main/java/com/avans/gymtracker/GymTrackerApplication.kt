package com.avans.gymtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
/**
 * Application class - initialiseert Volley RequestQueue als singleton
 * en maakt Android notification channel aan.
 */
class GymTrackerApplication : Application() {

    // Volley RequestQueue als lazy singleton
    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Maakt het notification channel aan voor workout reminders (Android 8+).
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Workout Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Herinneringen voor je workouts"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "workout_reminders"
    }
}
