package com.avans.gymtracker.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.avans.gymtracker.GymTrackerApplication
import com.avans.gymtracker.data.model.Exercise
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Netwerk service voor ExerciseDB API via Volley.
 * Zelfde API en key als in de iOS versie.
 *
 * Alle methodes zijn suspend functions zodat ze veilig
 * vanuit een coroutine/ViewModel aangeroepen kunnen worden.
 * Volley callbacks worden omgezet via suspendCancellableCoroutine.
 */
class ExerciseApiService(private val context: Context) {

    private val requestQueue get() =
        (context.applicationContext as GymTrackerApplication).requestQueue

    companion object {
        private const val BASE_URL = "https://exercisedb.p.rapidapi.com"
        private const val API_KEY = "73cb6614e3msh69702632d4e793ap1404cbjsn6e213d128110"
        private const val API_HOST = "exercisedb.p.rapidapi.com"
    }

    /**
     * Haalt oefeningen op per body part.
     * @param bodyPart API-waarde (bijv. "chest", "back")
     * @param limit    Aantal resultaten
     * @param offset   Voor pagination (endless scroll)
     */
    suspend fun fetchByBodyPart(
        bodyPart: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<Exercise> {
        val encodedPart = bodyPart.replace(" ", "%20")
        val url = "$BASE_URL/exercises/bodyPart/$encodedPart?limit=$limit&offset=$offset"
        return performRequest(url)
    }

    /**
     * Zoekt oefeningen op naam.
     */
    suspend fun searchByName(name: String, limit: Int = 20): List<Exercise> {
        val encoded = name.replace(" ", "%20")
        val url = "$BASE_URL/exercises/name/$encoded?limit=$limit"
        return performRequest(url)
    }

    /**
     * Haalt alle oefeningen op (met limit + offset voor pagination).
     */
    suspend fun fetchAll(limit: Int = 20, offset: Int = 0): List<Exercise> {
        val url = "$BASE_URL/exercises?limit=$limit&offset=$offset"
        return performRequest(url)
    }

    /**
     * Voert een Volley JsonArrayRequest uit en converteert de callback
     * naar een suspend function via suspendCancellableCoroutine.
     */
    private suspend fun performRequest(url: String): List<Exercise> =
        suspendCancellableCoroutine { continuation ->
            val request = object : JsonArrayRequest(
                Method.GET, url, null,
                { jsonArray ->
                    try {
                        val exercises = mutableListOf<Exercise>()
                        for (i in 0 until jsonArray.length()) {
                            exercises.add(Exercise.fromJson(jsonArray.getJSONObject(i)))
                        }
                        continuation.resume(exercises)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                },
                { error ->
                    continuation.resumeWithException(
                        Exception("Netwerk fout: ${error.message ?: "Onbekende fout"}")
                    )
                }
            ) {
                // RapidAPI headers toevoegen aan elk verzoek
                override fun getHeaders(): Map<String, String> = mapOf(
                    "X-RapidAPI-Key" to API_KEY,
                    "X-RapidAPI-Host" to API_HOST,
                    "Accept" to "application/json"
                )
            }

            // Annuleer Volley request als de coroutine gecanceld wordt
            continuation.invokeOnCancellation {
                request.cancel()
            }

            requestQueue.add(request)
        }
}
