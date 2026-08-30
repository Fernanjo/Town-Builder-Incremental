package com.cosytown.engine

import kotlin.math.pow

/** Coins/Materials price of constructing one more of a building. */
data class BuildingCost(
    val coins: Double = 0.0,
    val materials: Double = 0.0,
) {
    fun scaledBy(factor: Double) = BuildingCost(coins * factor, materials * factor)
}

/**
 * Static, non-persisted configuration for one building type: what it costs to build and what it
 * produces/consumes per unit per second. Values here are the vertical-slice defaults called out
 * in the design brief as tunable after playtesting.
 */
data class BuildingDef(
    val type: BuildingType,
    val displayName: String,
    val description: String,
    val baseCost: BuildingCost,
    val costGrowth: Double,
    val coinsOutputPerSecond: Double = 0.0,
    val materialsOutputPerSecond: Double = 0.0,
    val energyOutputPerSecond: Double = 0.0,
    val energyInputPerSecond: Double = 0.0,
    val techPointsOutputPerSecond: Double = 0.0,
    val populationCapacityPerUnit: Double = 0.0,
    val happinessPerUnit: Double = 0.0,
    val unlockRequirement: (Map<BuildingType, Int>) -> Boolean = { true },
)

object BuildingDefs {

    val ALL: List<BuildingDef> = listOf(
        BuildingDef(
            type = BuildingType.HOUSE,
            displayName = "House",
            description = "Provides housing capacity so your Population can grow.",
            baseCost = BuildingCost(coins = 50.0, materials = 25.0),
            costGrowth = 1.15,
            populationCapacityPerUnit = 5.0,
        ),
        BuildingDef(
            type = BuildingType.MARKET,
            displayName = "Market",
            description = "Generates Coins.",
            baseCost = BuildingCost(coins = 40.0),
            costGrowth = 1.13,
            coinsOutputPerSecond = 1.0,
        ),
        BuildingDef(
            type = BuildingType.WORKSHOP,
            displayName = "Workshop",
            description = "Generates Materials.",
            baseCost = BuildingCost(coins = 45.0),
            costGrowth = 1.13,
            materialsOutputPerSecond = 0.6,
        ),
        BuildingDef(
            type = BuildingType.POWER_PLANT,
            displayName = "Power Plant",
            description = "Generates Energy to fuel Research Labs.",
            baseCost = BuildingCost(coins = 100.0, materials = 50.0),
            costGrowth = 1.18,
            energyOutputPerSecond = 1.0,
        ),
        BuildingDef(
            type = BuildingType.RESEARCH_LAB,
            displayName = "Research Lab",
            description = "Consumes Energy to generate Tech Points. Requires a Power Plant.",
            baseCost = BuildingCost(coins = 150.0, materials = 75.0),
            costGrowth = 1.20,
            energyInputPerSecond = 0.8,
            techPointsOutputPerSecond = 0.5,
            unlockRequirement = { counts -> (counts[BuildingType.POWER_PLANT] ?: 0) >= 1 },
        ),
        BuildingDef(
            type = BuildingType.PARK,
            displayName = "Park",
            description = "Raises Happiness, a global multiplier on all production.",
            baseCost = BuildingCost(coins = 60.0),
            costGrowth = 1.12,
            happinessPerUnit = 0.02,
        ),
    )

    private val byType = ALL.associateBy { it.type }

    fun of(type: BuildingType): BuildingDef = byType.getValue(type)

    fun isUnlocked(type: BuildingType, ownedCounts: Map<BuildingType, Int>): Boolean =
        of(type).unlockRequirement(ownedCounts)

    /** Cost to build the (ownedCount + 1)-th unit of [type], before any Legacy discounts. */
    fun costFor(type: BuildingType, ownedCount: Int): BuildingCost {
        val def = of(type)
        val growth = def.costGrowth.pow(ownedCount)
        return def.baseCost.scaledBy(growth)
    }
}
