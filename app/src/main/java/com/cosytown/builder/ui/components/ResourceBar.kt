package com.cosytown.builder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cosytown.builder.ui.theme.CoinsColor
import com.cosytown.builder.ui.theme.EnergyColor
import com.cosytown.builder.ui.theme.HappinessColor
import com.cosytown.builder.ui.theme.MaterialsColor
import com.cosytown.builder.ui.theme.PopulationColor
import com.cosytown.builder.ui.theme.TechnologyColor
import com.cosytown.engine.GameEngine
import com.cosytown.engine.GameState

/** Top-of-screen resource counters, shown on every screen so the player always has context. */
@Composable
fun ResourceBar(state: GameState, modifier: Modifier = Modifier) {
    val multipliers = GameEngine.multipliersFor(state)
    val capacity = GameEngine.populationCapacity(state)

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ResourceChip("Coins", state.resources.coins.formatCompact(), CoinsColor)
        ResourceChip("Materials", state.resources.materials.formatCompact(), MaterialsColor)
        ResourceChip("Energy", state.resources.energy.formatCompact(), EnergyColor)
        ResourceChip(
            "Pop",
            "${state.population.current.formatCompact()}/${capacity.formatCompact()}",
            PopulationColor,
        )
        ResourceChip("Tech", state.technology.points.formatCompact(), TechnologyColor)
        ResourceChip("Happiness", "x%.2f".format(multipliers.happinessMultiplier), HappinessColor)
    }
}

@Composable
private fun ResourceChip(label: String, value: String, color: Color) {
    Row {
        Text(text = "$label ", style = MaterialTheme.typography.labelMedium, color = color)
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
