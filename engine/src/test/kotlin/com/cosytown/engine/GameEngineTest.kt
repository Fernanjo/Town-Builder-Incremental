package com.cosytown.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineTest {

    @Test
    fun `market produces coins scaled by elapsed seconds`() {
        val state = GameState.newRun(0L).copy(buildings = mapOf(BuildingType.MARKET to 2))
        val ticked = GameEngine.tick(state, elapsedSeconds = 10.0, nowEpochMillis = 10_000L)

        // 2 markets * 1.0 coin/s * 10s = 20, multipliers are all 1.0 with no pop/tech/parks yet.
        assertEquals(state.resources.coins + 20.0, ticked.resources.coins, absoluteTolerance = 1e-9)
        assertEquals(10_000L, ticked.lastTickEpochMillis)
    }

    @Test
    fun `research lab is gated by available energy`() {
        val state = GameState.newRun(0L).copy(
            buildings = mapOf(BuildingType.RESEARCH_LAB to 1),
            resources = ResourceState(coins = 0.0, materials = 0.0, energy = 0.4),
        )
        // Demands 0.8 energy for 1s but only 0.4 is available -> half output, energy drained to 0.
        val ticked = GameEngine.tick(state, elapsedSeconds = 1.0, nowEpochMillis = 1000L)

        assertEquals(0.0, ticked.resources.energy, absoluteTolerance = 1e-9)
        assertEquals(0.25, ticked.technology.points, absoluteTolerance = 1e-9)
    }

    @Test
    fun `population grows toward capacity but is capped by upkeep affordability`() {
        val state = GameState.newRun(0L).copy(
            buildings = mapOf(BuildingType.HOUSE to 2),
            resources = ResourceState(coins = 0.0, materials = 0.0, energy = 0.0),
            population = PopulationState(current = 0.0),
        )
        val ticked = GameEngine.tick(state, elapsedSeconds = 1.0, nowEpochMillis = 1000L)

        // No coins/materials income and none in stock -> growth fully blocked this tick.
        assertEquals(0.0, ticked.population.current, absoluteTolerance = 1e-9)
    }

    @Test
    fun `population grows when upkeep is affordable`() {
        val state = GameState.newRun(0L).copy(
            buildings = mapOf(BuildingType.HOUSE to 2),
            resources = ResourceState(coins = 1000.0, materials = 1000.0, energy = 0.0),
        )
        val ticked = GameEngine.tick(state, elapsedSeconds = 1.0, nowEpochMillis = 1000L)

        // capacity = 2*5 = 10, targetGrowth = (10-0)*0.05*1 = 0.5
        assertEquals(0.5, ticked.population.current, absoluteTolerance = 1e-9)
        assertTrue(ticked.resources.coins < 1000.0)
        assertTrue(ticked.resources.materials < 1000.0)
    }

    @Test
    fun `building cost grows geometrically with owned count`() {
        val base = BuildingDefs.costFor(BuildingType.MARKET, 0)
        val afterFive = BuildingDefs.costFor(BuildingType.MARKET, 5)

        assertEquals(40.0, base.coins, absoluteTolerance = 1e-9)
        assertTrue(afterFive.coins > base.coins)
    }

    @Test
    fun `research lab is locked until a power plant is built`() {
        assertFalse(BuildingDefs.isUnlocked(BuildingType.RESEARCH_LAB, emptyMap()))
        assertTrue(BuildingDefs.isUnlocked(BuildingType.RESEARCH_LAB, mapOf(BuildingType.POWER_PLANT to 1)))
    }

    @Test
    fun `buyBuilding fails when unaffordable and succeeds when affordable`() {
        val poor = GameState.newRun(0L).copy(resources = ResourceState(coins = 0.0, materials = 0.0, energy = 0.0))
        assertEquals(null, GameEngine.buyBuilding(poor, BuildingType.MARKET))

        val rich = GameState.newRun(0L).copy(resources = ResourceState(coins = 1000.0, materials = 1000.0, energy = 0.0))
        val afterBuy = GameEngine.buyBuilding(rich, BuildingType.MARKET)
        assertTrue(afterBuy != null)
        assertEquals(1, afterBuy!!.buildings[BuildingType.MARKET])
    }

    @Test
    fun `prestige is unavailable before thresholds are hit`() {
        val fresh = GameState.newRun(0L)
        assertFalse(PrestigeCalculator.isPrestigeAvailable(fresh))
        assertEquals(0L, PrestigeCalculator.legacyPointsPreview(fresh))
    }

    @Test
    fun `prestige legacy points use the geometric mean of peak population and tech`() {
        val state = GameState.newRun(0L).copy(
            runStats = RunStats(peakPopulation = 100.0, peakTechPoints = 100.0, runStartEpochMillis = 0L),
        )
        // sqrt(100*100)/5 = 100/5 = 20
        assertEquals(20L, PrestigeCalculator.legacyPointsPreview(state))
        assertTrue(PrestigeCalculator.isPrestigeAvailable(state))
    }

    @Test
    fun `prestige resets progress but carries legacy points and applies founding treasury bonus`() {
        val state = GameState.newRun(0L).copy(
            buildings = mapOf(BuildingType.HOUSE to 5, BuildingType.MARKET to 3),
            resources = ResourceState(coins = 500.0, materials = 300.0, energy = 20.0),
            population = PopulationState(current = 40.0),
            technology = TechnologyState(points = 60.0),
            runStats = RunStats(peakPopulation = 100.0, peakTechPoints = 100.0, runStartEpochMillis = 0L),
            legacy = LegacyState(legacyPoints = 10, purchasedLevels = mapOf(LegacyUpgradeId.FOUNDING_TREASURY to 2)),
        )

        val next = PrestigeCalculator.prestige(state, nowEpochMillis = 5000L)

        assertTrue(next.buildings.isEmpty())
        assertEquals(0.0, next.population.current)
        assertEquals(0.0, next.technology.points)
        assertEquals(10L + 20L, next.legacy.legacyPoints) // 10 carried + 20 earned this run
        assertEquals(1, next.runStats.prestigeCount)
        // base 50 coins + 2 levels * 50 bonus = 150; base 30 materials + 2 levels * 25 bonus = 80
        assertEquals(150.0, next.resources.coins, absoluteTolerance = 1e-9)
        assertEquals(80.0, next.resources.materials, absoluteTolerance = 1e-9)
    }

    @Test
    fun `legacy upgrade purchase deducts points, increments level, and is capped at max level`() {
        val state = GameState.newRun(0L).copy(legacy = LegacyState(legacyPoints = 100))

        val afterOne = LegacyUpgradeDefs.buy(state, LegacyUpgradeId.MASTER_BUILDERS)
        assertTrue(afterOne != null)
        assertEquals(1, afterOne!!.legacy.purchasedLevels[LegacyUpgradeId.MASTER_BUILDERS])
        assertEquals(97L, afterOne.legacy.legacyPoints) // baseCost 3 for level 0->1

        val poor = state.copy(legacy = LegacyState(legacyPoints = 0))
        assertEquals(null, LegacyUpgradeDefs.buy(poor, LegacyUpgradeId.MASTER_BUILDERS))

        val maxed = state.copy(
            legacy = LegacyState(legacyPoints = 1000, purchasedLevels = mapOf(LegacyUpgradeId.MASTER_BUILDERS to 5)),
        )
        assertEquals(null, LegacyUpgradeDefs.buy(maxed, LegacyUpgradeId.MASTER_BUILDERS))
    }
}

private fun assertEquals(expected: Double, actual: Double, absoluteTolerance: Double) {
    kotlin.test.assertTrue(
        kotlin.math.abs(expected - actual) <= absoluteTolerance,
        "Expected $expected but was $actual (tolerance $absoluteTolerance)",
    )
}
