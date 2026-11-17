package com.example.diariiviatges.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diariiviatges.data.StorageRepository
import com.example.diariiviatges.model.Difficulty
import com.example.diariiviatges.model.RouteEntry
import com.example.diariiviatges.model.TravelDiaryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * VIEWMODEL: conserva l'estat reactiu i conté tota la lògica de validació i persistència.
 * La UI no accedeix mai directament a les dades: només crida mètodes i observa uiState.
 */
class TravelDiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StorageRepository(application.applicationContext)

    // MutableStateFlow privat + StateFlow públic amb .asStateFlow()
    private val _uiState = MutableStateFlow(TravelDiaryUiState())
    val uiState: StateFlow<TravelDiaryUiState> = _uiState.asStateFlow()

    init {
        // En obrir l'app, recuperem l'historial privat de filesDir.
        viewModelScope.launch {
            val history = repository.loadHistory()
            // FIX: antes se hacía copy(history = history) y se pisaba la lista. Si el usuario
            // creaba una ruta antes de que acabase la lectura del disco, esa ruta se perdía.
            // Ahora se ponen primero las del disco y después las ya creadas en memoria.
            _uiState.update { it.copy(history = history + it.history) }
        }
    }

    // ---------------------------------------------------------------------
    // Esdeveniments del formulari (validació en temps real amb .copy())
    // ---------------------------------------------------------------------

    fun onRouteNameChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                routeName = value,
                routeNameError = if (value.isBlank()) "El nom de la ruta no pot estar buit" else null
            ).withValidity()
        }
    }

    fun onDestinationChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                destination = value,
                destinationError = if (value.isBlank()) "El destí no pot estar buit" else null
            ).withValidity()
        }
    }

    fun onDifficultySelected(value: Difficulty) {
        _uiState.update { it.copy(difficulty = value) }
    }

    fun onBudgetChanged(value: String) {
        _uiState.update { state ->
            val amount = value.toBudgetOrNull()
            state.copy(
                estimatedBudget = value,
                budgetError = when {
                    value.isBlank() -> "El pressupost no pot estar buit"
                    amount == null -> "El pressupost ha de ser un número vàlid"
                    amount < 0 -> "El pressupost no pot ser negatiu"
                    else -> null
                }
            ).withValidity()
        }
    }

    fun onDateChanged(value: String) {
        _uiState.update { it.copy(date = value) }
    }

    fun onSoloChanged(value: Boolean) {
        _uiState.update { it.copy(isSolo = value) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    // ---------------------------------------------------------------------
    // Accions principals
    // ---------------------------------------------------------------------

    /** Crea la RouteEntry, l'afegeix a l'historial i la deixa llesta per a PostalScreen. */
    fun onCreatePostal() {
        val state = _uiState.value
        if (!state.isFormValid) return

        val entry = RouteEntry(
            routeName = state.routeName.trim(),
            destination = state.destination.trim(),
            difficulty = state.difficulty,
            estimatedBudget = state.estimatedBudget.toBudgetOrNull() ?: 0.0,
            date = state.date,
            isSolo = state.isSolo,
            notes = state.notes
        )
        val updatedHistory = state.history + entry

        // L'estat s'actualitza de forma sincrònica perquè la navegació trobi la dada.
        _uiState.update { it.copy(selectedEntry = entry, history = updatedHistory) }
        // La E/S al disc es fa en segon pla gràcies a withContext(Dispatchers.IO) del repositori.
        viewModelScope.launch { repository.saveHistory(updatedHistory) }
    }

    /** Selecciona una ruta de l'historial per visualitzar-la com a postal. */
    fun onEntrySelected(entry: RouteEntry) {
        _uiState.update { it.copy(selectedEntry = entry) }
    }

    /** Exporta la crònica seleccionada a la Uri retornada pel selector del sistema. */
    fun exportSelectedEntry(uri: Uri) {
        val entry = _uiState.value.selectedEntry ?: return
        val content = repository.formatEntry(entry)
        viewModelScope.launch { repository.writeToUri(uri, content) }
    }

    // ---------------------------------------------------------------------
    // Regles de validació
    // ---------------------------------------------------------------------

    /** Recalcula isFormValid a partir dels errors Actualitzats. */
    private fun TravelDiaryUiState.withValidity(): TravelDiaryUiState =
        copy(
            isFormValid = routeNameError == null &&
                destinationError == null &&
                budgetError == null &&
                routeName.isNotBlank() &&
                destination.isNotBlank() &&
                (estimatedBudget.toBudgetOrNull() ?: -1.0) >= 0.0
        )

    /**
     * FIX: convierte el texto del pressupost a Double aceptando coma decimal ("12,5").
     * toDoubleOrNull() solo entiende el punto, y en catalán/español se escribe con coma,
     * así que un valor correcto salía como "número no válido".
     */
    private fun String.toBudgetOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()
}
