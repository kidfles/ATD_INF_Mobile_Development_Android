package com.avans.gymtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.avans.gymtracker.data.model.BodyPart
import com.avans.gymtracker.data.model.Exercise
import com.avans.gymtracker.network.ExerciseApiService
import com.avans.gymtracker.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel voor het ExercisesScreen.
 * Beheert de staat van oefeningen opgehaald van de API.
 * Asynchroon via viewModelScope + coroutines (multithreaded).
 */
class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ExerciseApiService(application)
    private val prefs = PreferencesManager(application)

    // ── UI State ──────────────────────────────────────────────────────────────

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedBodyPart = MutableStateFlow(prefs.preferredBodyPart)
    val selectedBodyPart: StateFlow<BodyPart> = _selectedBodyPart.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ── Pagination ────────────────────────────────────────────────────────────

    private var currentOffset = 0
    private val pageSize get() = prefs.exercisesPerPage
    private var hasMorePages = true

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        fetchExercises(prefs.preferredBodyPart)
    }

    // ── Publieke methodes ──────────────────────────────────────────────────────

    /**
     * Selecteer een nieuwe body part en laad de bijbehorende oefeningen.
     * Reset pagination.
     */
    fun selectBodyPart(bodyPart: BodyPart) {
        _selectedBodyPart.value = bodyPart
        _searchQuery.value = ""
        prefs.preferredBodyPart = bodyPart
        fetchExercises(bodyPart)
    }

    /**
     * Zoek oefeningen op naam.
     */
    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            fetchExercises(_selectedBodyPart.value)
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _exercises.value = apiService.searchByName(query, limit = pageSize)
                hasMorePages = false // Zoekresultaten hebben geen pagination
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Laad de volgende pagina oefeningen (endless scroll).
     * Wordt aangeroepen vanuit het scherm als de gebruiker bijna onderaan scrollt.
     */
    fun loadNextPage() {
        if (_isLoadingMore.value || !hasMorePages || _searchQuery.value.isNotBlank()) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            try {
                val newExercises = apiService.fetchByBodyPart(
                    bodyPart = _selectedBodyPart.value.apiValue,
                    limit = pageSize,
                    offset = currentOffset
                )
                if (newExercises.isEmpty()) {
                    hasMorePages = false
                } else {
                    _exercises.value = _exercises.value + newExercises
                    currentOffset += newExercises.size
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    // ── Prive methodes ────────────────────────────────────────────────────────

    private fun fetchExercises(bodyPart: BodyPart) {
        currentOffset = 0
        hasMorePages = true
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = apiService.fetchByBodyPart(
                    bodyPart = bodyPart.apiValue,
                    limit = pageSize,
                    offset = 0
                )
                _exercises.value = result
                currentOffset = result.size
                hasMorePages = result.size == pageSize
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _exercises.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
