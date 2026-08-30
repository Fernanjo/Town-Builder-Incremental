package com.cosytown.engine

import kotlin.math.floor

/** Every N population/tech points earned crosses a "tier" that grants +5% to the relevant multiplier. */
private const val POPULATION_PER_MULTIPLIER_TIER = 10.0
private const val TECH_POINTS_PER_MULTIPLIER_TIER = 20.0
private const val MULTIPLIER_BONUS_PER_TIER = 0.05
private const val HAPPINESS_BONUS_PER_PARK = 0.02

private const val POPULATION_GROWTH_RATE_PER_SECOND = 0.05
private const val UPKEEP_COINS_PER_NEW_POP = 2.0
private const val UPKEEP_MATERIALS_PER_NEW_POP = 1.0

/** A snapshot of the multipliers currently in effect, exposed for the UI's stats readout. */
data class MultiplierSnapshot(
    val populationMultiplier: Double,
    val technologyMultiplier: Double,
    val happinessMultiplier: Double,
) {
    /** Applies to Coins, Materials and Energy generation. Tech generation deliberately excludes
     * [technologyMultiplier] so research cannot compound into an unbounded feedback loop. */
    val globalMultiplier: Double get() = populationMultiplier * technologyMultiplier * happinessMultiplier
}

/**
 * Pure simulation core: no I/O, no Android dependency, no wall-clock reads. Every function takes
 * the current [GameState] and returns a new one, so the same tick logic drives both the live 1s
 * ticker and a single large-dt batch for offline catch-up.
 */
object GameEngine {

    fun multipliersFor(state: GameState): MultiplierSnapshot {
        val effects = LegacyUpgradeDefs.effectsFor(state.legacy.purchasedLevels)
        val parkCount = state.buildings[BuildingType.PARK] ?: 0
        val populationTier = floor(state.population.current / POPULATION_PER_MULTIPLIER_TIER)
        val techTier = floor(state.technology.points / TECH_POINTS_PER_MULTIPLIER_TIER)
        return MultiplierSnapshot(
            populationMultiplier = 1.0 + populationTier * MULTIPLIER_BONUS_PER_TIER * (1.0 + effects.populationMultiplierBonus),
            technologyMultiplier = 1.0 + techTier * MULTIPLIER_BONUS_PER_TIER * (1.0 + effects.techMultiplierBonus),
            happinessMultiplier = 1.0 + parkCount * HAPPINESS_BONUS_PER_PARK + effects.happinessMultiplierBonus,
        )
    }

    fun populationCapacity(state: GameState): Double {
        val houses = state.buildings[BuildingType.HOUSE] ?: 0
        return houses * BuildingDefs.of(BuildingType.HOUSE).populationCapacityPerUnit
    }

    /**
     * Advances the simulation by [elapsedSeconds]. Used both for the ~1s foreground ticker and,
     * with a single large capped value, for offline catch-up on resume — the latter is an
     * approximation (it does not replay second-by-second) which is standard for idle games and
     * fine given production rates only change when the player builds something.
     */
    fun tick(state: GameState, elapsedSeconds: Double, nowEpochMillis: Long): GameState {
        if (elapsedSeconds <= 0.0) return state.copy(lastTickEpochMillis = nowEpochMillis)

        val multipliers = multipliersFor(state)
        val counts = state.buildings

        val marketCount = counts[BuildingType.MARKET] ?: 0
        val workshopCount = counts[BuildingType.WORKSHOP] ?: 0
        val powerPlantCount = counts[BuildingType.POWER_PLANT] ?: 0
        val researchLabCount = counts[BuildingType.RESEARCH_LAB] ?: 0

        val coinsGained = marketCount * BuildingDefs.of(BuildingType.MARKET).coinsOutputPerSecond *
            elapsedSeconds * multipliers.globalMultiplier
        val materialsGained = workshopCount * BuildingDefs.of(BuildingType.WORKSHOP).materialsOutputPerSecond *
            elapsedSeconds * multipliers.globalMultiplier
        val energyGenerated = powerPlantCount * BuildingDefs.of(BuildingType.POWER_PLANT).energyOutputPerSecond *
            elapsedSeconds * multipliers.globalMultiplier

        val availableEnergy = state.resources.energy + energyGenerated
        val energyDemand = researchLabCount * BuildingDefs.of(BuildingType.RESEARCH_LAB).energyInputPerSecond * elapsedSeconds
        val energyConsumed = minOf(energyDemand, availableEnergy)
        val energyServedFraction = if (energyDemand > 0.0) energyConsumed / energyDemand else 0.0

        val techGained = researchLabCount * BuildingDefs.of(BuildingType.RESEARCH_LAB).techPointsOutputPerSecond *
            elapsedSeconds * energyServedFraction * multipliers.populationMultiplier * multipliers.happinessMultiplier

        val coinsBeforeUpkeep = state.resources.coins + coinsGained
        val materialsBeforeUpkeep = state.resources.materials + materialsGained

        val capacity = populationCapacity(state)
        val targetGrowth = ((capacity - state.population.current).coerceAtLeast(0.0)) *
            POPULATION_GROWTH_RATE_PER_SECOND * elapsedSeconds
        val coinsNeededForGrowth = targetGrowth * UPKEEP_COINS_PER_NEW_POP
        val materialsNeededForGrowth = targetGrowth * UPKEEP_MATERIALS_PER_NEW_POP
        val affordableFraction = minOf(
            1.0,
            if (coinsNeededForGrowth > 0.0) coinsBeforeUpkeep / coinsNeededForGrowth else 1.0,
            if (materialsNeededForGrowth > 0.0) materialsBeforeUpkeep / materialsNeededForGrowth else 1.0,
        ).coerceIn(0.0, 1.0)
        val actualGrowth = targetGrowth * affordableFraction

        val newPopulation = state.population.current + actualGrowth
        val newTechPoints = state.technology.points + techGained
        val newCoins = coinsBeforeUpkeep - coinsNeededForGrowth * affordableFraction
        val newMaterials = materialsBeforeUpkeep - materialsNeededForGrowth * affordableFraction
        val newEnergy = availableEnergy - energyConsumed

        return state.copy(
            lastTickEpochMillis = nowEpochMillis,
            resources = ResourceState(coins = newCoins, materials = newMaterials, energy = newEnergy),
            population = PopulationState(current = newPopulation),
            technology = TechnologyState(points = newTechPoints),
            runStats = state.runStats.copy(
                peakPopulation = maxOf(state.runStats.peakPopulation, newPopulation),
                peakTechPoints = maxOf(state.runStats.peakTechPoints, newTechPoints),
            ),
        )
    }

    fun canAfford(state: GameState, cost: BuildingCost): Boolean =
        state.resources.coins >= cost.coins && state.resources.materials >= cost.materials

    fun costFor(state: GameState, type: BuildingType): BuildingCost {
        val owned = state.buildings[type] ?: 0
        val effects = LegacyUpgradeDefs.effectsFor(state.legacy.purchasedLevels)
        return BuildingDefs.costFor(type, owned).scaledBy(effects.buildingCostMultiplier)
    }

    /** Returns the updated [GameState] after buying one more [type], or null if locked/unaffordable. */
    fun buyBuilding(state: GameState, type: BuildingType): GameState? {
        if (!BuildingDefs.isUnlocked(type, state.buildings)) return null
        val cost = costFor(state, type)
        if (!canAfford(state, cost)) return null
        val owned = state.buildings[type] ?: 0
        return state.copy(
            resources = state.resources.copy(
                coins = state.resources.coins - cost.coins,
                materials = state.resources.materials - cost.materials,
            ),
            buildings = state.buildings + (type to owned + 1),
        )
    }
}
