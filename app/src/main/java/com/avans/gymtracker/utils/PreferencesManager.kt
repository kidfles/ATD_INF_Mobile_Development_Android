package com.avans.gymtracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.avans.gymtracker.data.model.BodyPart

/**
 * Wrapper om SharedPreferences voor gebruikersinstellingen.
 * Equivalent van @AppStorage in iOS.
 *
 * Bevat:
 * - Login status en gebruikersnaam
 * - Voorkeur body part voor exercises
 * - Gewichtseenheid (kg of lbs)
 * - Notificaties aan/uit
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Login ─────────────────────────────────────────────────────────────────

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    // ── Instellingen ──────────────────────────────────────────────────────────

    /**
     * Voorkeur body part (opgeslagen als API-waarde string).
     */
    var preferredBodyPart: BodyPart
        get() {
            val value = prefs.getString(KEY_BODY_PART, BodyPart.CHEST.apiValue) ?: BodyPart.CHEST.apiValue
            return BodyPart.fromApiValue(value)
        }
        set(value) = prefs.edit().putString(KEY_BODY_PART, value.apiValue).apply()

    /**
     * Gewichtseenheid: true = kg, false = lbs.
     */
    var useKilograms: Boolean
        get() = prefs.getBoolean(KEY_USE_KG, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_KG, value).apply()

    /**
     * Workout notificaties ingeschakeld.
     */
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    /**
     * Aantal oefeningen per laadactie (voor pagination).
     */
    var exercisesPerPage: Int
        get() = prefs.getInt(KEY_PAGE_SIZE, 20)
        set(value) = prefs.edit().putInt(KEY_PAGE_SIZE, value).apply()

    // ── Uitloggen ─────────────────────────────────────────────────────────────

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "gym_tracker_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_BODY_PART = "preferred_body_part"
        private const val KEY_USE_KG = "use_kilograms"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_PAGE_SIZE = "exercises_per_page"
    }
}
