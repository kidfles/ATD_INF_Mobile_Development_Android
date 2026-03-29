package com.avans.gymtracker.data.model

import org.json.JSONObject

/**
 * Exercise model - data van ExerciseDB API.
 * Zelfde velden als iOS versie.
 */
data class Exercise(
    val id: String,
    val name: String,
    val target: String,
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    val instructions: List<String>,
    val secondaryMuscles: List<String>
) {
    companion object {
        /**
         * Parst een Exercise uit een JSON object van de API.
         */
        fun fromJson(json: JSONObject): Exercise {
            val instructionsList = mutableListOf<String>()
            val instructionsArray = json.optJSONArray("instructions")
            if (instructionsArray != null) {
                for (i in 0 until instructionsArray.length()) {
                    instructionsList.add(instructionsArray.getString(i))
                }
            }

            val secondaryMusclesList = mutableListOf<String>()
            val secondaryArray = json.optJSONArray("secondaryMuscles")
            if (secondaryArray != null) {
                for (i in 0 until secondaryArray.length()) {
                    secondaryMusclesList.add(secondaryArray.getString(i))
                }
            }

            return Exercise(
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                target = json.optString("target", ""),
                bodyPart = json.optString("bodyPart", ""),
                equipment = json.optString("equipment", ""),
                gifUrl = json.optString("gifUrl", ""),
                instructions = instructionsList,
                secondaryMuscles = secondaryMusclesList
            )
        }
    }
}

/**
 * Body part categorieën voor filtering - zelfde als iOS versie.
 */
enum class BodyPart(val apiValue: String, val displayName: String, val emoji: String) {
    BACK("back", "Rug", "🏋️"),
    CARDIO("cardio", "Cardio", "❤️"),
    CHEST("chest", "Borst", "💪"),
    LOWER_ARMS("lower arms", "Onderarmen", "🤜"),
    LOWER_LEGS("lower legs", "Onderbenen", "🦵"),
    NECK("neck", "Nek", "🧘"),
    SHOULDERS("shoulders", "Schouders", "🏊"),
    UPPER_ARMS("upper arms", "Bovenarmen", "💪"),
    UPPER_LEGS("upper legs", "Bovenbenen", "🦿"),
    WAIST("waist", "Core/Buik", "🎯");

    companion object {
        fun fromApiValue(value: String): BodyPart =
            entries.firstOrNull { it.apiValue == value } ?: CHEST
    }
}
