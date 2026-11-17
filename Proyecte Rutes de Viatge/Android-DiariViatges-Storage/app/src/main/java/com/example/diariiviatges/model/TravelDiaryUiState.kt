package com.example.diariiviatges.model

/**
 * Estat únic de la UI (UiState) del formulari i de l'historial.
 * La vista només observa aquest objecte i reacciona a les seves mutacions
 * mitjançant .copy() al ViewModel (patró MVVM de l'enunciat).
 */
data class TravelDiaryUiState(
    // --- Camps del formulari ---
    val routeName: String = "",
    val routeNameError: String? = null,
    val destination: String = "",
    val destinationError: String? = null,
    val difficulty: Difficulty = Difficulty.FACIL,
    // El pressupost es manté com a String al formulari i es converteix a Double en desar-lo.
    val estimatedBudget: String = "",
    val budgetError: String? = null,
    val date: String = "",
    val isSolo: Boolean = false,
    val notes: String = "",

    // --- Validació ---
    val isFormValid: Boolean = false,

    // --- Historial personal (emmagatzematge intern, filesDir) ---
    val history: List<RouteEntry> = emptyList(),

    // --- Ruta acabada de crear o seleccionada: es passa a PostalScreen ---
    val selectedEntry: RouteEntry? = null
)
