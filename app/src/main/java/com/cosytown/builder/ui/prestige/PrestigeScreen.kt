package com.cosytown.builder.ui.prestige

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosytown.builder.ui.components.formatCompact
import com.cosytown.engine.GameState
import com.cosytown.engine.PrestigeCalculator

@Composable
fun PrestigeScreen(
    state: GameState,
    onConfirmPrestige: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = PrestigeCalculator.isPrestigeAvailable(state)
    val preview = PrestigeCalculator.legacyPointsPreview(state)
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Prestige", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Reset your town to earn Legacy Points based on the Population and Technology you've " +
                "reached this run. Legacy Points buy permanent upgrades that make your next run faster.",
            style = MaterialTheme.typography.bodyMedium,
        )

        RunStatsCard(state, preview)

        if (!available) {
            Text(
                text = "Prestige unlocks once you reach ${PrestigeCalculator.MIN_PEAK_POPULATION_TO_PRESTIGE.formatCompact()} " +
                    "Population and ${PrestigeCalculator.MIN_PEAK_TECH_POINTS_TO_PRESTIGE.formatCompact()} Tech Points in a single run.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = { showConfirmDialog = true },
            enabled = available,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (available) "Prestige now" else "Not yet available")
        }
    }

    if (showConfirmDialog) {
        PrestigeConfirmDialog(
            legacyPointsPreview = preview,
            onConfirm = {
                showConfirmDialog = false
                onConfirmPrestige()
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

@Composable
private fun RunStatsCard(state: GameState, preview: Long) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatLine("Peak Population", state.runStats.peakPopulation.formatCompact())
            StatLine("Peak Tech Points", state.runStats.peakTechPoints.formatCompact())
            StatLine("Prestiges so far", state.runStats.prestigeCount.toString())
            StatLine("Current Legacy Points", state.legacy.legacyPoints.formatCompact())
            StatLine("Legacy Points on Prestige", "+${preview.formatCompact()}")
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun PrestigeConfirmDialog(legacyPointsPreview: Long, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset your town?") },
        text = {
            Text(
                "This will reset all resources, buildings, Population and Technology. " +
                    "You'll earn +${legacyPointsPreview.formatCompact()} Legacy Points to spend in the Legacy Shop.",
            )
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Prestige") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } },
    )
}
