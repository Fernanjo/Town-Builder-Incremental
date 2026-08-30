package com.cosytown.engine

import kotlin.math.pow

enum class LegacyUpgradeId {
    MASTER_BUILDERS,
    EFFICIENT_WORKERS,
    RESEARCH_GRANTS,
    FOUNDING_TREASURY,
    COMMUNITY_SPIRIT,
}

/** Static definition of one permanent Legacy Shop upgrade. Cost grows linearly per level. */
data class LegacyUpgradeDef(
    val id: LegacyUpgradeId,
    val displayName: String,
    val description: String,
    val maxLevel: Int,
    val baseCost: Long,
    val costIncrementPerLevel: Long,
) {
    /** Legacy Point cost to go from [currentLevel] to currentLevel + 1. */
    fun costForNextLevel(currentLevel: Int): Long = baseCost + costIncrementPerLevel * currentLevel
}

/** The combined, resolved effect of every Legacy upgrade level the player has ever purchased. */
data class LegacyEffects(
    val buildingCostMultiplier: Double = 1.0,
    val populationMultiplierBonus: Double = 0.0,
    val techMultiplierBonus: Double = 0.0,
    val happinessMultiplierBonus: Double = 0.0,
    val startingCoinsBonus: Double = 0.0,
    val startingMaterialsBonus: Double = 0.0,
)

object LegacyUpgradeDefs {

    val ALL: List<LegacyUpgradeDef> = listOf(
        LegacyUpgradeDef(
            id = LegacyUpgradeId.MASTER_BUILDERS,
            displayName = "Master Builders",
            description = "All building costs are permanently reduced by 5% per level.",
            maxLevel = 5,
            baseCost = 3,
            costIncrementPerLevel = 3,
        ),
        LegacyUpgradeDef(
            id = LegacyUpgradeId.EFFICIENT_WORKERS,
            displayName = "Efficient Workers",
            description = "Population's production bonus is permanently increased by 10% per level.",
            maxLevel = 5,
            baseCost = 3,
            costIncrementPerLevel = 3,
        ),
        LegacyUpgradeDef(
            id = LegacyUpgradeId.RESEARCH_GRANTS,
            displayName = "Research Grants",
            description = "Technology's efficiency bonus is permanently increased by 10% per level.",
            maxLevel = 5,
            baseCost = 3,
            costIncrementPerLevel = 3,
        ),
        LegacyUpgradeDef(
            id = LegacyUpgradeId.FOUNDING_TREASURY,
            displayName = "Founding Treasury",
            description = "Start every new run with +50 Coins and +25 Materials per level.",
            maxLevel = 5,
            baseCost = 2,
            costIncrementPerLevel = 2,
        ),
        LegacyUpgradeDef(
            id = LegacyUpgradeId.COMMUNITY_SPIRIT,
            displayName = "Community Spirit",
            description = "Happiness's global production multiplier is permanently increased by 5% per level.",
            maxLevel = 5,
            baseCost = 3,
            costIncrementPerLevel = 3,
        ),
    )

    private val byId = ALL.associateBy { it.id }

    fun of(id: LegacyUpgradeId): LegacyUpgradeDef = byId.getValue(id)

    fun effectsFor(levels: Map<LegacyUpgradeId, Int>): LegacyEffects {
        val masterBuilders = levels[LegacyUpgradeId.MASTER_BUILDERS] ?: 0
        val efficientWorkers = levels[LegacyUpgradeId.EFFICIENT_WORKERS] ?: 0
        val researchGrants = levels[LegacyUpgradeId.RESEARCH_GRANTS] ?: 0
        val foundingTreasury = levels[LegacyUpgradeId.FOUNDING_TREASURY] ?: 0
        val communitySpirit = levels[LegacyUpgradeId.COMMUNITY_SPIRIT] ?: 0
        return LegacyEffects(
            buildingCostMultiplier = 0.95.pow(masterBuilders),
            populationMultiplierBonus = 0.10 * efficientWorkers,
            techMultiplierBonus = 0.10 * researchGrants,
            happinessMultiplierBonus = 0.05 * communitySpirit,
            startingCoinsBonus = 50.0 * foundingTreasury,
            startingMaterialsBonus = 25.0 * foundingTreasury,
        )
    }

    /** Returns the updated [GameState] after buying one level of [id], or null if not affordable / maxed out. */
    fun buy(state: GameState, id: LegacyUpgradeId): GameState? {
        val def = of(id)
        val currentLevel = state.legacy.purchasedLevels[id] ?: 0
        if (currentLevel >= def.maxLevel) return null
        val cost = def.costForNextLevel(currentLevel)
        if (state.legacy.legacyPoints < cost) return null
        return state.copy(
            legacy = state.legacy.copy(
                legacyPoints = state.legacy.legacyPoints - cost,
                purchasedLevels = state.legacy.purchasedLevels + (id to currentLevel + 1),
            ),
        )
    }
}
