package io.github.jamesmyatt.randomatizer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.jamesmyatt.randomatizer.RandomatizerApp
import io.github.jamesmyatt.randomatizer.dice.DiceRoller
import io.github.jamesmyatt.randomatizer.dice.SecureRandomSource
import io.github.jamesmyatt.randomatizer.dice.StandardDie
import io.github.jamesmyatt.randomatizer.history.HistoryEntry
import io.github.jamesmyatt.randomatizer.history.RollHistory
import io.github.jamesmyatt.randomatizer.settings.AppSettings
import io.github.jamesmyatt.randomatizer.settings.Mode
import io.github.jamesmyatt.randomatizer.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RollerUiState(
    val settings: AppSettings,
    val current: HistoryEntry?,
    val history: List<HistoryEntry>,
)

/**
 * Holds the current roll and the session history in memory only.
 *
 * History deliberately does not use SavedStateHandle: it must never be written to storage.
 */
class RollerViewModel(
    private val settingsRepository: SettingsRepository,
    private val roller: DiceRoller,
) : ViewModel() {
    private data class Session(
        val current: HistoryEntry? = null,
        val history: RollHistory = RollHistory(),
        val nextId: Long = 0,
    )

    private val session = MutableStateFlow(Session())

    /** Null until settings have loaded. */
    val uiState: StateFlow<RollerUiState?> = combine(settingsRepository.settings, session) { settings, session ->
        RollerUiState(settings, session.current, session.history.entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun roll() {
        val settings = uiState.value?.settings ?: return
        val dice = when (settings.mode) {
            Mode.Basic -> List(settings.basicCount) { StandardDie.D6 }
            Mode.Advanced -> settings.advancedSelection.dice()
        }
        val roll = roller.roll(dice)
        session.update {
            val entry = HistoryEntry(it.nextId, roll, settings.mode)
            it.copy(current = entry, history = it.history + entry, nextId = it.nextId + 1)
        }
    }

    fun clearHistory() {
        session.update { it.copy(history = it.history.cleared()) }
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RandomatizerApp
                RollerViewModel(app.settingsRepository, DiceRoller(SecureRandomSource()))
            }
        }
    }
}
