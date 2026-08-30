package com.cosytown.engine

import kotlinx.serialization.Serializable

/** Root save state for a single player. Everything here is either reset or carried across Prestige. */
@Serializable
data class GameState(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val lastTickEpochMillis: Long = 0L,
    val resources: ResourceState = ResourceState(),
    val buildings: Map<BuildingType, Int> = emptyMap(),
    val population: PopulationState = PopulationState(),
    val technology: TechnologyState = TechnologyState(),
    val legacy: LegacyState = LegacyState(),
    val runStats: RunStats = RunStats(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1

        /** A brand new save, as used on first install and after every Prestige. */
        fun newRun(nowEpochMillis: Long, legacy: LegacyState = LegacyState()): GameState {
            val effects = LegacyUpgradeDefs.effectsFor(legacy.purchasedLevels)
            return GameState(
                lastTickEpochMillis = nowEpochMillis,
                resources = ResourceState(
                    coins = BASE_STARTING_COINS + effects.startingCoinsBonus,
                    materials = BASE_STARTING_MATERIALS + effects.startingMaterialsBonus,
                    energy = 0.0,
                ),
                legacy = legacy,
                runStats = RunStats(runStartEpochMillis = nowEpochMillis),
            )
        }

        private const val BASE_STARTING_COINS = 50.0
        private const val BASE_STARTING_MATERIALS = 30.0
    }
}

/** Spendable/storable resource pools. Happiness is deliberately absent here: it is a derived multiplier, not a pool. */
@Serializable
data class ResourceState(
    val coins: Double = 0.0,
    val materials: Double = 0.0,
    val energy: Double = 0.0,
)

@Serializable
data class PopulationState(
    val current: Double = 0.0,
)

@Serializable
data class TechnologyState(
    val points: Double = 0.0,
)

@Serializable
data class LegacyState(
    val legacyPoints: Long = 0,
    val purchasedLevels: Map<LegacyUpgradeId, Int> = emptyMap(),
)

/** Tracks the high-water marks used to compute the Legacy Point payout on the next Prestige. */
@Serializable
data class RunStats(
    val peakPopulation: Double = 0.0,
    val peakTechPoints: Double = 0.0,
    val runStartEpochMillis: Long = 0L,
    val prestigeCount: Int = 0,
)
