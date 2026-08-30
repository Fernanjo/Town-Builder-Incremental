package com.cosytown.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cosytown.builder.data.GameStateRepository
import com.cosytown.engine.BuildingType
import com.cosytown.engine.GameEngine
import com.cosytown.engine.GameState
import com.cosytown.engine.LegacyUpgradeDefs
import com.cosytown.engine.LegacyUpgradeId
import com.cosytown.engine.PrestigeCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TICK_INTERVAL_MILLIS = 1000L

/**
 * How much time offline is simulated on resume. Chosen as a conservative vertical-slice default
 * per the design brief ("start conservative, tune after playtesting") -- flag for review.
 */
private const val MAX_OFFLINE_CATCH_UP_SECONDS = 8.0 * 60.0 * 60.0

private const val AUTOSAVE_EVERY_N_TICKS = 10

/** Holds the live [GameState], drives the 1s tick loop, and exposes player actions. */
class GameViewModel(private val repository: GameStateRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<GameState?>(null)
    val uiState: StateFlow<GameState?> = _uiState.asStateFlow()

    private var ticksSinceLastSave = 0

    init {
        viewModelScope.launch {
            val loaded = repository.gameState.first()
            val now = System.currentTimeMillis()
            val elapsedSeconds = ((now - loaded.lastTickEpochMillis) / 1000.0)
                .coerceIn(0.0, MAX_OFFLINE_CATCH_UP_SECONDS)
            val caughtUp = GameEngine.tick(loaded, elapsedSeconds, now)
            _uiState.value = caughtUp
            repository.save(caughtUp)
            runForegroundTicker()
        }
    }

    private suspend fun runForegroundTicker() {
        while (true) {
            delay(TICK_INTERVAL_MILLIS)
            val current = _uiState.value ?: continue
            val now = System.currentTimeMillis()
            val elapsedSeconds = (now - current.lastTickEpochMillis) / 1000.0
            val next = GameEngine.tick(current, elapsedSeconds, now)
            _uiState.value = next

            ticksSinceLastSave++
            if (ticksSinceLastSave >= AUTOSAVE_EVERY_N_TICKS) {
                ticksSinceLastSave = 0
                repository.save(next)
            }
        }
    }

    fun buyBuilding(type: BuildingType) = updateAndSave { GameEngine.buyBuilding(it, type) ?: it }

    fun buyLegacyUpgrade(id: LegacyUpgradeId) = updateAndSave { LegacyUpgradeDefs.buy(it, id) ?: it }

    fun prestige() = updateAndSave { state ->
        if (PrestigeCalculator.isPrestigeAvailable(state)) {
            PrestigeCalculator.prestige(state, System.currentTimeMillis())
        } else {
            state
        }
    }

    /** Flushes the current state to disk immediately, e.g. when the app goes to the background. */
    fun persistNow() {
        val current = _uiState.value ?: return
        viewModelScope.launch { repository.save(current) }
    }

    private fun updateAndSave(transform: (GameState) -> GameState) {
        val current = _uiState.value ?: return
        val next = transform(current)
        if (next === current) return
        _uiState.value = next
        viewModelScope.launch { repository.save(next) }
    }
}

class GameViewModelFactory(private val repository: GameStateRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = GameViewModel(repository) as T
}
