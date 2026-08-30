package com.cosytown.engine

import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Prestige eligibility and payout. Legacy Points use the geometric-mean formula so a lopsided run
 * (all population, no tech, or vice versa) earns noticeably less than a balanced one — this was
 * chosen over a simple additive formula specifically to reward playing both tracks.
 *
 * Thresholds and the LP divisor are conservative starting points per the design brief and are
 * expected to be tuned after playtesting.
 */
object PrestigeCalculator {

    const val MIN_PEAK_POPULATION_TO_PRESTIGE = 50.0
    const val MIN_PEAK_TECH_POINTS_TO_PRESTIGE = 30.0
    private const val LEGACY_POINTS_DIVISOR = 5.0

    fun isPrestigeAvailable(state: GameState): Boolean =
        state.runStats.peakPopulation >= MIN_PEAK_POPULATION_TO_PRESTIGE &&
            state.runStats.peakTechPoints >= MIN_PEAK_TECH_POINTS_TO_PRESTIGE

    /** Legacy Points that would be earned if the player prestiges right now. */
    fun legacyPointsPreview(state: GameState): Long {
        val product = state.runStats.peakPopulation * state.runStats.peakTechPoints
        if (product <= 0.0) return 0L
        return floor(sqrt(product) / LEGACY_POINTS_DIVISOR).toLong()
    }

    /** Returns a fresh [GameState] for the next run, carrying forward Legacy Points/upgrades. */
    fun prestige(state: GameState, nowEpochMillis: Long): GameState {
        val earned = legacyPointsPreview(state)
        val prestigeCount = state.runStats.prestigeCount + 1
        val carriedLegacy = state.legacy.copy(legacyPoints = state.legacy.legacyPoints + earned)
        return GameState.newRun(nowEpochMillis, carriedLegacy).let {
            it.copy(runStats = it.runStats.copy(prestigeCount = prestigeCount))
        }
    }
}
